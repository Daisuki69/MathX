package com.example.ui.keyboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.ui.theme.JetBrainsMono
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SciCalcKeyboard(
    selectedTab: KeyboardTab,
    onTabSelected: (KeyboardTab) -> Unit,
    onKeyClick: (SciCalcKey) -> Unit,
    onSelectAlternate: (SciCalcKey, SciCalcKey) -> Unit = { _, alt -> onKeyClick(alt) },
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onCursorLeft: () -> Unit,
    onCursorRight: () -> Unit,
    onNextStep: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("scicalc_keyboard_container"),
        color = Color(0xFF0F131C),
        tonalElevation = 8.dp,
        border = BorderStroke(1.dp, Color(0xFF1E2433))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 7.dp, vertical = 7.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KeyboardTab.entries.forEach { tab ->
                    val isSelected = tab == selectedTab
                    val tabBg by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFF252D3E) else Color(0xFF141822),
                        label = "tabBg"
                    )
                    val tabBorder by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFF404E6B) else Color(0xFF1E2433),
                        label = "tabBorder"
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFFFFFFFF) else Color(0xFF7A869E),
                        label = "tabText"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(tabBg)
                            .border(BorderStroke(1.dp, tabBorder), RoundedCornerShape(10.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onTabSelected(tab)
                            }
                            .testTag("keyboard_tab_${tab.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.label,
                            color = textColor,
                            fontSize = 13.sp,
                            fontFamily = JetBrainsMono,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RepeatActionButton(
                    onAction = onCursorLeft,
                    modifier = Modifier.weight(1f),
                    testTag = "keyboard_btn_cursor_left"
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Cursor Left",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                }

                RepeatActionButton(
                    onAction = onCursorRight,
                    modifier = Modifier.weight(1f),
                    testTag = "keyboard_btn_cursor_right"
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Cursor Right",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                }

                TouchDownButton(
                    onAction = onNextStep,
                    modifier = Modifier.weight(1f),
                    testTag = "keyboard_btn_next_step",
                    backgroundColor = Color(0xFF1E2638),
                    borderColor = Color(0xFF2E3B56)
                ) {
                    Text(
                        text = "⇥",
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono,
                        fontSize = 16.sp
                    )
                }

                TouchDownButton(
                    onAction = { onKeyClick(SciCalcKey("(", "(", 0, KeyCategory.OPERATOR)) },
                    modifier = Modifier.weight(1f),
                    testTag = "keyboard_btn_paren_open",
                    backgroundColor = Color(0xFF172030),
                    borderColor = Color(0xFF24324D)
                ) {
                    Text(
                        text = "(",
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono,
                        fontSize = 15.sp
                    )
                }

                TouchDownButton(
                    onAction = { onKeyClick(SciCalcKey(")", ")", 0, KeyCategory.OPERATOR)) },
                    modifier = Modifier.weight(1f),
                    testTag = "keyboard_btn_paren_close",
                    backgroundColor = Color(0xFF172030),
                    borderColor = Color(0xFF24324D)
                ) {
                    Text(
                        text = ")",
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono,
                        fontSize = 15.sp
                    )
                }

                TouchDownButton(
                    onAction = onClear,
                    modifier = Modifier.weight(1.1f),
                    testTag = "keyboard_btn_clear",
                    backgroundColor = Color(0xFF2C1620),
                    borderColor = Color(0xFF4E2032)
                ) {
                    Text(
                        text = "AC",
                        color = Color(0xFFFF4D6D),
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono,
                        fontSize = 13.sp
                    )
                }

                RepeatActionButton(
                    onAction = onBackspace,
                    modifier = Modifier.weight(1.1f),
                    testTag = "keyboard_btn_backspace",
                    backgroundColor = Color(0xFF282015),
                    borderColor = Color(0xFF47361C)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            val currentRows = when (selectedTab) {
                KeyboardTab.BASIC -> KeyboardLayouts.basicRows
                KeyboardTab.FUNCTIONS -> KeyboardLayouts.functionRows
                KeyboardTab.CALCULUS -> KeyboardLayouts.calculusRows
                KeyboardTab.GREEK -> KeyboardLayouts.greekRows
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                currentRows.forEach { rowKeys ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        rowKeys.forEach { key ->
                            KeyButton(
                                key = key,
                                onAction = { onKeyClick(key) },
                                onSelectAlternate = { altKey ->
                                    onSelectAlternate(key, altKey)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TouchDownButton(
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String,
    backgroundColor: Color = Color(0xFF1A202E),
    borderColor: Color = Color(0xFF29334A),
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }

    val bg = if (isPressed) backgroundColor.copy(alpha = 0.6f) else backgroundColor

    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(8.dp))
            .pointerInput(onAction) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    isPressed = true
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onAction()
                    waitForUpOrCancellation()
                    isPressed = false
                }
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun RepeatActionButton(
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String,
    backgroundColor: Color = Color(0xFF1A202E),
    borderColor: Color = Color(0xFF29334A),
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }

    val bg = if (isPressed) backgroundColor.copy(alpha = 0.6f) else backgroundColor

    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(8.dp))
            .pointerInput(onAction) {
                coroutineScope {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        isPressed = true
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onAction()

                        val repeatJob = launch {
                            delay(380L)
                            while (down.pressed) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onAction()
                                delay(45L)
                            }
                        }

                        waitForUpOrCancellation()
                        repeatJob.cancel()
                        isPressed = false
                    }
                }
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun KeyButton(
    key: SciCalcKey,
    onAction: () -> Unit,
    onSelectAlternate: (SciCalcKey) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    var isPopupOpen by remember { mutableStateOf(false) }
    var hoveredIndex by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    val (keyBg, borderColor, textColor) = when (key.category) {
        KeyCategory.NUMBER -> Triple(Color(0xFF202738), Color(0xFF303B52), Color(0xFFF8FAFC))
        KeyCategory.OPERATOR -> Triple(Color(0xFF172030), Color(0xFF26334D), Color(0xFF38BDF8))
        KeyCategory.VARIABLE -> Triple(Color(0xFF192338), Color(0xFF2A3A5E), Color(0xFF818CF8))
        KeyCategory.FUNCTION -> Triple(Color(0xFF181E2E), Color(0xFF27324B), Color(0xFFC084FC))
        KeyCategory.ACTION -> Triple(Color(0xFFE11D48), Color(0xFFFB7185), Color(0xFFFFFFFF))
    }

    val displayBg = if (isPressed) keyBg.copy(alpha = 0.5f) else keyBg
    val isItalic = key.category == KeyCategory.VARIABLE && key.label.length == 1 && (key.label[0].isLetter() || key.label[0] in "αβγδεθλμπρστφωΔ")

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(displayBg)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(10.dp))
            .pointerInput(key) {
                coroutineScope {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        isPressed = true
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

                        onAction()

                        if (key.alternates.isNotEmpty()) {
                            val pointerId = down.id
                            var popupActive = false

                            val holdJob = launch {
                                delay(250L)
                                popupActive = true
                                isPopupOpen = true
                                hoveredIndex = 0
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }

                            val itemWidthPx = with(density) { 44.dp.toPx() }
                            val maxDeltaY = with(density) { 90.dp.toPx() }

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.find { it.id == pointerId } ?: break

                                if (popupActive) {
                                    val deltaX = change.position.x - down.position.x
                                    val deltaY = change.position.y - down.position.y

                                    val calculatedIdx = (deltaX / itemWidthPx).toInt().coerceIn(0, key.alternates.lastIndex)
                                    if (calculatedIdx != hoveredIndex) {
                                        hoveredIndex = calculatedIdx
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }

                                    if (change.changedToUp()) {
                                        change.consume()
                                        holdJob.cancel()
                                        isPopupOpen = false
                                        isPressed = false
                                        if (deltaY > -maxDeltaY * 2 && deltaY < maxDeltaY) {
                                            onSelectAlternate(key.alternates[hoveredIndex])
                                        }
                                        break
                                    }
                                } else {
                                    if (change.changedToUp()) {
                                        change.consume()
                                        holdJob.cancel()
                                        isPressed = false
                                        break
                                    }
                                }
                            }
                        } else {
                            waitForUpOrCancellation()
                            isPressed = false
                        }
                    }
                }
            }
            .testTag("keyboard_key_${key.label}"),
        contentAlignment = Alignment.Center
    ) {
        when (key.label) {
            "a/b" -> FractionKeyIcon(color = textColor)
            "√x", "√" -> SquareRootKeyIcon(color = textColor)
            "xⁿ" -> PowerEmptyKeyIcon(color = textColor)
            "x²" -> PowerSquaredKeyIcon(color = textColor)
            "|x|" -> AbsValKeyIcon(color = textColor)
            "d/dx" -> DerivativeKeyIcon(color = textColor)
            "lim" -> LimitKeyIcon(color = textColor)
            else -> {
                Text(
                    text = key.label,
                    color = textColor,
                    fontSize = when {
                        key.category == KeyCategory.ACTION -> 22.sp
                        key.category == KeyCategory.NUMBER -> 20.sp
                        key.label.length >= 4 -> 12.sp
                        key.label.length == 3 -> 13.sp
                        else -> 17.sp
                    },
                    fontWeight = when (key.category) {
                        KeyCategory.ACTION -> FontWeight.Bold
                        KeyCategory.NUMBER -> FontWeight.SemiBold
                        else -> FontWeight.SemiBold
                    },
                    fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                    fontFamily = if (isItalic) FontFamily.Serif else JetBrainsMono,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (key.hasAlternate || key.alternates.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 4.dp, bottom = 4.dp)
                    .size(3.dp)
                    .background(Color(0xFF64748B), CircleShape)
            )
        }

        if (isPopupOpen && key.alternates.isNotEmpty()) {
            val offsetYPx = with(density) { (-54).dp.roundToPx() }

            Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(0, offsetYPx),
                properties = PopupProperties(focusable = false)
            ) {
                KeyPopupSelector(
                    alternates = key.alternates,
                    hoveredIndex = hoveredIndex
                )
            }
        }
    }
}

@Composable
private fun KeyPopupSelector(
    alternates: List<SciCalcKey>,
    hoveredIndex: Int
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E2638),
        border = BorderStroke(1.5.dp, Color(0xFF38BDF8)),
        shadowElevation = 14.dp
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            alternates.forEachIndexed { idx, alt ->
                val isHovered = idx == hoveredIndex
                val bg = if (isHovered) Color(0xFF0284C7) else Color(0xFF28344E)
                val border = if (isHovered) BorderStroke(1.5.dp, Color.White) else BorderStroke(1.dp, Color(0xFF334155))

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bg)
                        .border(border, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = alt.label,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono
                    )
                }
            }
        }
    }
}

@Composable
private fun FractionKeyIcon(
    color: Color,
    modifier: Modifier = Modifier.size(24.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 2.dp.toPx()

        drawLine(
            color = color,
            start = Offset(w * 0.15f, h * 0.5f),
            end = Offset(w * 0.85f, h * 0.5f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.32f, h * 0.10f),
            size = androidx.compose.ui.geometry.Size(w * 0.36f, h * 0.30f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx())
        )

        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.32f, h * 0.60f),
            size = androidx.compose.ui.geometry.Size(w * 0.36f, h * 0.30f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}

@Composable
private fun SquareRootKeyIcon(
    color: Color,
    modifier: Modifier = Modifier.size(24.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 2.dp.toPx()

        val sqrtPath = Path().apply {
            moveTo(w * 0.08f, h * 0.58f)
            lineTo(w * 0.22f, h * 0.78f)
            lineTo(w * 0.38f, h * 0.16f)
            lineTo(w * 0.92f, h * 0.16f)
            lineTo(w * 0.92f, h * 0.28f)
        }

        drawPath(
            path = sqrtPath,
            color = color,
            style = Stroke(
                width = stroke,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.44f, h * 0.32f),
            size = androidx.compose.ui.geometry.Size(w * 0.38f, h * 0.44f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}

@Composable
private fun PowerEmptyKeyIcon(
    color: Color,
    modifier: Modifier = Modifier.size(24.dp)
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(13.dp)
                .border(BorderStroke(1.5.dp, color), RoundedCornerShape(2.5.dp))
        )
        Spacer(modifier = Modifier.width(2.dp))
        Box(
            modifier = Modifier
                .padding(bottom = 10.dp)
                .size(9.dp)
                .border(BorderStroke(1.5.dp, color), RoundedCornerShape(2.dp))
        )
    }
}

@Composable
private fun PowerSquaredKeyIcon(
    color: Color,
    modifier: Modifier = Modifier.size(24.dp)
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "x",
            color = color,
            fontSize = 17.sp,
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "²",
            color = color,
            fontSize = 14.sp,
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
    }
}

@Composable
private fun AbsValKeyIcon(
    color: Color,
    modifier: Modifier = Modifier.size(24.dp)
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .width(1.8.dp)
                .height(18.dp)
                .background(color, RoundedCornerShape(1.dp))
        )
        Spacer(modifier = Modifier.width(3.dp))
        Box(
            modifier = Modifier
                .size(11.dp)
                .border(BorderStroke(1.5.dp, color), RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(3.dp))
        Box(
            modifier = Modifier
                .width(1.8.dp)
                .height(18.dp)
                .background(color, RoundedCornerShape(1.dp))
        )
    }
}

@Composable
private fun DerivativeKeyIcon(
    color: Color,
    modifier: Modifier = Modifier.size(24.dp)
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "d",
            color = color,
            fontSize = 10.sp,
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Bold,
            lineHeight = 10.sp
        )
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(1.5.dp)
                .background(color, RoundedCornerShape(1.dp))
        )
        Text(
            text = "dx",
            color = color,
            fontSize = 10.sp,
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
            lineHeight = 10.sp
        )
    }
}

@Composable
private fun LimitKeyIcon(
    color: Color,
    modifier: Modifier = Modifier.size(24.dp)
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "lim",
            color = color,
            fontSize = 11.sp,
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Bold,
            lineHeight = 11.sp
        )
        Text(
            text = "x→0",
            color = color,
            fontSize = 8.sp,
            fontFamily = JetBrainsMono,
            lineHeight = 8.sp
        )
    }
}
