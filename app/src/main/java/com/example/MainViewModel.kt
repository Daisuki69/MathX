package com.example

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.symja.MutationResult
import com.example.symja.SymjaEngine
import com.example.symja.SymjaValidation
import com.example.ui.keyboard.KeyboardTab
import com.example.ui.keyboard.SciCalcKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val formulaInput: TextFieldValue = TextFieldValue(""),
    val validation: SymjaValidation = SymjaValidation(
        isValid = true,
        astNode = null,
        variables = emptyList(),
        constants = emptyList(),
        operators = emptyList()
    ),
    val mutationResult: MutationResult? = null,
    val isKeyboardVisible: Boolean = true,
    val selectedTab: KeyboardTab = KeyboardTab.BASIC
)

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val symjaEngine = SymjaEngine()
    private var autoRandomizeJob: Job? = null

    init {
        validateFormula(_uiState.value.formulaInput.text, debounceMs = 0)
    }

    fun onFormulaChange(newValue: TextFieldValue) {
        _uiState.update { it.copy(formulaInput = newValue) }
        validateFormula(newValue.text)
    }

    fun setKeyboardVisible(visible: Boolean) {
        _uiState.update { it.copy(isKeyboardVisible = visible) }
    }

    fun onTabSelected(tab: KeyboardTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onKeyClick(key: SciCalcKey) {
        val current = _uiState.value.formulaInput
        val text = current.text
        val selection = current.selection

        val start = selection.min
        val end = selection.max

        val (insertText, cursorOffset) = when (key.label) {
            "x²" -> {
                if (start > 0 && isPrecedingTerm(text, start)) {
                    Pair("²", 0)
                } else {
                    Pair("x²", 0)
                }
            }
            "xⁿ" -> {
                if (start > 0 && isPrecedingTerm(text, start)) {
                    Pair("^", 0)
                } else {
                    Pair("x^", 0)
                }
            }
            "√x" -> Pair("√()", -1)
            "|x|" -> Pair("| |", -1)
            "a/b" -> {
                if (start == 0 || !isPrecedingTerm(text, start)) {
                    Pair("1/", 0)
                } else {
                    Pair("/", 0)
                }
            }
            else -> Pair(key.insertText, key.cursorOffset)
        }

        val newText = buildString {
            append(text.substring(0, start))
            append(insertText)
            append(text.substring(end))
        }

        val rawNewCursor = start + insertText.length + cursorOffset
        val newCursor = rawNewCursor.coerceIn(0, newText.length)

        val newTfv = TextFieldValue(newText, selection = TextRange(newCursor))
        _uiState.update { it.copy(formulaInput = newTfv) }
        validateFormula(newText)
    }

    private fun isPrecedingTerm(text: String, index: Int): Boolean {
        val prevChar = text.getOrNull(index - 1) ?: return false
        return prevChar.isLetterOrDigit() || prevChar == ')' || prevChar == '²' || prevChar == '³' || prevChar == 'ⁿ' || prevChar == '|'
    }

    fun onKeyLongClick(key: SciCalcKey) {
        val altText = key.altInsertText ?: return
        val current = _uiState.value.formulaInput
        val text = current.text
        val selection = current.selection
        val start = selection.min
        val end = selection.max

        val newText = buildString {
            append(text.substring(0, start))
            append(altText)
            append(text.substring(end))
        }

        val rawNewCursor = start + altText.length + key.altCursorOffset
        val newCursor = rawNewCursor.coerceIn(0, newText.length)

        val newTfv = TextFieldValue(newText, selection = TextRange(newCursor))
        _uiState.update { it.copy(formulaInput = newTfv) }
        validateFormula(newText)
    }

    fun onNextStep() {
        val current = _uiState.value.formulaInput
        val text = current.text
        val pos = current.selection.min

        if (pos >= text.length) return

        var nextPos = text.length
        for (i in (pos + 1) until text.length) {
            val c = text[i]
            if (c == ')' || c == '|' || c == ',' || c == '+' || c == '−' || c == '-' || c == '=') {
                nextPos = if (c == ')' || c == '|') i + 1 else i
                break
            }
        }

        val clamped = nextPos.coerceIn(0, text.length)
        _uiState.update { it.copy(formulaInput = current.copy(selection = TextRange(clamped))) }
    }

    fun onCursorSet(index: Int) {
        val current = _uiState.value.formulaInput
        val clamped = index.coerceIn(0, current.text.length)
        _uiState.update { it.copy(formulaInput = current.copy(selection = TextRange(clamped))) }
    }

    fun onBackspace() {
        val current = _uiState.value.formulaInput
        val text = current.text
        val selection = current.selection

        if (selection.min != selection.max) {
            val newText = text.removeRange(selection.min, selection.max)
            val newTfv = TextFieldValue(newText, selection = TextRange(selection.min))
            _uiState.update { it.copy(formulaInput = newTfv) }
            validateFormula(newText)
        } else if (selection.min > 0) {
            val removeIdx = selection.min - 1
            val newText = text.removeRange(removeIdx, removeIdx + 1)
            val newTfv = TextFieldValue(newText, selection = TextRange(removeIdx))
            _uiState.update { it.copy(formulaInput = newTfv) }
            validateFormula(newText)
        }
    }

    fun onClear() {
        autoRandomizeJob?.cancel()
        val newTfv = TextFieldValue("", selection = TextRange.Zero)
        _uiState.update { it.copy(formulaInput = newTfv, mutationResult = null) }
        validateFormula("")
    }

    fun onCursorLeft() {
        val current = _uiState.value.formulaInput
        val newPos = (current.selection.min - 1).coerceAtLeast(0)
        _uiState.update { it.copy(formulaInput = current.copy(selection = TextRange(newPos))) }
    }

    fun onCursorRight() {
        val current = _uiState.value.formulaInput
        val newPos = (current.selection.max + 1).coerceAtMost(current.text.length)
        _uiState.update { it.copy(formulaInput = current.copy(selection = TextRange(newPos))) }
    }

    fun randomize() {
        val validation = _uiState.value.validation
        if (!validation.isValid || validation.astNode == null) return

        val result = symjaEngine.mutate(validation)
        _uiState.update { it.copy(mutationResult = result) }
    }

    fun applyMutatedToInput() {
        val mutated = _uiState.value.mutationResult?.mutatedFormula ?: return
        val newTfv = TextFieldValue(mutated, selection = TextRange(mutated.length))
        _uiState.update { it.copy(formulaInput = newTfv) }
        validateFormula(mutated)
    }

    private fun validateFormula(input: String, debounceMs: Long = 400L) {
        val validation = symjaEngine.validate(input)
        _uiState.update { it.copy(validation = validation) }

        autoRandomizeJob?.cancel()
        if (validation.isValid && validation.astNode != null) {
            autoRandomizeJob = viewModelScope.launch {
                if (debounceMs > 0) {
                    delay(debounceMs)
                }
                val result = symjaEngine.mutate(validation)
                _uiState.update { it.copy(mutationResult = result) }
            }
        } else {
            _uiState.update { it.copy(mutationResult = null) }
        }
    }
}
