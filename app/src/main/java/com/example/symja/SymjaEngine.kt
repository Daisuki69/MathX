package com.example.symja

import org.matheclipse.parser.client.Parser
import org.matheclipse.parser.client.SyntaxError
import org.matheclipse.parser.client.ast.ASTNode
import org.matheclipse.parser.client.ast.FloatNode
import org.matheclipse.parser.client.ast.FractionNode
import org.matheclipse.parser.client.ast.FunctionNode
import org.matheclipse.parser.client.ast.IntegerNode
import org.matheclipse.parser.client.ast.SymbolNode
import org.matheclipse.parser.client.eval.DoubleEvaluator
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random

data class MutationRecord(
    val original: String,
    val mutated: String,
    val type: MutationType
)

enum class MutationType {
    VARIABLE,
    CONSTANT,
    OPERATOR
}

data class MutationResult(
    val mutatedFormula: String,
    val records: List<MutationRecord>,
    val variableValues: Map<String, Double> = emptyMap()
)

data class SymjaValidation(
    val isValid: Boolean,
    val astNode: ASTNode?,
    val variables: List<String>,
    val constants: List<String>,
    val operators: List<String>,
    val errorMessage: String? = null,
    val errorPosition: Int = 0
)

class SymjaEngine(
    private val random: Random = Random.Default
) {
    private val variablePool = listOf("x", "y", "z", "t", "u", "v", "w", "a", "b", "c", "m", "n", "theta", "alpha", "beta", "lambda", "phi")
    private val constantPool = listOf("2", "3", "4", "5", "6", "7", "8", "9", "12", "pi", "e")

    fun validate(formula: String): SymjaValidation {
        val trimmed = formula.trim()
        if (trimmed.isEmpty()) {
            return SymjaValidation(
                isValid = false,
                astNode = null,
                variables = emptyList(),
                constants = emptyList(),
                operators = emptyList()
            )
        }

        return try {
            val normalized = com.example.ui.MathFormatter.toParseableString(trimmed)
            val parser = Parser(true)
            val root = parser.parse(normalized)
            val detectedVars = LinkedHashSet<String>()
            val detectedConsts = LinkedHashSet<String>()
            val detectedOps = LinkedHashSet<String>()

            collectTokens(root, detectedVars, detectedConsts, detectedOps)

            SymjaValidation(
                isValid = true,
                astNode = root,
                variables = detectedVars.toList(),
                constants = detectedConsts.toList(),
                operators = detectedOps.toList()
            )
        } catch (e: SyntaxError) {
            SymjaValidation(
                isValid = false,
                astNode = null,
                variables = emptyList(),
                constants = emptyList(),
                operators = emptyList(),
                errorMessage = e.message ?: "Syntax error",
                errorPosition = e.startOffset
            )
        } catch (e: Exception) {
            SymjaValidation(
                isValid = false,
                astNode = null,
                variables = emptyList(),
                constants = emptyList(),
                operators = emptyList(),
                errorMessage = e.message ?: "Invalid formula",
                errorPosition = 0
            )
        }
    }

    private fun collectTokens(
        node: ASTNode,
        vars: MutableSet<String>,
        consts: MutableSet<String>,
        ops: MutableSet<String>
    ) {
        when (node) {
            is FunctionNode -> {
                if (node.isNotEmpty()) {
                    val head = node[0].toString()
                    val mappedOp = mapHeadToOperator(head)
                    if (mappedOp != null) {
                        ops.add(mappedOp)
                    }
                    for (i in 1 until node.size) {
                        collectTokens(node[i], vars, consts, ops)
                    }
                }
            }
            is IntegerNode -> consts.add(node.toString())
            is FloatNode -> consts.add(node.toString())
            is FractionNode -> consts.add(node.toString())
            is SymbolNode -> {
                val name = node.toString()
                if (isConstantSymbol(name)) {
                    consts.add(name)
                } else {
                    vars.add(name)
                }
            }
        }
    }

    fun mutate(validation: SymjaValidation): MutationResult {
        val root = validation.astNode ?: return MutationResult("", emptyList())
        val records = mutableListOf<MutationRecord>()
        val varMapping = mutableMapOf<String, String>()

        val availableVars = variablePool.filter { it !in validation.variables }.shuffled(random)
        var poolIdx = 0
        for (v in validation.variables) {
            val target = if (poolIdx < availableVars.size) availableVars[poolIdx++] else variablePool.random(random)
            varMapping[v] = target
            records.add(MutationRecord(original = v, mutated = target, type = MutationType.VARIABLE))
        }

        val primaryVar = if (varMapping.isNotEmpty()) varMapping.values.first() else variablePool.random(random)

        fun mutateAst(node: ASTNode): String = when (node) {
            is SymbolNode -> {
                val name = node.toString()
                if (isConstantSymbol(name)) {
                    val newConst = if (name.equals("pi", ignoreCase = true)) "e" else "pi"
                    records.add(MutationRecord(original = name, mutated = newConst, type = MutationType.CONSTANT))
                    newConst
                } else {
                    if (random.nextInt(4) == 0) {
                        val constVal = random.nextInt(1, 15).toString()
                        records.add(MutationRecord(original = name, mutated = constVal, type = MutationType.CONSTANT))
                        constVal
                    } else {
                        val baseVar = varMapping[name] ?: variablePool.random(random)
                        val roll = random.nextInt(0, 3)
                        val mutatedVar = when (roll) {
                            0 -> {
                                val coeff = random.nextInt(2, 9)
                                records.add(MutationRecord(original = name, mutated = "$coeff * $baseVar", type = MutationType.VARIABLE))
                                "$coeff * $baseVar"
                            }
                            1 -> {
                                val coeff = random.nextInt(2, 9)
                                records.add(MutationRecord(original = name, mutated = "$coeff * $baseVar", type = MutationType.VARIABLE))
                                "$coeff * $baseVar"
                            }
                            else -> {
                                records.add(MutationRecord(original = name, mutated = baseVar, type = MutationType.VARIABLE))
                                baseVar
                            }
                        }
                        mutatedVar
                    }
                }
            }
            is IntegerNode -> {
                val orig = node.toString()
                if (random.nextInt(3) == 0) {
                    val targetVar = if (random.nextInt(10) < 7) primaryVar else variablePool.random(random)
                    val roll = random.nextInt(3)
                    val res = when (roll) {
                        0 -> {
                            val coeff = random.nextInt(2, 9)
                            "$coeff * $targetVar"
                        }
                        else -> targetVar
                    }
                    records.add(MutationRecord(original = orig, mutated = res, type = MutationType.VARIABLE))
                    res
                } else {
                    val newConst = if (random.nextInt(5) == 0) {
                        (-random.nextInt(1, 9)).toString()
                    } else {
                        random.nextInt(1, 15).toString()
                    }
                    records.add(MutationRecord(original = orig, mutated = newConst, type = MutationType.CONSTANT))
                    newConst
                }
            }
            is FloatNode -> {
                val orig = node.toString()
                if (random.nextInt(4) == 0) {
                    val targetVar = if (random.nextInt(10) < 7) primaryVar else variablePool.random(random)
                    records.add(MutationRecord(original = orig, mutated = targetVar, type = MutationType.VARIABLE))
                    targetVar
                } else {
                    val newConst = ((random.nextInt(1, 99)) / 10.0).toString()
                    records.add(MutationRecord(original = orig, mutated = newConst, type = MutationType.CONSTANT))
                    newConst
                }
            }
            is FractionNode -> {
                val orig = node.toString()
                if (random.nextInt(4) == 0) {
                    val targetVar = if (random.nextInt(10) < 7) primaryVar else variablePool.random(random)
                    records.add(MutationRecord(original = orig, mutated = targetVar, type = MutationType.VARIABLE))
                    targetVar
                } else {
                    val den = random.nextInt(2, 9)
                    val num = random.nextInt(1, 9)
                    val newConst = "$num/$den"
                    records.add(MutationRecord(original = orig, mutated = newConst, type = MutationType.CONSTANT))
                    newConst
                }
            }
            is FunctionNode -> {
                if (node.isEmpty()) ""
                else {
                    val head = node[0].toString()
                    val args = (1 until node.size).map { mutateAst(node[it]) }
                    formatMutatedFunction(head, args, records)
                }
            }
            else -> node.toString()
        }

        var mutatedFormula = mutateAst(root)
        mutatedFormula = mutatedFormula.replace(Regex("""/\s*(?=[+=\-<>]|$)"""), "/${random.nextInt(2, 6)}")
        mutatedFormula = mutatedFormula.replace(Regex("""/\s*\(\s*\)"""), "/(${random.nextInt(2, 6)})")
        mutatedFormula = mutatedFormula.replace(Regex("""/\s*0(?!\d)"""), "/${random.nextInt(2, 6)}")
        mutatedFormula = mutatedFormula.replace(Regex("""\s*/\s*"""), "/")

        val varValues = mutableMapOf<String, Double>()

        val detectedVars = mutableSetOf<String>()
        DoubleEvaluator.getVariables(mutatedFormula, detectedVars)
        val freeVars = detectedVars.filter { !isConstantSymbol(it) && !isBuiltInFunction(it) }

        if (freeVars.isNotEmpty()) {
            if (!mutatedFormula.contains("==")) {
                val candidates = listOf(1, 2, 3, 4, 5, -1, -2, -3, 6, 0).shuffled(random)
                val testEval = DoubleEvaluator(true).apply {
                    defineVariable("pi", Math.PI)
                    defineVariable("e", Math.E)
                }
                var selectedEquation = ""
                if (freeVars.size == 1) {
                    val v = freeVars.first()
                    for (cand in candidates) {
                        testEval.defineVariable(v, cand.toDouble())
                        try {
                            val evalVal = testEval.evaluate(mutatedFormula)
                            if (!evalVal.isNaN() && !evalVal.isInfinite()) {
                                varValues[v] = cand.toDouble()
                                if (random.nextBoolean()) {
                                    val rightCoeff = listOf(1, 2, 3, -1).random(random)
                                    val rightConst = evalVal - rightCoeff * cand.toDouble()
                                    val rightStr = if (Math.abs(rightConst) < 1e-9) {
                                        if (rightCoeff == 1) v else if (rightCoeff == -1) "-$v" else "$rightCoeff * $v"
                                    } else if (rightConst > 0) {
                                        val formattedConst = formatDouble(rightConst)
                                        if (rightCoeff == 1) "$v + $formattedConst" else if (rightCoeff == -1) "-$v + $formattedConst" else "$rightCoeff * $v + $formattedConst"
                                    } else {
                                        val formattedConst = formatDouble(-rightConst)
                                        if (rightCoeff == 1) "$v - $formattedConst" else if (rightCoeff == -1) "-$v - $formattedConst" else "$rightCoeff * $v - $formattedConst"
                                    }
                                    selectedEquation = "$mutatedFormula == $rightStr"
                                } else {
                                    val formattedAns = formatDouble(evalVal)
                                    selectedEquation = "$mutatedFormula == $formattedAns"
                                }
                                break
                            }
                        } catch (_: Exception) {}
                    }
                    if (selectedEquation.isEmpty()) {
                        val fallbackCand = candidates.first()
                        varValues[v] = fallbackCand.toDouble()
                        selectedEquation = "$mutatedFormula == $fallbackCand"
                    }
                } else {
                    for (cand in candidates) {
                        for (v in freeVars) {
                            testEval.defineVariable(v, cand.toDouble())
                        }
                        try {
                            val evalVal = testEval.evaluate(mutatedFormula)
                            if (!evalVal.isNaN() && !evalVal.isInfinite()) {
                                for (v in freeVars) {
                                    varValues[v] = cand.toDouble()
                                }
                                val formattedAns = formatDouble(evalVal)
                                selectedEquation = "$mutatedFormula == $formattedAns"
                                break
                            }
                        } catch (_: Exception) {}
                    }
                    if (selectedEquation.isEmpty()) {
                        val fallbackCand = candidates.first()
                        for (v in freeVars) {
                            varValues[v] = fallbackCand.toDouble()
                        }
                        selectedEquation = "$mutatedFormula == $fallbackCand"
                    }
                }
                mutatedFormula = selectedEquation
            }
        } else {
            if (!mutatedFormula.contains("==")) {
                val assignedVar = listOf("x", "y", "z", "a", "b", "c", "n").random(random)
                val testEval = DoubleEvaluator(true).apply {
                    defineVariable("pi", Math.PI)
                    defineVariable("e", Math.E)
                }
                try {
                    val evalVal = testEval.evaluate(mutatedFormula)
                    if (!evalVal.isNaN() && !evalVal.isInfinite()) {
                        varValues[assignedVar] = evalVal
                        mutatedFormula = "$mutatedFormula == $assignedVar"
                    }
                } catch (_: Exception) {}
            }
        }

        return MutationResult(
            mutatedFormula = mutatedFormula,
            records = records,
            variableValues = varValues
        )
    }

    fun evaluateAnswer(formula: String, variableValues: Map<String, Double> = emptyMap()): String {
        val trimmed = formula.trim()
        if (trimmed.isEmpty()) return ""

        return try {
            val normalized = com.example.ui.MathFormatter.toParseableString(trimmed)

            if (normalized.contains("==")) {
                val parts = normalized.split("==")
                if (parts.size == 2) {
                    val leftStr = parts[0].trim()
                    val rightStr = parts[1].trim()
                    val evaluator = DoubleEvaluator(true).apply {
                        defineVariable("pi", Math.PI)
                        defineVariable("e", Math.E)
                    }
                    val leftVars = mutableSetOf<String>()
                    val rightVars = mutableSetOf<String>()
                    DoubleEvaluator.getVariables(leftStr, leftVars)
                    DoubleEvaluator.getVariables(rightStr, rightVars)
                    val allVars = (leftVars + rightVars).filter { !isConstantSymbol(it) && !isBuiltInFunction(it) }

                    if (allVars.isEmpty()) {
                        val lVal = evaluator.evaluate(leftStr)
                        val rVal = evaluator.evaluate(rightStr)
                        val isEq = Math.abs(lVal - rVal) < 1e-9
                        return if (isEq) "True" else "False"
                    } else if (allVars.size == 1) {
                        val varName = allVars.first()
                        if (variableValues.containsKey(varName)) {
                            return "$varName = ${formatDouble(variableValues[varName]!!)}"
                        }
                        for (testRoot in -30..30) {
                            evaluator.defineVariable(varName, testRoot.toDouble())
                            try {
                                val diff = evaluator.evaluate(leftStr) - evaluator.evaluate(rightStr)
                                if (Math.abs(diff) < 1e-6) {
                                    return "$varName = $testRoot"
                                }
                            } catch (_: Exception) {}
                        }
                        evaluator.defineVariable(varName, 0.0)
                        val f0 = evaluator.evaluate(leftStr) - evaluator.evaluate(rightStr)
                        evaluator.defineVariable(varName, 1.0)
                        val f1 = evaluator.evaluate(leftStr) - evaluator.evaluate(rightStr)
                        val slope = f1 - f0
                        if (Math.abs(slope) > 1e-12) {
                            val solution = -f0 / slope
                            return "$varName = ${formatDouble(solution)}"
                        }
                    } else {
                        if (variableValues.isNotEmpty()) {
                            return allVars.mapNotNull { v ->
                                variableValues[v]?.let { "$v = ${formatDouble(it)}" }
                            }.joinToString(", ")
                        }
                    }
                }
            }

            val evaluator = DoubleEvaluator(true).apply {
                defineVariable("pi", Math.PI)
                defineVariable("e", Math.E)
            }

            val vars = mutableSetOf<String>()
            DoubleEvaluator.getVariables(normalized, vars)
            val freeVars = vars.filter { !isConstantSymbol(it) && !isBuiltInFunction(it) }
            if (freeVars.isNotEmpty()) {
                if (variableValues.isNotEmpty()) {
                    return freeVars.mapNotNull { v ->
                        variableValues[v]?.let { "$v = ${formatDouble(it)}" }
                    }.joinToString(", ")
                }
                return ""
            }

            val value = evaluator.evaluate(normalized)
            formatDouble(value)
        } catch (e: Exception) {
            ""
        }
    }

    private fun formatDouble(d: Double): String {
        if (d.isNaN()) return "NaN"
        if (d.isInfinite()) return if (d > 0) "∞" else "-∞"
        if (Math.abs(d - Math.round(d)) < 1e-9) {
            return Math.round(d).toString()
        }
        val rounded = BigDecimal(d).setScale(4, RoundingMode.HALF_UP).stripTrailingZeros()
        return rounded.toPlainString()
    }

    private fun formatMutatedFunction(head: String, args: List<String>, records: MutableList<MutationRecord>): String = when (head) {
        "Equal" -> {
            val left = args.getOrElse(0) { "" }
            val right = args.getOrElse(1) { "" }
            "$left == $right"
        }
        "Plus" -> {
            if (args.isEmpty()) "0"
            else {
                if (args.size == 2) {
                    val op = if (random.nextBoolean()) "+" else "-"
                    if (op != "+") records.add(MutationRecord("+", op, MutationType.OPERATOR))
                    "${args[0]} $op ${args[1]}"
                } else {
                    val sep = if (random.nextBoolean()) " + " else " - "
                    args.joinToString(sep)
                }
            }
        }
        "Subtract" -> {
            val left = args.getOrElse(0) { "" }
            val right = args.getOrElse(1) { "" }
            val op = if (random.nextBoolean()) "+" else "-"
            if (op != "-") records.add(MutationRecord("-", op, MutationType.OPERATOR))
            "$left $op $right"
        }
        "Times" -> {
            if (args.isEmpty()) "1"
            else {
                if (args.size == 2 && random.nextInt(3) == 0) {
                    val left = args[0].ifBlank { "1" }
                    var right = args[1].trim()
                    if (right.isEmpty() || right == "0" || right == "()") {
                        right = random.nextInt(2, 6).toString()
                    }
                    val safeLeft = if (left.contains("+") || left.contains("-")) "($left)" else left
                    val safeRight = if (right.contains("/") || right.contains("+") || right.contains("-") || right.contains("*")) "($right)" else right
                    records.add(MutationRecord("*", "/", MutationType.OPERATOR))
                    "$safeLeft/$safeRight"
                } else {
                    args.joinToString(" * ")
                }
            }
        }
        "Divide" -> {
            val left = args.getOrElse(0) { "1" }.ifBlank { "1" }
            var right = args.getOrElse(1) { "" }.trim()
            if (right.isEmpty() || right == "0" || right == "()") {
                right = random.nextInt(2, 6).toString()
            }
            val safeLeft = if (left.contains("+") || left.contains("-")) "($left)" else left
            val safeRight = if (right.contains("/") || right.contains("+") || right.contains("-") || right.contains("*")) "($right)" else right
            val op = if (random.nextBoolean()) "*" else "/"
            if (op != "/") records.add(MutationRecord("/", op, MutationType.OPERATOR))
            if (op == "/") "$safeLeft/$safeRight" else "$left * $right"
        }
        "Power" -> {
            val base = args.getOrElse(0) { "" }
            val exp = args.getOrElse(1) { "" }
            val newExp = if (random.nextBoolean()) random.nextInt(2, 5).toString() else exp
            records.add(MutationRecord("^$exp", "^$newExp", MutationType.CONSTANT))
            "$base^$newExp"
        }
        "Sin" -> {
            val fn = listOf("cos", "sin", "tan").random(random)
            records.add(MutationRecord("sin", fn, MutationType.OPERATOR))
            "$fn(${args.joinToString(", ")})"
        }
        "Cos" -> {
            val fn = listOf("sin", "cos", "tan").random(random)
            records.add(MutationRecord("cos", fn, MutationType.OPERATOR))
            "$fn(${args.joinToString(", ")})"
        }
        "Tan" -> {
            val fn = listOf("sin", "cos", "tan").random(random)
            records.add(MutationRecord("tan", fn, MutationType.OPERATOR))
            "$fn(${args.joinToString(", ")})"
        }
        "Sqrt" -> {
            val fn = if (random.nextBoolean()) "cbrt" else "sqrt"
            records.add(MutationRecord("sqrt", fn, MutationType.OPERATOR))
            "$fn(${args.joinToString(", ")})"
        }
        else -> {
            val fnLower = head.lowercase()
            "$fnLower(${args.joinToString(", ")})"
        }
    }

    private fun mapHeadToOperator(head: String): String? = when (head) {
        "Equal" -> "=="
        "Unequal" -> "!="
        "Plus" -> "+"
        "Subtract" -> "-"
        "Times" -> "*"
        "Divide" -> "/"
        "Power" -> "^"
        "Less" -> "<"
        "Greater" -> ">"
        "LessEqual" -> "<="
        "GreaterEqual" -> ">="
        else -> null
    }

    private fun isConstantSymbol(name: String): Boolean =
        name.equals("pi", ignoreCase = true) ||
        name.equals("e", ignoreCase = true) ||
        name.equals("infinity", ignoreCase = true) ||
        name.equals("oo", ignoreCase = true) ||
        name.equals("i", ignoreCase = false)

    private fun isBuiltInFunction(name: String): Boolean =
        name in setOf("sin", "cos", "tan", "sec", "csc", "cot", "asin", "acos", "atan",
                      "arcsin", "arccos", "arctan", "sinh", "cosh", "tanh",
                      "sqrt", "cbrt", "root", "abs", "ln", "log", "log2", "exp", "mod")
}
