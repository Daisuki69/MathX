package com.example.ui.keyboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMono

@Composable
fun SciCalcKeyboard(
    selectedTab: KeyboardTab,
    onTabSelected: (KeyboardTab) -> Unit,
    onKeyClick: (SciCalcKey) -> Unit,
    onKeyLongClick: (SciCalcKey) -> Unit = {},
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
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1A202E))
                        .border(BorderStroke(1.dp, Color(0xFF29334A)), RoundedCornerShape(8.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onCursorLeft()
                        }
                        .testTag("keyboard_btn_cursor_left"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Cursor Left",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1A202E))
                        .border(BorderStroke(1.dp, Color(0xFF29334A)), RoundedCornerShape(8.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onCursorRight()
                        }
                        .testTag("keyboard_btn_cursor_right"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Cursor Right",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E2638))
                        .border(BorderStroke(1.dp, Color(0xFF2E3B56)), RoundedCornerShape(8.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNextStep()
                        }
                        .testTag("keyboard_btn_next_step"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⇥",
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono,
                        fontSize = 16.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF172030))
                        .border(BorderStroke(1.dp, Color(0xFF24324D)), RoundedCornerShape(8.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onKeyClick(SciCalcKey("(", "(", 0, KeyCategory.OPERATOR))
                        }
                        .testTag("keyboard_btn_paren_open"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "(",
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono,
                        fontSize = 15.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF172030))
                        .border(BorderStroke(1.dp, Color(0xFF24324D)), RoundedCornerShape(8.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onKeyClick(SciCalcKey(")", ")", 0, KeyCategory.OPERATOR))
                        }
                        .testTag("keyboard_btn_paren_close"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ")",
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono,
                        fontSize = 15.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2C1620))
                        .border(BorderStroke(1.dp, Color(0xFF4E2032)), RoundedCornerShape(8.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onClear()
                        }
                        .testTag("keyboard_btn_clear"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AC",
                        color = Color(0xFFFF4D6D),
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF282015))
                        .border(BorderStroke(1.dp, Color(0xFF47361C)), RoundedCornerShape(8.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onBackspace()
                        }
                        .testTag("keyboard_btn_backspace"),
                    contentAlignment = Alignment.Center
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
                                onClick = { onKeyClick(key) },
                                onLongClick = { onKeyLongClick(key) },
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
        Text(
            text = "x",
            color = color,
            fontSize = 17.sp,
            fontFamily = JetBrainsMono,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.width(2.dp))
        Box(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .size(11.dp)
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
            fontFamily = JetBrainsMono,
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
            fontFamily = JetBrainsMono,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun KeyButton(
    key: SciCalcKey,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val (keyBg, borderColor, textColor) = when (key.category) {
        KeyCategory.NUMBER -> Triple(
            Color(0xFF202738),
            Color(0xFF303B52),
            Color(0xFFF8FAFC)
        )
        KeyCategory.OPERATOR -> Triple(
            Color(0xFF172030),
            Color(0xFF26334D),
            Color(0xFF38BDF8)
        )
        KeyCategory.VARIABLE -> Triple(
            Color(0xFF192338),
            Color(0xFF2A3A5E),
            Color(0xFF818CF8)
        )
        KeyCategory.FUNCTION -> Triple(
            Color(0xFF181E2E),
            Color(0xFF27324B),
            Color(0xFFC084FC)
        )
        KeyCategory.ACTION -> Triple(
            Color(0xFFE11D48),
            Color(0xFFFB7185),
            Color(0xFFFFFFFF)
        )
    }

    val isItalic = key.category == KeyCategory.VARIABLE && key.label.length == 1 && key.label[0].isLetter()

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(keyBg)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(10.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(color = textColor),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
                onLongClick = {
                    if (key.hasAlternate) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()
                    }
                }
            )
            .testTag("keyboard_key_${key.label}"),
        contentAlignment = Alignment.Center
    ) {
        when (key.label) {
            "a/b" -> FractionKeyIcon(color = textColor)
            "√x" -> SquareRootKeyIcon(color = textColor)
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
                    fontFamily = JetBrainsMono,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (key.hasAlternate) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 4.dp, bottom = 4.dp)
                    .size(3.dp)
                    .background(Color(0xFF64748B), CircleShape)
            )
        }
    }
}
