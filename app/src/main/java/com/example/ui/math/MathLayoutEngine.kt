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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.JetBrainsMono

sealed class MathAstNode {
    abstract val start: Int
    abstract val end: Int

    data class TextToken(
        val text: String,
        override val start: Int,
        override val end: Int,
        val isVariable: Boolean = false,
        val isOperator: Boolean = false
    ) : MathAstNode()

    data class FractionNode(
        val numerator: List<MathAstNode>,
        val denominator: List<MathAstNode>,
        val numRange: IntRange,
        val denRange: IntRange,
        override val start: Int,
        override val end: Int
    ) : MathAstNode()

    data class SqrtNode(
        val inner: List<MathAstNode>,
        val innerRange: IntRange,
        val rootDegree: String? = null,
        override val start: Int,
        override val end: Int
    ) : MathAstNode()

    data class PowerNode(
        val base: List<MathAstNode>,
        val exponent: List<MathAstNode>,
        val baseRange: IntRange,
        val expRange: IntRange,
        override val start: Int,
        override val end: Int
    ) : MathAstNode()

    data class AbsNode(
        val inner: List<MathAstNode>,
        val innerRange: IntRange,
        override val start: Int,
        override val end: Int
    ) : MathAstNode()

    data class ParenNode(
        val inner: List<MathAstNode>,
        override val start: Int,
        override val end: Int
    ) : MathAstNode()

    data class FunctionNode(
        val name: String,
        val arg: List<MathAstNode>,
        val argRange: IntRange,
        override val start: Int,
        override val end: Int
    ) : MathAstNode()

    data class CalculusNode(
        val opType: String,
        val arg: List<MathAstNode>,
        override val start: Int,
        override val end: Int
    ) : MathAstNode()

    data class EmptyPlaceholder(
        val slotIndex: Int,
        override val start: Int,
        override val end: Int
    ) : MathAstNode()
}

object AstParser {
    private val KNOWN_FUNCS = listOf(
        "arcsin", "arccos", "arctan",
        "sin⁻¹", "cos⁻¹", "tan⁻¹",
        "sinh", "cosh", "tanh",
        "cbrt", "root",
        "asin", "acos", "atan",
        "sin", "cos", "tan", "sec", "csc",
        "log2", "log", "ln", "exp",
        "integrate", "d/dx", "lim", "sum", "mod",
        "abs", "sqrt"
    )

    fun parse(raw: String): List<MathAstNode> {
        return parseRange(raw, 0, raw.length)
    }

    private fun parseRange(raw: String, from: Int, to: Int): List<MathAstNode> {
        val nodes = mutableListOf<MathAstNode>()
        var i = from

        while (i < to) {
            val c = raw[i]

            when {
                raw.startsWith("==", i) -> {
                    nodes.add(MathAstNode.TextToken("=", i, i + 2, isOperator = true))
                    i += 2
                }
                raw.startsWith("!=", i) -> {
                    nodes.add(MathAstNode.TextToken("≠", i, i + 2, isOperator = true))
                    i += 2
                }
                raw.startsWith("<=", i) -> {
                    nodes.add(MathAstNode.TextToken("≤", i, i + 2, isOperator = true))
                    i += 2
                }
                raw.startsWith(">=", i) -> {
                    nodes.add(MathAstNode.TextToken("≥", i, i + 2, isOperator = true))
                    i += 2
                }
                raw.startsWith("oo", i) && (i + 2 == to || !raw[i + 2].isLetter()) -> {
                    nodes.add(MathAstNode.TextToken("∞", i, i + 2, isVariable = true))
                    i += 2
                }
                c == '√' -> {
                    val rootStart = i
                    i++
                    if (i < to && raw[i] == '(') {
                        val close = findMatchingParen(raw, i, to)
                        val inner = parseRange(raw, i + 1, close)
                        nodes.add(MathAstNode.SqrtNode(inner, (i + 1)..close, null, rootStart, close + 1))
                        i = close + 1
                    } else {
                        var end = i
                        while (end < to && raw[end].isLetterOrDigit()) end++
                        val inner = if (end > i) parseRange(raw, i, end) else emptyList()
                        nodes.add(MathAstNode.SqrtNode(inner, i..end, null, rootStart, end))
                        i = end
                    }
                }
                c == '|' -> {
                    val close = raw.indexOf('|', i + 1)
                    if (close in (i + 1) until to) {
                        val inner = parseRange(raw, i + 1, close)
                        nodes.add(MathAstNode.AbsNode(inner, (i + 1)..close, i, close + 1))
                        i = close + 1
                    } else {
                        nodes.add(MathAstNode.TextToken("|", i, i + 1, isOperator = true))
                        i++
                    }
                }
                c == '/' -> {
                    val last = if (nodes.isNotEmpty()) nodes.removeAt(nodes.size - 1) else null
                    val numNodes = if (last is MathAstNode.ParenNode) last.inner else if (last != null) listOf(last) else emptyList()
                    val numRange = if (last is MathAstNode.ParenNode) {
                        (last.start + 1)..(last.end - 1)
                    } else if (last != null) {
                        last.start..last.end
                    } else {
                        i..i
                    }
                    val numStart = last?.start ?: i
                    i++

                    while (i < to && raw[i] == ' ') i++

                    if (i < to && raw[i] == '(') {
                        val close = findMatchingParen(raw, i, to)
                        val den = parseRange(raw, i + 1, close)
                        val denRange = (i + 1)..close
                        nodes.add(MathAstNode.FractionNode(numNodes, den, numRange, denRange, numStart, close + 1))
                        i = close + 1
                    } else {
                        var end = i
                        while (end < to && (raw[end].isLetterOrDigit() || raw[end] in "αβγδεθλμπρστφωΔ")) end++
                        val den = if (end > i) parseRange(raw, i, end) else emptyList()
                        nodes.add(MathAstNode.FractionNode(numNodes, den, numRange, i..end, numStart, end))
                        i = end
                    }
                }
                c == '^' -> {
                    val last = if (nodes.isNotEmpty()) nodes.removeAt(nodes.size - 1) else null
                    val baseNodes = if (last is MathAstNode.ParenNode) last.inner else if (last != null) listOf(last) else emptyList()
                    val baseRange = if (last is MathAstNode.ParenNode) (last.start + 1)..(last.end - 1) else if (last != null) last.start..last.end else i..i
                    val baseStart = last?.start ?: i
                    i++

                    while (i < to && raw[i] == ' ') i++

                    if (i < to && raw[i] == '(') {
                        val close = findMatchingParen(raw, i, to)
                        val exp = parseRange(raw, i + 1, close)
                        val expRange = (i + 1)..close
                        nodes.add(MathAstNode.PowerNode(baseNodes, exp, baseRange, expRange, baseStart, close + 1))
                        i = close + 1
                    } else {
                        var end = i
                        while (end < to && (raw[end].isLetterOrDigit() || raw[end] in "αβγδεθλμπρστφωΔ")) end++
                        val exp = if (end > i) parseRange(raw, i, end) else emptyList()
                        nodes.add(MathAstNode.PowerNode(baseNodes, exp, baseRange, i..end, baseStart, end))
                        i = end
                    }
                }
                c in "²³⁴⁵⁶⁷⁸⁹" -> {
                    val last = if (nodes.isNotEmpty()) nodes.removeAt(nodes.size - 1) else null
                    val baseNodes = if (last is MathAstNode.ParenNode) last.inner else if (last != null) listOf(last) else emptyList()
                    val baseRange = if (last is MathAstNode.ParenNode) (last.start + 1)..(last.end - 1) else if (last != null) last.start..last.end else i..i
                    val baseStart = last?.start ?: i

                    val expVal = when (c) {
                        '²' -> "2"
                        '³' -> "3"
                        '⁴' -> "4"
                        '⁵' -> "5"
                        '⁶' -> "6"
                        '⁷' -> "7"
                        '⁸' -> "8"
                        '⁹' -> "9"
                        else -> c.toString()
                    }
                    val expNode = listOf(MathAstNode.TextToken(expVal, i, i + 1))
                    nodes.add(MathAstNode.PowerNode(baseNodes, expNode, baseRange, i..(i + 1), baseStart, i + 1))
                    i++
                }
                c == '(' -> {
                    val close = findMatchingParen(raw, i, to)
                    val inner = parseRange(raw, i + 1, close)
                    nodes.add(MathAstNode.ParenNode(inner, i, close + 1))
                    i = close + 1
                }
                c.isDigit() -> {
                    var end = i
                    while (end < to && (raw[end].isDigit() || raw[end] == '.')) end++
                    nodes.add(MathAstNode.TextToken(raw.substring(i, end), i, end))
                    i = end
                }
                c == '+' || c == '-' || c == '−' || c == '*' || c == '·' || c == '=' || c == '<' || c == '>' || c == ',' || c == '!' -> {
                    val sym = if (c == '*') "·" else c.toString()
                    nodes.add(MathAstNode.TextToken(sym, i, i + 1, isOperator = true))
                    i++
                }
                c in "αβγδεθλμπρστφωΔ" -> {
                    nodes.add(MathAstNode.TextToken(c.toString(), i, i + 1, isVariable = true))
                    i++
                }
                else -> {
                    val matchedFunc = KNOWN_FUNCS.firstOrNull { raw.startsWith(it, i) && i + it.length < to && raw[i + it.length] == '(' }
                    if (matchedFunc != null) {
                        val fnStart = i
                        val parenStart = i + matchedFunc.length
                        val close = findMatchingParen(raw, parenStart, to)
                        val arg = parseRange(raw, parenStart + 1, close)
                        when (matchedFunc) {
                            "cbrt" -> nodes.add(MathAstNode.SqrtNode(arg, (parenStart + 1)..close, "3", fnStart, close + 1))
                            "root" -> nodes.add(MathAstNode.SqrtNode(arg, (parenStart + 1)..close, "n", fnStart, close + 1))
                            "sqrt" -> nodes.add(MathAstNode.SqrtNode(arg, (parenStart + 1)..close, null, fnStart, close + 1))
                            "abs" -> nodes.add(MathAstNode.AbsNode(arg, (parenStart + 1)..close, fnStart, close + 1))
                            "d/dx", "integrate", "lim", "sum" -> nodes.add(MathAstNode.CalculusNode(matchedFunc, arg, fnStart, close + 1))
                            else -> nodes.add(MathAstNode.FunctionNode(matchedFunc, arg, (parenStart + 1)..close, fnStart, close + 1))
                        }
                        i = close + 1
                    } else if (c.isLetter()) {
                        nodes.add(MathAstNode.TextToken(c.toString(), i, i + 1, isVariable = true))
                        i++
                    } else {
                        if (!c.isWhitespace()) {
                            nodes.add(MathAstNode.TextToken(c.toString(), i, i + 1))
                        }
                        i++
                    }
                }
            }
        }

        return nodes
    }

    private fun findMatchingParen(raw: String, openIdx: Int, maxIdx: Int): Int {
        var depth = 1
        var i = openIdx + 1
        while (i < maxIdx) {
            when (raw[i]) {
                '(' -> depth++
                ')' -> {
                    depth--
                    if (depth == 0) return i
                }
            }
            i++
        }
        return maxIdx
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
    val astNodes = remember(formula) { AstParser.parse(formula) }

    LaunchedEffect(cursorIndex, formula) {
        if (formula.isNotEmpty()) {
            val progress = (cursorIndex.toFloat() / formula.length.coerceAtLeast(1)).coerceIn(0f, 1f)
            scrollState.animateScrollTo((scrollState.maxValue * progress).toInt())
        }
    }

    Row(
        modifier = modifier.horizontalScroll(scrollState),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RenderNodeList(
            nodes = astNodes,
            cursorIndex = cursorIndex,
            parentRange = 0..formula.length,
            fontSize = fontSize,
            color = color,
            onSlotClick = onSlotClick
        )
        if (cursorIndex >= formula.length && formula.isNotEmpty()) {
            PulsingCursorBar(fontSize = fontSize)
        }
    }
}

@Composable
fun RenderNodeList(
    nodes: List<MathAstNode>,
    cursorIndex: Int,
    parentRange: IntRange,
    fontSize: TextUnit,
    color: Color,
    onSlotClick: (Int) -> Unit
) {
    if (nodes.isEmpty()) {
        val slotIdx = parentRange.first
        val isActive = cursorIndex in parentRange
        PlaceholderSlot(
            isActive = isActive,
            fontSize = fontSize,
            onClick = { onSlotClick(slotIdx) }
        )
        return
    }

    nodes.forEachIndexed { index, node ->
        if (cursorIndex == node.start && index == 0 && parentRange.first == node.start) {
            PulsingCursorBar(fontSize = fontSize)
        }

        RenderAstNode(
            node = node,
            cursorIndex = cursorIndex,
            fontSize = fontSize,
            color = color,
            onSlotClick = onSlotClick
        )

        if (cursorIndex == node.end) {
            PulsingCursorBar(fontSize = fontSize)
        }
    }
}

@Composable
fun RenderAstNode(
    node: MathAstNode,
    cursorIndex: Int,
    fontSize: TextUnit,
    color: Color,
    onSlotClick: (Int) -> Unit
) {
    when (node) {
        is MathAstNode.TextToken -> {
            RenderTextToken(
                node = node,
                cursorIndex = cursorIndex,
                fontSize = fontSize,
                color = color,
                onSlotClick = onSlotClick
            )
        }
        is MathAstNode.FractionNode -> {
            Column(
                modifier = Modifier.padding(horizontal = 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    RenderNodeList(
                        nodes = node.numerator,
                        cursorIndex = cursorIndex,
                        parentRange = node.numRange,
                        fontSize = fontSize * 0.85f,
                        color = color,
                        onSlotClick = onSlotClick
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .widthIn(min = 18.dp)
                        .height(1.8.dp)
                        .background(color, RoundedCornerShape(1.dp))
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    RenderNodeList(
                        nodes = node.denominator,
                        cursorIndex = cursorIndex,
                        parentRange = node.denRange,
                        fontSize = fontSize * 0.85f,
                        color = color,
                        onSlotClick = onSlotClick
                    )
                }
            }
        }
        is MathAstNode.SqrtNode -> {
            Row(
                modifier = Modifier
                    .drawBehind {
                        val h = size.height
                        val stroke = 1.8.dp.toPx()
                        val tickW = 10.dp.toPx()

                        val path = Path().apply {
                            moveTo(1.dp.toPx(), h * 0.60f)
                            lineTo(4.dp.toPx(), h * 0.85f)
                            lineTo(tickW, 2.dp.toPx())
                            lineTo(size.width, 2.dp.toPx())
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
                if (node.rootDegree != null) {
                    Text(
                        text = node.rootDegree,
                        color = AccentCyan,
                        fontSize = fontSize * 0.6f,
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp, end = 2.dp)
                    )
                }
                RenderNodeList(
                    nodes = node.inner,
                    cursorIndex = cursorIndex,
                    parentRange = node.innerRange,
                    fontSize = fontSize,
                    color = color,
                    onSlotClick = onSlotClick
                )
            }
        }
        is MathAstNode.PowerNode -> {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.Bottom
            ) {
                Box {
                    if (node.base.isEmpty()) {
                        PlaceholderSlot(
                            isActive = cursorIndex in node.baseRange,
                            fontSize = fontSize,
                            onClick = { onSlotClick(node.baseRange.first) }
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            node.base.forEach { bNode ->
                                RenderAstNode(bNode, cursorIndex, fontSize, color, onSlotClick)
                            }
                        }
                    }
                }
                Box(modifier = Modifier.padding(bottom = (fontSize.value * 0.40f).dp, start = 1.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RenderNodeList(
                            nodes = node.exponent,
                            cursorIndex = cursorIndex,
                            parentRange = node.expRange,
                            fontSize = fontSize * 0.72f,
                            color = color,
                            onSlotClick = onSlotClick
                        )
                    }
                }
            }
        }
        is MathAstNode.AbsNode -> {
            Row(
                modifier = Modifier
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
                    RenderNodeList(
                        nodes = node.inner,
                        cursorIndex = cursorIndex,
                        parentRange = node.innerRange,
                        fontSize = fontSize,
                        color = color,
                        onSlotClick = onSlotClick
                    )
                }
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(color, RoundedCornerShape(1.dp))
                )
            }
        }
        is MathAstNode.ParenNode -> {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("(", color = color, fontSize = fontSize, fontFamily = JetBrainsMono)
                RenderNodeList(
                    nodes = node.inner,
                    cursorIndex = cursorIndex,
                    parentRange = (node.start + 1)..(node.end - 1),
                    fontSize = fontSize,
                    color = color,
                    onSlotClick = onSlotClick
                )
                Text(")", color = color, fontSize = fontSize, fontFamily = JetBrainsMono)
            }
        }
        is MathAstNode.FunctionNode -> {
            Row(
                modifier = Modifier,
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
                RenderNodeList(
                    nodes = node.arg,
                    cursorIndex = cursorIndex,
                    parentRange = node.argRange,
                    fontSize = fontSize,
                    color = color,
                    onSlotClick = onSlotClick
                )
                Text(")", color = color, fontSize = fontSize, fontFamily = JetBrainsMono)
            }
        }
        is MathAstNode.CalculusNode -> {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (node.opType) {
                    "d/dx" -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text("d", color = AccentCyan, fontSize = fontSize * 0.7f, fontFamily = JetBrainsMono, fontWeight = FontWeight.Bold)
                            Box(modifier = Modifier.width(14.dp).height(1.5.dp).background(AccentCyan, RoundedCornerShape(1.dp)))
                            Text("dx", color = AccentIndigo, fontSize = fontSize * 0.7f, fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold)
                        }
                    }
                    "integrate" -> {
                        Text("∫", color = AccentCyan, fontSize = fontSize * 1.3f, fontFamily = JetBrainsMono, modifier = Modifier.padding(end = 2.dp))
                    }
                    "lim" -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 4.dp)) {
                            Text("lim", color = AccentCyan, fontSize = fontSize * 0.8f, fontFamily = JetBrainsMono, fontWeight = FontWeight.Bold)
                            Text("x→0", color = AccentCyan, fontSize = fontSize * 0.5f, fontFamily = JetBrainsMono)
                        }
                    }
                    "sum" -> {
                        Text("∑", color = AccentCyan, fontSize = fontSize * 1.3f, fontFamily = JetBrainsMono, modifier = Modifier.padding(end = 2.dp))
                    }
                }
                Text("(", color = color, fontSize = fontSize, fontFamily = JetBrainsMono)
                RenderNodeList(
                    nodes = node.arg,
                    cursorIndex = cursorIndex,
                    parentRange = (node.start + node.opType.length + 1)..(node.end - 1),
                    fontSize = fontSize,
                    color = color,
                    onSlotClick = onSlotClick
                )
                Text(")", color = color, fontSize = fontSize, fontFamily = JetBrainsMono)
            }
        }
        is MathAstNode.EmptyPlaceholder -> {
            PlaceholderSlot(
                isActive = cursorIndex in node.start..node.end,
                fontSize = fontSize,
                onClick = { onSlotClick(node.slotIndex) }
            )
        }
    }
}

@Composable
fun RenderTextToken(
    node: MathAstNode.TextToken,
    cursorIndex: Int,
    fontSize: TextUnit,
    color: Color,
    onSlotClick: (Int) -> Unit
) {
    val text = node.text
    val tokenColor = if (node.isVariable) AccentIndigo else if (node.isOperator) AccentCyan else color
    val fontFamily = if (node.isVariable) FontFamily.Serif else JetBrainsMono
    val fontStyle = if (node.isVariable) FontStyle.Italic else FontStyle.Normal
    val fontWeight = if (node.isOperator) FontWeight.Medium else FontWeight.SemiBold
    val opPadding = if (node.isOperator && text != "·" && text != "!" && text != ",") 3.dp else 0.dp

    val clickModifier = Modifier
        .padding(horizontal = opPadding)
        .pointerInput(node) {
            awaitEachGesture {
                val down = awaitFirstDown()
                val clickX = down.position.x
                val w = size.width
                val target = if (clickX < w / 2f) node.start else node.end
                onSlotClick(target)
            }
        }

    if (cursorIndex in (node.start + 1) until node.end) {
        val splitOffset = (cursorIndex - node.start).coerceIn(0, text.length)
        val left = text.substring(0, splitOffset)
        val right = text.substring(splitOffset)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = clickModifier
        ) {
            Text(
                text = left,
                color = tokenColor,
                fontSize = fontSize,
                fontFamily = fontFamily,
                fontWeight = fontWeight,
                fontStyle = fontStyle
            )
            PulsingCursorBar(fontSize = fontSize)
            Text(
                text = right,
                color = tokenColor,
                fontSize = fontSize,
                fontFamily = fontFamily,
                fontWeight = fontWeight,
                fontStyle = fontStyle
            )
        }
    } else {
        Text(
            text = text,
            color = tokenColor,
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            modifier = clickModifier
        )
    }
}

@Composable
fun PulsingCursorBar(fontSize: TextUnit) {
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
    val barHeight = fontSize.value.dp * 1.15f

    Box(
        modifier = Modifier
            .padding(horizontal = 0.5.dp)
            .width(2.dp)
            .height(barHeight)
            .alpha(alpha)
            .background(AccentCyan, RoundedCornerShape(1.dp))
    )
}

@Composable
fun PlaceholderSlot(
    isActive: Boolean,
    fontSize: TextUnit,
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

    val slotW = (fontSize.value * 0.85f).dp.coerceAtLeast(16.dp)
    val slotH = (fontSize.value * 0.95f).dp.coerceAtLeast(18.dp)

    Box(
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .width(slotW)
            .height(slotH)
            .clip(RoundedCornerShape(3.dp))
            .background(if (isActive) Color(0xFF142438) else Color(0xFF161C28))
            .border(
                BorderStroke(
                    width = if (isActive) 1.8.dp else 1.dp,
                    color = if (isActive) glowColor else Color(0xFF38435C)
                ),
                RoundedCornerShape(3.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isActive) {
            Box(
                modifier = Modifier
                    .width(1.8.dp)
                    .height(slotH * 0.65f)
                    .background(AccentCyan, RoundedCornerShape(1.dp))
            )
        }
    }
}
