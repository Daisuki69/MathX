package com.example.ui

object MathFormatter {
    private val greekToAscii = listOf(
        "α" to "alpha",
        "β" to "beta",
        "γ" to "gamma",
        "δ" to "delta",
        "ε" to "epsilon",
        "θ" to "theta",
        "λ" to "lambda",
        "μ" to "mu",
        "π" to "pi",
        "ρ" to "rho",
        "σ" to "sigma",
        "τ" to "tau",
        "φ" to "phi",
        "ω" to "omega",
        "Δ" to "Delta"
    )

    fun toDisplayString(rawFormula: String): String {
        var result = rawFormula
            .replace("==", " = ")
            .replace("<=", " ≤ ")
            .replace(">=", " ≥ ")
            .replace("!=", " ≠ ")
            .replace(" * ", " · ")
            .replace("*", " · ")
            .replace("^2", "²")
            .replace("^3", "³")
            .replace("^4", "⁴")
            .replace("^5", "⁵")
            .replace("^6", "⁶")
            .replace("^7", "⁷")
            .replace("^8", "⁸")
            .replace("^9", "⁹")
            .replace("^0", "⁰")
            .replace("^1", "¹")
            .replace("^n", "ⁿ")
            .replace("^x", "ˣ")
            .replace("^y", "ʸ")
            .replace("oo", "∞")
            .replace("arcsin", "sin⁻¹")
            .replace("arccos", "cos⁻¹")
            .replace("arctan", "tan⁻¹")

        for ((unicode, ascii) in greekToAscii) {
            result = result.replace(Regex("""\b$ascii\b"""), unicode)
        }

        result = result.replace(Regex("""sqrt\(([^)]+)\)"""), "√($1)")
        result = result.replace(Regex("""abs\(([^)]+)\)"""), "|$1|")
        result = result.replace(Regex("""\s*/\s*"""), "/")
        result = result.replace(Regex("""\s+"""), " ").trim()
        return result
    }

    fun toParseableString(displayFormula: String): String {
        var result = displayFormula
            .replace("·", "*")
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("≤", "<=")
            .replace("≥", ">=")
            .replace("≠", "!=")
            .replace("²", "^2")
            .replace("³", "^3")
            .replace("⁴", "^4")
            .replace("⁵", "^5")
            .replace("⁶", "^6")
            .replace("⁷", "^7")
            .replace("⁸", "^8")
            .replace("⁹", "^9")
            .replace("⁰", "^0")
            .replace("¹", "^1")
            .replace("ⁿ", "^n")
            .replace("ˣ", "^x")
            .replace("ʸ", "^y")
            .replace("∞", "oo")
            .replace("sin⁻¹", "arcsin")
            .replace("cos⁻¹", "arccos")
            .replace("tan⁻¹", "arctan")

        for ((unicode, ascii) in greekToAscii) {
            result = result.replace(unicode, ascii)
        }

        result = result.replace(Regex("""√\(([^)]*)\)"""), "sqrt($1)")
        result = result.replace(Regex("""√([a-zA-Z0-9]+)"""), "sqrt($1)")
        result = result.replace(Regex("""\|([^|]+)\|"""), "abs($1)")

        return result
    }
}
