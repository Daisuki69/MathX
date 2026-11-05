package com.example.ui.math

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.JetBrainsMono

sealed class MathNode {
    data class Text(val content: String, val charIndex: Int = 0, val isVariable: Boolean = false, val isOperator: Boolean = false) : MathNode()
    data class Fraction(val numerator: List<MathNode>, val denominator: List<MathNode>, val numIndex: Int = 0, val denIndex: Int = 0) : MathNode()
    data class Sqrt(val inner: List<MathNode>, val startIndex: Int = 0) : MathNode()
    data class Power(val base: List<MathNode>, val exponent: List<MathNode>, val expIndex: Int = 0) : MathNode()
    data class AbsVal(val inner: List<MathNode>, val startIndex: Int = 0) : MathNode()
    data class Paren(val inner: List<MathNode>, val startIndex: Int = 0) : MathNode()
    data class Func(val name: String, val arg: List<MathNode>, val argIndex: Int = 0) : MathNode()
    data class Placeholder(val slotIndex: Int, val isActive: Boolean) : MathNode()
    object Cursor : MathNode()
}

object MathParser {
    fun parse(raw: String, cursorIndex: Int = -1): List<MathNode> {
        val sanitized = raw.replace("==", "=").replace("*", "·")
        val withCursor = if (cursorIndex in 0..sanitized.length) {
            sanitized.substring(0, cursorIndex) + "§" + sanitized.substring(cursorIndex)
        } else {
            sanitized
        }

        return parseExpression(withCursor, 0, cursorIndex)
    }

    private fun parseExpression(input: String, baseOffset: Int, cursorIndex: Int): List<MathNode> {
        val nodes = mutableListOf<MathNode>()
        var i = 0

        while (i < input.length) {
            val c = input[i]
            val currentIdx = baseOffset + i

            when {
                c == '§' -> {
                    nodes.add(MathNode.Cursor)
                    i++
                }
                c == '√' -> {
                    val rootIdx = currentIdx
                    i++
                    if (i < input.length && input[i] == '(') {
                        val close = findMatchingParen(input, i)
                        val innerStr = input.substring(i + 1, close)
                        val innerNodes = parseExpression(innerStr, baseOffset + i + 1, cursorIndex)
                        val finalInner = if (innerNodes.isEmpty() || (innerNodes.size == 1 && innerNodes[0] is MathNode.Cursor)) {
                            listOf(MathNode.Placeholder(baseOffset + i + 1, innerNodes.isNotEmpty() && innerNodes[0] is MathNode.Cursor))
                        } else {
                            innerNodes
                        }
                        nodes.add(MathNode.Sqrt(finalInner, rootIdx))
                        i = close + 1
                    } else {
                        var end = i
                        while (end < input.length && (input[end].isLetterOrDigit() || input[end] == '§')) {
                            end++
                        }
                        val innerStr = if (end > i) input.substring(i, end) else ""
                        val innerNodes = parseExpression(innerStr, baseOffset + i, cursorIndex)
                        val finalInner = if (innerNodes.isEmpty() || (innerNodes.size == 1 && innerNodes[0] is MathNode.Cursor)) {
                            listOf(MathNode.Placeholder(baseOffset + i, innerNodes.isNotEmpty() && innerNodes[0] is MathNode.Cursor))
                        } else {
                            innerNodes
                        }
                        nodes.add(MathNode.Sqrt(finalInner, rootIdx))
                        i = end
                    }
                }
                c == '|' -> {
                    val barStart = currentIdx
                    val nextBar = input.indexOf('|', i + 1)
                    if (nextBar != -1) {
                        val innerStr = input.substring(i + 1, nextBar)
                        val innerNodes = parseExpression(innerStr, baseOffset + i + 1, cursorIndex)
                        val finalInner = if (innerNodes.isEmpty() || (innerNodes.size == 1 && innerNodes[0] is MathNode.Cursor)) {
                            listOf(MathNode.Placeholder(baseOffset + i + 1, innerNodes.isNotEmpty() && innerNodes[0] is MathNode.Cursor))
                        } else {
                            innerNodes
                        }
                        nodes.add(MathNode.AbsVal(finalInner, barStart))
                        i = nextBar + 1
                    } else {
                        nodes.add(MathNode.Text("|", barStart, isOperator = true))
                        i++
                    }
                }
                input.startsWith("sin⁻¹(", i) || input.startsWith("cos⁻¹(", i) || input.startsWith("tan⁻¹(", i) -> {
                    val fnName = input.substring(i, i + 5)
                    val parenStart = i + 5
                    val close = findMatchingParen(input, parenStart)
                    val innerStr = input.substring(parenStart + 1, close)
                    val innerNodes = parseExpression(innerStr, baseOffset + parenStart + 1, cursorIndex)
                    val finalInner = if (innerNodes.isEmpty() || (innerNodes.size == 1 && innerNodes[0] is MathNode.Cursor)) {
                        listOf(MathNode.Placeholder(baseOffset + parenStart + 1, innerNodes.isNotEmpty() && innerNodes[0] is MathNode.Cursor))
                    } else {
                        innerNodes
                    }
                    nodes.add(MathNode.Func(fnName, finalInner, baseOffset + parenStart + 1))
                    i = close + 1
                }
                input.startsWith("sin(", i) || input.startsWith("cos(", i) || input.startsWith("tan(", i) ||
                input.startsWith("ln(", i) || input.startsWith("log(", i) || input.startsWith("exp(", i) -> {
                    val parenStart = input.indexOf('(', i)
                    val fnName = input.substring(i, parenStart)
                    val close = findMatchingParen(input, parenStart)
                    val innerStr = input.substring(parenStart + 1, close)
                    val innerNodes = parseExpression(innerStr, baseOffset + parenStart + 1, cursorIndex)
                    val finalInner = if (innerNodes.isEmpty() || (innerNodes.size == 1 && innerNodes[0] is MathNode.Cursor)) {
                        listOf(MathNode.Placeholder(baseOffset + parenStart + 1, innerNodes.isNotEmpty() && innerNodes[0] is MathNode.Cursor))
                    } else {
                        innerNodes
                    }
                    nodes.add(MathNode.Func(fnName, finalInner, baseOffset + parenStart + 1))
                    i = close + 1
                }
                c == '/' -> {
                    val numIndex = if (nodes.isNotEmpty()) currentIdx - 1 else currentIdx
                    val prev = if (nodes.isNotEmpty()) nodes.removeAt(nodes.size - 1) else MathNode.Placeholder(numIndex, false)
                    i++
                    if (i < input.length && input[i] == '(') {
                        val close = findMatchingParen(input, i)
                        val denStr = input.substring(i + 1, close)
                        val denNodes = parseExpression(denStr, baseOffset + i + 1, cursorIndex)
                        val finalDen = if (denNodes.isEmpty() || (denNodes.size == 1 && denNodes[0] is MathNode.Cursor)) {
                            listOf(MathNode.Placeholder(baseOffset + i + 1, denNodes.isNotEmpty() && denNodes[0] is MathNode.Cursor))
                        } else {
                            denNodes
                        }
                        nodes.add(MathNode.Fraction(listOf(prev), finalDen, numIndex, baseOffset + i + 1))
                        i = close + 1
                    } else {
                        var end = i
                        while (end < input.length && (input[end].isLetterOrDigit() || input[end] == '§')) {
                            end++
                        }
                        val denStr = if (end > i) input.substring(i, end) else ""
                        val denNodes = parseExpression(denStr, baseOffset + i, cursorIndex)
                        val finalDen = if (denNodes.isEmpty() || (denNodes.size == 1 && denNodes[0] is MathNode.Cursor)) {
                            listOf(MathNode.Placeholder(baseOffset + i, denNodes.isNotEmpty() && denNodes[0] is MathNode.Cursor))
                        } else {
                            denNodes
                        }
                        nodes.add(MathNode.Fraction(listOf(prev), finalDen, numIndex, baseOffset + i))
                        i = end
                    }
                }
                c == '^' -> {
                    val expBaseIndex = if (nodes.isNotEmpty()) currentIdx - 1 else currentIdx
                    val prev = if (nodes.isNotEmpty()) nodes.removeAt(nodes.size - 1) else MathNode.Text("x", expBaseIndex, isVariable = true)
                    i++
                    if (i < input.length && input[i] == '(') {
                        val close = findMatchingParen(input, i)
                        val expStr = input.substring(i + 1, close)
                        val expNodes = parseExpression(expStr, baseOffset + i + 1, cursorIndex)
                        val finalExp = if (expNodes.isEmpty() || (expNodes.size == 1 && expNodes[0] is MathNode.Cursor)) {
                            listOf(MathNode.Placeholder(baseOffset + i + 1, expNodes.isNotEmpty() && expNodes[0] is MathNode.Cursor))
                        } else {
                            expNodes
                        }
                        nodes.add(MathNode.Power(listOf(prev), finalExp, baseOffset + i + 1))
                        i = close + 1
                    } else {
                        var end = i
                        while (end < input.length && (input[end].isLetterOrDigit() || input[end] == '§')) {
                            end++
                        }
                        val expStr = if (end > i) input.substring(i, end) else ""
                        val expNodes = parseExpression(expStr, baseOffset + i, cursorIndex)
                        val finalExp = if (expNodes.isEmpty() || (expNodes.size == 1 && expNodes[0] is MathNode.Cursor)) {
                            listOf(MathNode.Placeholder(baseOffset + i, expNodes.isNotEmpty() && expNodes[0] is MathNode.Cursor))
                        } else {
                            expNodes
                        }
                        nodes.add(MathNode.Power(listOf(prev), finalExp, baseOffset + i))
                        i = end
                    }
                }
                c in "²³⁴⁵⁶⁷⁸⁹⁰¹ⁿˣʸ" -> {
                    val prev = if (nodes.isNotEmpty()) nodes.removeAt(nodes.size - 1) else MathNode.Text("x", currentIdx, isVariable = true)
                    val normalExp = when (c) {
                        '²' -> "2"
                        '³' -> "3"
                        '⁴' -> "4"
                        '⁵' -> "5"
                        '⁶' -> "6"
                        '⁷' -> "7"
                        '⁸' -> "8"
                        '⁹' -> "9"
                        '⁰' -> "0"
                        '¹' -> "1"
                        'ⁿ' -> "n"
                        'ˣ' -> "x"
                        'ʸ' -> "y"
                        else -> c.toString()
                    }
                    nodes.add(MathNode.Power(listOf(prev), listOf(MathNode.Text(normalExp, currentIdx))))
                    i++
                }
                c == '(' -> {
                    val close = findMatchingParen(input, i)
                    val innerStr = input.substring(i + 1, close)
                    val innerNodes = parseExpression(innerStr, baseOffset + i + 1, cursorIndex)
                    nodes.add(MathNode.Paren(innerNodes, currentIdx))
                    i = close + 1
                }
                c == '+' || c == '−' || c == '-' || c == '·' || c == '×' || c == '÷' || c == '=' || c == '<' || c == '>' || c == '≤' || c == '≥' || c == '≠' -> {
                    nodes.add(MathNode.Text(" $c ", currentIdx, isOperator = true))
                    i++
                }
                c.isLetter() -> {
                    nodes.add(MathNode.Text(c.toString(), currentIdx, isVariable = true))
                    i++
                }
                c.isDigit() || c == '.' -> {
                    var end = i
                    while (end < input.length && (input[end].isDigit() || input[end] == '.')) {
                        end++
                    }
                    nodes.add(MathNode.Text(input.substring(i, end), currentIdx))
                    i = end
                }
                c.isWhitespace() -> {
                    i++
                }
                else -> {
                    nodes.add(MathNode.Text(c.toString(), currentIdx))
                    i++
                }
            }
        }

        return nodes
    }

    private fun findMatchingParen(str: String, startIdx: Int): Int {
        var depth = 0
        for (idx in startIdx until str.length) {
            if (str[idx] == '(') depth++
            if (str[idx] == ')') {
                depth--
                if (depth == 0) return idx
            }
        }
        return str.length
    }
}

@Composable
fun MathFormulaView(
    formula: String,
    modifier: Modifier = Modifier,
    cursorIndex: Int = -1,
    fontSize: TextUnit = 22.sp,
    color: Color = Color.White,
    onSlotClick: (Int) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val nodes = remember(formula, cursorIndex) { MathParser.parse(formula, cursorIndex) }

    Row(
        modifier = modifier.horizontalScroll(scrollState),
        verticalAlignment = Alignment.CenterVertically
    ) {
        nodes.forEach { node ->
            MathNodeRenderer(node = node, fontSize = fontSize, color = color, onSlotClick = onSlotClick)
        }
    }
}

@Composable
fun MathNodeRenderer(
    node: MathNode,
    fontSize: TextUnit,
    color: Color,
    onSlotClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    when (node) {
        is MathNode.Text -> {
            Text(
                text = node.content,
                color = if (node.isVariable) AccentIndigo else if (node.isOperator) AccentCyan else color,
                fontSize = fontSize,
                fontFamily = JetBrainsMono,
                fontWeight = if (node.isOperator) FontWeight.Medium else FontWeight.SemiBold,
                fontStyle = if (node.isVariable) FontStyle.Italic else FontStyle.Normal,
                modifier = modifier.clickable { onSlotClick(node.charIndex) }
            )
        }
        is MathNode.Placeholder -> {
            PlaceholderSlot(
                isActive = node.isActive,
                onClick = { onSlotClick(node.slotIndex) },
                modifier = modifier
            )
        }
        is MathNode.Cursor -> {
            val transition = rememberInfiniteTransition(label = "cursor")
            val alpha by transition.animateFloat(
                initialValue = 1f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(450),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "cursorAlpha"
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 1.dp)
                    .width(2.dp)
                    .height(24.dp)
                    .alpha(alpha)
                    .background(AccentCyan, RoundedCornerShape(1.dp))
            )
        }
        is MathNode.Fraction -> {
            Column(
                modifier = modifier.padding(horizontal = 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    node.numerator.forEach { MathNodeRenderer(it, fontSize * 0.85f, color, onSlotClick) }
                }
                Box(
                    modifier = Modifier
                        .widthIn(min = 18.dp)
                        .height(1.8.dp)
                        .background(color, RoundedCornerShape(1.dp))
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    node.denominator.forEach { MathNodeRenderer(it, fontSize * 0.85f, color, onSlotClick) }
                }
            }
        }
        is MathNode.Sqrt -> {
            Row(
                modifier = modifier
                    .drawBehind {
                        val w = size.width
                        val h = size.height
                        val stroke = 1.8.dp.toPx()

                        val path = Path().apply {
                            moveTo(0f, h * 0.65f)
                            lineTo(w * 0.08f, h * 0.88f)
                            lineTo(w * 0.18f, h * 0.10f)
                            lineTo(w, h * 0.10f)
                        }

                        drawPath(
                            path = path,
                            color = color,
                            style = Stroke(
                                width = stroke,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                    .padding(start = 12.dp, top = 5.dp, end = 3.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                node.inner.forEach { MathNodeRenderer(it, fontSize, color, onSlotClick) }
            }
        }
        is MathNode.Power -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.Top
            ) {
                Box(modifier = Modifier.padding(top = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        node.base.forEach { MathNodeRenderer(it, fontSize, color, onSlotClick) }
                    }
                }
                Box(modifier = Modifier.padding(start = 1.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        node.exponent.forEach { MathNodeRenderer(it, fontSize * 0.72f, color, onSlotClick) }
                    }
                }
            }
        }
        is MathNode.AbsVal -> {
            Row(
                modifier = modifier
                    .height(IntrinsicSize.Min)
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(color, RoundedCornerShape(1.dp))
                )
                Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        node.inner.forEach { MathNodeRenderer(it, fontSize, color, onSlotClick) }
                    }
                }
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(color, RoundedCornerShape(1.dp))
                )
            }
        }
        is MathNode.Paren -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("(", color = color, fontSize = fontSize, fontFamily = JetBrainsMono)
                node.inner.forEach { MathNodeRenderer(it, fontSize, color, onSlotClick) }
                Text(")", color = color, fontSize = fontSize, fontFamily = JetBrainsMono)
            }
        }
        is MathNode.Func -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = node.name,
                    color = AccentCyan,
                    fontSize = fontSize,
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.SemiBold
                )
                Text("(", color = color, fontSize = fontSize, fontFamily = JetBrainsMono)
                node.arg.forEach { MathNodeRenderer(it, fontSize, color, onSlotClick) }
                Text(")", color = color, fontSize = fontSize, fontFamily = JetBrainsMono)
            }
        }
    }
}

@Composable
fun PlaceholderSlot(
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "slot")
    val glowColor by transition.animateColor(
        initialValue = AccentCyan,
        targetValue = AccentCyan.copy(alpha = 0.35f),
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "slotGlow"
    )

    Box(
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .widthIn(min = 18.dp)
            .height(20.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (isActive) Color(0xFF142438) else Color(0xFF161C28))
            .border(
                BorderStroke(
                    width = if (isActive) 1.8.dp else 1.dp,
                    color = if (isActive) glowColor else Color(0xFF38435C)
                ),
                RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isActive) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(13.dp)
                    .background(AccentCyan, RoundedCornerShape(1.dp))
            )
        }
    }
}
