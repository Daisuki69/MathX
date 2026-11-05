package com.example.symja

import org.matheclipse.parser.client.Parser
import org.matheclipse.parser.client.SyntaxError
import org.matheclipse.parser.client.ast.ASTNode
import org.matheclipse.parser.client.ast.FloatNode
import org.matheclipse.parser.client.ast.FractionNode
import org.matheclipse.parser.client.ast.FunctionNode
import org.matheclipse.parser.client.ast.IntegerNode
import org.matheclipse.parser.client.ast.SymbolNode
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
    val records: List<MutationRecord>
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

        fun mutateAst(node: ASTNode): String = when (node) {
            is SymbolNode -> {
                val name = node.toString()
                if (isConstantSymbol(name)) {
                    val newConst = if (name.equals("pi", ignoreCase = true)) "e" else "pi"
                    records.add(MutationRecord(original = name, mutated = newConst, type = MutationType.CONSTANT))
                    newConst
                } else {
                    varMapping[name] ?: variablePool.random(random)
                }
            }
            is IntegerNode -> {
                val orig = node.toString()
                val newConst = random.nextInt(1, 15).toString()
                records.add(MutationRecord(original = orig, mutated = newConst, type = MutationType.CONSTANT))
                newConst
            }
            is FloatNode -> {
                val orig = node.toString()
                val newConst = ((random.nextInt(1, 99)) / 10.0).toString()
                records.add(MutationRecord(original = orig, mutated = newConst, type = MutationType.CONSTANT))
                newConst
            }
            is FractionNode -> {
                val orig = node.toString()
                val newConst = "${random.nextInt(1, 9)}/${random.nextInt(2, 9)}"
                records.add(MutationRecord(original = orig, mutated = newConst, type = MutationType.CONSTANT))
                newConst
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

        val mutatedFormula = mutateAst(root)
        return MutationResult(
            mutatedFormula = mutatedFormula,
            records = records
        )
    }

    private fun formatMutatedFunction(head: String, args: List<String>, records: MutableList<MutationRecord>): String = when (head) {
        "Equal" -> {
            val left = args.getOrElse(0) { "" }
            val right = args.getOrElse(1) { "" }
            "$left == $right"
        }
        "Plus" -> {
            if (args.isEmpty()) "0"
            else args.joinToString(" + ")
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
            else args.joinToString(" * ")
        }
        "Divide" -> {
            val left = args.getOrElse(0) { "" }
            val right = args.getOrElse(1) { "" }
            val op = if (random.nextBoolean()) "*" else "/"
            if (op != "/") records.add(MutationRecord("/", op, MutationType.OPERATOR))
            "$left $op $right"
        }
        "Power" -> {
            val base = args.getOrElse(0) { "" }
            val exp = args.getOrElse(1) { "" }
            val op = if (random.nextBoolean()) "^" else "**"
            "$base$op$exp"
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
}
