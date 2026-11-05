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
    val answer: String? = null,
    val isKeyboardVisible: Boolean = true,
    val selectedTab: KeyboardTab = KeyboardTab.BASIC
)

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val symjaEngine = SymjaEngine()
    private var autoRandomizeJob: Job? = null

    private val knownFunctions = listOf(
        "arcsin(", "arccos(", "arctan(",
        "sin⁻¹(", "cos⁻¹(", "tan⁻¹(",
        "sinh(", "cosh(", "tanh(",
        "cbrt(", "root(", "asin(", "acos(", "atan(",
        "sin(", "cos(", "tan(", "sec(", "csc(",
        "log2(", "log(", "ln(", "exp(",
        "integrate(", "d/dx(", "lim(", "sum(", "mod(",
        "abs(", "sqrt("
    )

    private val multiCharSymbols = listOf("==", "!=", "<=", ">=")

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

    fun onSelectAlternate(baseKey: SciCalcKey, altKey: SciCalcKey) {
        onBackspace()
        onKeyClick(altKey)
    }

    fun onKeyClick(key: SciCalcKey) {
        val current = _uiState.value.formulaInput
        val text = current.text
        val selection = current.selection
        val start = selection.min
        val end = selection.max

        when (key.label) {
            "a/b", "÷", "/" -> {
                handleFractionInput(text, start, end)
                return
            }
            "x²" -> {
                handlePowerInput(text, start, end, "²")
                return
            }
            "x³" -> {
                handlePowerInput(text, start, end, "³")
                return
            }
            "x⁴" -> {
                handlePowerInput(text, start, end, "^(4)")
                return
            }
            "xⁿ", "^" -> {
                handleExponentInput(text, start, end, "^()")
                return
            }
            "10ⁿ" -> {
                handleDirectExponentInput(text, start, end, "10^()")
                return
            }
            "eⁿ" -> {
                handleDirectExponentInput(text, start, end, "e^()")
                return
            }
            "√x", "√" -> {
                handleSquareRootInput(text, start, end, "√()")
                return
            }
            "∛" -> {
                handleSquareRootInput(text, start, end, "cbrt()")
                return
            }
            "ⁿ√" -> {
                handleSquareRootInput(text, start, end, "root()")
                return
            }
            "|x|" -> {
                handleAbsoluteValueInput(text, start, end)
                return
            }
        }

        val insertText = key.insertText
        val cursorOffset = key.cursorOffset

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

    private fun handleFractionInput(text: String, start: Int, end: Int) {
        if (start != end) {
            val selected = text.substring(start, end)
            val wrapped = "($selected)/()"
            val newText = text.substring(0, start) + wrapped + text.substring(end)
            val newCursor = start + wrapped.length - 1
            val newTfv = TextFieldValue(newText, selection = TextRange(newCursor))
            _uiState.update { it.copy(formulaInput = newTfv) }
            validateFormula(newText)
            return
        }

        if (start == 0) {
            val newText = "()/()" + text
            val newCursor = 1
            val newTfv = TextFieldValue(newText, selection = TextRange(newCursor))
            _uiState.update { it.copy(formulaInput = newTfv) }
            validateFormula(newText)
            return
        }

        val prevChar = text[start - 1]
        if (isOperatorOrDelimiter(prevChar)) {
            val insert = "()/()"
            val newText = text.substring(0, start) + insert + text.substring(start)
            val newCursor = start + 1
            val newTfv = TextFieldValue(newText, selection = TextRange(newCursor))
            _uiState.update { it.copy(formulaInput = newTfv) }
            validateFormula(newText)
            return
        }

        val termStart = findPrecedingTermStart(text, start)
        val term = text.substring(termStart, start).trim()
        val wrappedNumerator = if (term.startsWith("(") && term.endsWith(")")) term else "($term)"
        val insert = "$wrappedNumerator/()"
        val newText = text.substring(0, termStart) + insert + text.substring(start)
        val newCursor = termStart + insert.length - 1
        val newTfv = TextFieldValue(newText, selection = TextRange(newCursor))
        _uiState.update { it.copy(formulaInput = newTfv) }
        validateFormula(newText)
    }

    private fun handlePowerInput(text: String, start: Int, end: Int, powerStr: String) {
        if (start == 0 || isOperatorOrDelimiter(text[start - 1])) {
            val insert = "()$powerStr"
            val newText = text.substring(0, start) + insert + text.substring(end)
            val newCursor = start + 1
            val newTfv = TextFieldValue(newText, selection = TextRange(newCursor))
            _uiState.update { it.copy(formulaInput = newTfv) }
            validateFormula(newText)
            return
        }

        val newText = text.substring(0, start) + powerStr + text.substring(end)
        val newCursor = start + powerStr.length
        val newTfv = TextFieldValue(newText, selection = TextRange(newCursor))
        _uiState.update { it.copy(formulaInput = newTfv) }
        validateFormula(newText)
    }

    private fun handleExponentInput(text: String, start: Int, end: Int, expPattern: String) {
        if (start != end) {
            val selected = text.substring(start, end)
            val wrapped = "($selected)^()"
            val newText = text.substring(0, start) + wrapped + text.substring(end)
            val newCursor = start + wrapped.length - 1
            val newTfv = TextFieldValue(newText, selection = TextRange(newCursor))
            _uiState.update { it.copy(formulaInput = newTfv) }
            validateFormula(newText)
            return
        }

        if (start == 0 || isOperatorOrDelimiter(text[start - 1])) {
            val insert = "()^()"
            val newText = text.substring(0, start) + insert + text.substring(start)
            val newCursor = start + 1
            val newTfv = TextFieldValue(newText, selection = TextRange(newCursor))
            _uiState.update { it.copy(formulaInput = newTfv) }
            validateFormula(newText)
            return
        }

        val insert = "^()"
        val newText = text.substring(0, start) + insert + text.substring(start)
        val newCursor = start + insert.length - 1
        val newTfv = TextFieldValue(newText, selection = TextRange(newCursor))
        _uiState.update { it.copy(formulaInput = newTfv) }
        validateFormula(newText)
    }

    private fun handleDirectExponentInput(text: String, start: Int, end: Int, pattern: String) {
        val newText = text.substring(0, start) + pattern + text.substring(end)
        val newCursor = start + pattern.length - 1
        val newTfv = TextFieldValue(newText, selection = TextRange(newCursor))
        _uiState.update { it.copy(formulaInput = newTfv) }
        validateFormula(newText)
    }

    private fun handleSquareRootInput(text: String, start: Int, end: Int, template: String) {
        if (start != end) {
            val selected = text.substring(start, end)
            val fnPrefix = template.substringBeforeLast(")")
            val wrapped = "$fnPrefix$selected)"
            val newText = text.substring(0, start) + wrapped + text.substring(end)
            val newCursor = start + wrapped.length
            val newTfv = TextFieldValue(newText, selection = TextRange(newCursor))
            _uiState.update { it.copy(formulaInput = newTfv) }
            validateFormula(newText)
            return
        }

        val newText = text.substring(0, start) + template + text.substring(start)
        val newCursor = start + template.length - 1
        val newTfv = TextFieldValue(newText, selection = TextRange(newCursor))
        _uiState.update { it.copy(formulaInput = newTfv) }
        validateFormula(newText)
    }

    private fun handleAbsoluteValueInput(text: String, start: Int, end: Int) {
        if (start != end) {
            val selected = text.substring(start, end)
            val wrapped = "|$selected|"
            val newText = text.substring(0, start) + wrapped + text.substring(end)
            val newCursor = start + wrapped.length
            val newTfv = TextFieldValue(newText, selection = TextRange(newCursor))
            _uiState.update { it.copy(formulaInput = newTfv) }
            validateFormula(newText)
            return
        }

        val insert = "| |"
        val newText = text.substring(0, start) + insert + text.substring(start)
        val newCursor = start + 1
        val newTfv = TextFieldValue(newText, selection = TextRange(newCursor))
        _uiState.update { it.copy(formulaInput = newTfv) }
        validateFormula(newText)
    }

    private fun findPrecedingTermStart(text: String, end: Int): Int {
        if (end <= 0) return 0
        if (text[end - 1] == ')') {
            var depth = 1
            var i = end - 2
            while (i >= 0) {
                if (text[i] == ')') depth++
                else if (text[i] == '(') {
                    depth--
                    if (depth == 0) {
                        var fnStart = i
                        while (fnStart > 0 && text[fnStart - 1].isLetter()) {
                            fnStart--
                        }
                        return fnStart
                    }
                }
                i--
            }
            return 0
        }

        var i = end - 1
        while (i >= 0) {
            val c = text[i]
            if (isOperatorOrDelimiter(c)) {
                return i + 1
            }
            i--
        }
        return 0
    }

    private fun isOperatorOrDelimiter(c: Char): Boolean {
        return c == '+' || c == '-' || c == '−' || c == '*' || c == '·' || c == '/' || c == '=' || c == '<' || c == '>' || c == '(' || c == ',' || c == '^' || c == '|' || c == '!'
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
            return
        }

        val pos = selection.min
        if (pos <= 0) return

        for (fn in knownFunctions) {
            val fnLen = fn.length
            val emptyPattern = fn + ")"
            if (pos >= fnLen && text.startsWith(emptyPattern, pos - fnLen)) {
                val startDel = pos - fnLen
                val newText = text.removeRange(startDel, startDel + emptyPattern.length)
                val newTfv = TextFieldValue(newText, selection = TextRange(startDel))
                _uiState.update { it.copy(formulaInput = newTfv) }
                validateFormula(newText)
                return
            }
        }

        if (pos >= 2 && text.startsWith("√()", pos - 2)) {
            val newText = text.removeRange(pos - 2, pos + 1)
            val newTfv = TextFieldValue(newText, selection = TextRange(pos - 2))
            _uiState.update { it.copy(formulaInput = newTfv) }
            validateFormula(newText)
            return
        }

        if (pos >= 2 && text.startsWith("^()", pos - 2)) {
            val newText = text.removeRange(pos - 2, pos + 1)
            val newTfv = TextFieldValue(newText, selection = TextRange(pos - 2))
            _uiState.update { it.copy(formulaInput = newTfv) }
            validateFormula(newText)
            return
        }

        if (pos >= 2 && text.startsWith("/()", pos - 2)) {
            val newText = text.removeRange(pos - 2, pos + 1)
            val newTfv = TextFieldValue(newText, selection = TextRange(pos - 2))
            _uiState.update { it.copy(formulaInput = newTfv) }
            validateFormula(newText)
            return
        }

        if (pos >= 1 && pos < text.length && text[pos - 1] == '|' && text[pos] == '|') {
            val newText = text.removeRange(pos - 1, pos + 1)
            val newTfv = TextFieldValue(newText, selection = TextRange(pos - 1))
            _uiState.update { it.copy(formulaInput = newTfv) }
            validateFormula(newText)
            return
        }

        for (sym in multiCharSymbols) {
            if (pos >= sym.length && text.startsWith(sym, pos - sym.length)) {
                val startDel = pos - sym.length
                val newText = text.removeRange(startDel, pos)
                val newTfv = TextFieldValue(newText, selection = TextRange(startDel))
                _uiState.update { it.copy(formulaInput = newTfv) }
                validateFormula(newText)
                return
            }
        }

        val removeIdx = pos - 1
        val newText = text.removeRange(removeIdx, removeIdx + 1)
        val newTfv = TextFieldValue(newText, selection = TextRange(removeIdx))
        _uiState.update { it.copy(formulaInput = newTfv) }
        validateFormula(newText)
    }

    fun onClear() {
        autoRandomizeJob?.cancel()
        val newTfv = TextFieldValue("", selection = TextRange.Zero)
        _uiState.update { it.copy(formulaInput = newTfv, mutationResult = null, answer = null) }
        validateFormula("")
    }

    fun onCursorLeft() {
        val current = _uiState.value.formulaInput
        val text = current.text
        val pos = current.selection.min
        if (pos <= 0) return

        for (fn in knownFunctions) {
            val fnLen = fn.length
            if (pos in 1..text.length) {
                for (offset in 1..fnLen) {
                    val candidateStart = pos - offset
                    if (candidateStart >= 0 && text.startsWith(fn, candidateStart)) {
                        _uiState.update { it.copy(formulaInput = current.copy(selection = TextRange(candidateStart))) }
                        return
                    }
                }
            }
        }

        for (sym in multiCharSymbols) {
            if (pos >= sym.length && text.startsWith(sym, pos - sym.length)) {
                _uiState.update { it.copy(formulaInput = current.copy(selection = TextRange(pos - sym.length))) }
                return
            }
        }

        val newPos = (pos - 1).coerceAtLeast(0)
        _uiState.update { it.copy(formulaInput = current.copy(selection = TextRange(newPos))) }
    }

    fun onCursorRight() {
        val current = _uiState.value.formulaInput
        val text = current.text
        val pos = current.selection.max
        if (pos >= text.length) return

        for (fn in knownFunctions) {
            if (text.startsWith(fn, pos)) {
                val target = (pos + fn.length).coerceAtMost(text.length)
                _uiState.update { it.copy(formulaInput = current.copy(selection = TextRange(target))) }
                return
            }
        }

        for (sym in multiCharSymbols) {
            if (text.startsWith(sym, pos)) {
                val target = (pos + sym.length).coerceAtMost(text.length)
                _uiState.update { it.copy(formulaInput = current.copy(selection = TextRange(target))) }
                return
            }
        }

        if (text[pos] == ')' || text[pos] == '|') {
            val stepOutPos = (pos + 1).coerceAtMost(text.length)
            _uiState.update { it.copy(formulaInput = current.copy(selection = TextRange(stepOutPos))) }
            return
        }

        val newPos = (pos + 1).coerceAtMost(text.length)
        _uiState.update { it.copy(formulaInput = current.copy(selection = TextRange(newPos))) }
    }

    fun randomize() {
        val validation = _uiState.value.validation
        if (!validation.isValid || validation.astNode == null) return

        val result = symjaEngine.mutate(validation)
        val ans = symjaEngine.evaluateAnswer(result.mutatedFormula, result.variableValues)
        _uiState.update { it.copy(mutationResult = result, answer = ans.ifEmpty { null }) }
    }

    fun applyMutatedToInput() {
        val mutated = _uiState.value.mutationResult?.mutatedFormula ?: return
        val displayFormula = com.example.ui.MathFormatter.toDisplayString(mutated)
        val newTfv = TextFieldValue(displayFormula, selection = TextRange(displayFormula.length))
        _uiState.update { it.copy(formulaInput = newTfv) }
        validateFormula(displayFormula)
    }

    fun applyAnswerToInput() {
        val ans = _uiState.value.answer ?: return
        val displayAns = com.example.ui.MathFormatter.toDisplayString(ans)
        val newTfv = TextFieldValue(displayAns, selection = TextRange(displayAns.length))
        _uiState.update { it.copy(formulaInput = newTfv) }
        validateFormula(displayAns)
    }

    private fun validateFormula(input: String, debounceMs: Long = 350L) {
        val sanitized = input.replace("()", "").trim()
        if (sanitized.isEmpty() || sanitized.endsWith("/") || sanitized.endsWith("^") || sanitized.endsWith("√")) {
            _uiState.update {
                it.copy(
                    validation = SymjaValidation(
                        isValid = false,
                        astNode = null,
                        variables = emptyList(),
                        constants = emptyList(),
                        operators = emptyList()
                    ),
                    mutationResult = null,
                    answer = null
                )
            }
            return
        }

        val validation = symjaEngine.validate(input)
        _uiState.update { it.copy(validation = validation) }

        autoRandomizeJob?.cancel()
        if (validation.isValid && validation.astNode != null) {
            autoRandomizeJob = viewModelScope.launch {
                if (debounceMs > 0) {
                    delay(debounceMs)
                }
                val result = symjaEngine.mutate(validation)
                val target = result.mutatedFormula.ifEmpty { input }
                val ans = symjaEngine.evaluateAnswer(target, result.variableValues)
                _uiState.update { it.copy(mutationResult = result, answer = ans.ifEmpty { null }) }
            }
        } else {
            _uiState.update { it.copy(mutationResult = null, answer = null) }
        }
    }
}
