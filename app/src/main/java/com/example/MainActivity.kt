package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.symja.MutationResult
import com.example.symja.SymjaValidation
import com.example.ui.keyboard.SciCalcKeyboard
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRose
import com.example.ui.theme.BgDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.BorderFocused
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.SageFormulaLabTheme
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextTertiary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SageFormulaLabTheme {
                val viewModel: MainViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()
                FormulaLabScreen(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormulaLabScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val outsideInteractionSource = remember { MutableInteractionSource() }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.linearGradient(listOf(AccentCyan, AccentIndigo))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Functions,
                                contentDescription = "App Icon",
                                tint = BgDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Formula Lab",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgDark
                )
            )
        },
        containerColor = BgDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = outsideInteractionSource,
                        indication = null
                    ) {
                        viewModel.setKeyboardVisible(false)
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 680.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            FormulaBox(
                                value = uiState.formulaInput,
                                validation = uiState.validation,
                                isKeyboardVisible = uiState.isKeyboardVisible,
                                onValueChange = viewModel::onFormulaChange,
                                onFocusBox = { viewModel.setKeyboardVisible(true) },
                                onClear = viewModel::onClear,
                                onSlotClick = viewModel::onCursorSet
                            )

                            OutputBox(
                                mutationResult = uiState.mutationResult,
                                onCopy = { text ->
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Formula", text))
                                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                                },
                                onApply = viewModel::applyMutatedToInput,
                                onReroll = viewModel::randomize,
                                onOutsideClick = { viewModel.setKeyboardVisible(false) }
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            if (uiState.isKeyboardVisible) {
                SciCalcKeyboard(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = viewModel::onTabSelected,
                    onKeyClick = viewModel::onKeyClick,
                    onKeyLongClick = viewModel::onKeyLongClick,
                    onNextStep = viewModel::onNextStep,
                    onBackspace = viewModel::onBackspace,
                    onClear = viewModel::onClear,
                    onCursorLeft = viewModel::onCursorLeft,
                    onCursorRight = viewModel::onCursorRight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FormulaBox(
    value: androidx.compose.ui.text.input.TextFieldValue,
    validation: SymjaValidation,
    isKeyboardVisible: Boolean,
    onValueChange: (androidx.compose.ui.text.input.TextFieldValue) -> Unit,
    onFocusBox: () -> Unit,
    onClear: () -> Unit,
    onSlotClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val textInteractionSource = remember { MutableInteractionSource() }

    LaunchedEffect(textInteractionSource) {
        textInteractionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Press || interaction is PressInteraction.Release) {
                onFocusBox()
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    onFocusBox()
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onFocusBox()
            }
            .testTag("formula_input_box"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(
            width = if (isKeyboardVisible) 2.dp else 1.dp,
            color = if (isKeyboardVisible) BorderFocused else BorderDark
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "┌── Formula ────────────────────┐",
                    color = AccentCyan,
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                if (value.text.isNotEmpty()) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("formula_btn_clear")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear formula",
                            tint = TextTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BgDark)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(pass = PointerEventPass.Initial)
                            onFocusBox()
                        }
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onFocusBox()
                    }
                    .padding(12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.text.isEmpty()) {
                    Text(
                        text = "Tap to enter formula",
                        color = TextTertiary,
                        fontSize = 16.sp,
                        fontFamily = JetBrainsMono
                    )
                } else {
                    com.example.ui.math.MathFormulaView(
                        formula = value.text,
                        cursorIndex = if (isKeyboardVisible) value.selection.min else -1,
                        fontSize = 22.sp,
                        color = TextPrimary,
                        onSlotClick = onSlotClick
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "└────────────────────────────┘",
                color = AccentCyan,
                fontFamily = JetBrainsMono,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun OutputBox(
    mutationResult: MutationResult?,
    onCopy: (String) -> Unit,
    onApply: () -> Unit,
    onReroll: () -> Unit,
    onOutsideClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onOutsideClick()
            }
            .testTag("output_card_box"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, BorderDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "┌── Output─────────────────────┐",
                    color = AccentPurple,
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                if (mutationResult != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onReroll,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("output_btn_reroll")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reroll",
                                tint = AccentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = { onCopy(mutationResult.mutatedFormula) },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("output_btn_copy")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy output",
                                tint = AccentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = onApply,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("output_btn_apply")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Apply to formula",
                                tint = AccentIndigo,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BgDark)
                    .padding(12.dp)
                    .testTag("output_formula_display"),
                contentAlignment = Alignment.CenterStart
            ) {
                if (mutationResult != null) {
                    val displayFormula = com.example.ui.MathFormatter.toDisplayString(mutationResult.mutatedFormula)
                    com.example.ui.math.MathFormulaView(
                        formula = displayFormula,
                        fontSize = 22.sp,
                        color = AccentCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "└────────────────────────────┘",
                color = AccentPurple,
                fontFamily = JetBrainsMono,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
