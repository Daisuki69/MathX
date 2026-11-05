package com.example.ui.keyboard

object KeyboardLayouts {
    val basicRows: List<List<SciCalcKey>> = listOf(
        listOf(
            SciCalcKey("x²", "²", 0, KeyCategory.FUNCTION, hasAlternate = true, altInsertText = "³"),
            SciCalcKey("xⁿ", "^", 0, KeyCategory.FUNCTION, hasAlternate = true, altInsertText = "^4"),
            SciCalcKey("√x", "√()", -1, KeyCategory.FUNCTION, hasAlternate = true, altInsertText = "cbrt()", altCursorOffset = -1),
            SciCalcKey("a/b", "/", 0, KeyCategory.OPERATOR),
            SciCalcKey("|x|", "| |", -1, KeyCategory.FUNCTION)
        ),
        listOf(
            SciCalcKey("7", "7", 0, KeyCategory.NUMBER),
            SciCalcKey("8", "8", 0, KeyCategory.NUMBER),
            SciCalcKey("9", "9", 0, KeyCategory.NUMBER),
            SciCalcKey("÷", "/", 0, KeyCategory.OPERATOR, hasAlternate = true, altInsertText = "%"),
            SciCalcKey("x", "x", 0, KeyCategory.VARIABLE)
        ),
        listOf(
            SciCalcKey("4", "4", 0, KeyCategory.NUMBER),
            SciCalcKey("5", "5", 0, KeyCategory.NUMBER),
            SciCalcKey("6", "6", 0, KeyCategory.NUMBER),
            SciCalcKey("×", "*", 0, KeyCategory.OPERATOR),
            SciCalcKey("y", "y", 0, KeyCategory.VARIABLE)
        ),
        listOf(
            SciCalcKey("1", "1", 0, KeyCategory.NUMBER),
            SciCalcKey("2", "2", 0, KeyCategory.NUMBER),
            SciCalcKey("3", "3", 0, KeyCategory.NUMBER),
            SciCalcKey("−", "-", 0, KeyCategory.OPERATOR),
            SciCalcKey("z", "z", 0, KeyCategory.VARIABLE)
        ),
        listOf(
            SciCalcKey("0", "0", 0, KeyCategory.NUMBER),
            SciCalcKey(".", ".", 0, KeyCategory.NUMBER),
            SciCalcKey("π", "pi", 0, KeyCategory.VARIABLE),
            SciCalcKey("+", "+", 0, KeyCategory.OPERATOR),
            SciCalcKey("=", "==", 0, KeyCategory.ACTION, hasAlternate = true, altInsertText = "!=")
        )
    )

    val functionRows: List<List<SciCalcKey>> = listOf(
        listOf(
            SciCalcKey("sin", "sin()", -1, KeyCategory.FUNCTION, hasAlternate = true, altInsertText = "sin⁻¹()", altCursorOffset = -1),
            SciCalcKey("cos", "cos()", -1, KeyCategory.FUNCTION, hasAlternate = true, altInsertText = "cos⁻¹()", altCursorOffset = -1),
            SciCalcKey("tan", "tan()", -1, KeyCategory.FUNCTION, hasAlternate = true, altInsertText = "tan⁻¹()", altCursorOffset = -1),
            SciCalcKey("sin⁻¹", "sin⁻¹()", -1, KeyCategory.FUNCTION),
            SciCalcKey("cos⁻¹", "cos⁻¹()", -1, KeyCategory.FUNCTION)
        ),
        listOf(
            SciCalcKey("ln", "ln()", -1, KeyCategory.FUNCTION, hasAlternate = true, altInsertText = "log()", altCursorOffset = -1),
            SciCalcKey("log", "log()", -1, KeyCategory.FUNCTION),
            SciCalcKey("eˣ", "e^", 0, KeyCategory.FUNCTION),
            SciCalcKey("tan⁻¹", "tan⁻¹()", -1, KeyCategory.FUNCTION),
            SciCalcKey("n!", "!", 0, KeyCategory.OPERATOR)
        ),
        listOf(
            SciCalcKey("sinh", "sinh()", -1, KeyCategory.FUNCTION),
            SciCalcKey("cosh", "cosh()", -1, KeyCategory.FUNCTION),
            SciCalcKey("tanh", "tanh()", -1, KeyCategory.FUNCTION),
            SciCalcKey("e", "e", 0, KeyCategory.VARIABLE),
            SciCalcKey("∞", "oo", 0, KeyCategory.VARIABLE)
        ),
        listOf(
            SciCalcKey("(", "(", 0, KeyCategory.OPERATOR),
            SciCalcKey(")", ")", 0, KeyCategory.OPERATOR),
            SciCalcKey(",", ", ", 0, KeyCategory.OPERATOR),
            SciCalcKey("a", "a", 0, KeyCategory.VARIABLE),
            SciCalcKey("b", "b", 0, KeyCategory.VARIABLE)
        ),
        listOf(
            SciCalcKey("<", "<", 0, KeyCategory.OPERATOR, hasAlternate = true, altInsertText = "<="),
            SciCalcKey(">", ">", 0, KeyCategory.OPERATOR, hasAlternate = true, altInsertText = ">="),
            SciCalcKey("≤", "<=", 0, KeyCategory.OPERATOR),
            SciCalcKey("≥", ">=", 0, KeyCategory.OPERATOR),
            SciCalcKey("≠", "!=", 0, KeyCategory.OPERATOR)
        )
    )

    val calculusRows: List<List<SciCalcKey>> = listOf(
        listOf(
            SciCalcKey("d/dx", "d/dx()", -1, KeyCategory.FUNCTION),
            SciCalcKey("∫", "integrate()", -1, KeyCategory.FUNCTION),
            SciCalcKey("lim", "lim()", -1, KeyCategory.FUNCTION),
            SciCalcKey("∑", "sum()", -1, KeyCategory.FUNCTION),
            SciCalcKey("∞", "oo", 0, KeyCategory.VARIABLE)
        ),
        listOf(
            SciCalcKey("sin", "sin()", -1, KeyCategory.FUNCTION),
            SciCalcKey("cos", "cos()", -1, KeyCategory.FUNCTION),
            SciCalcKey("tan", "tan()", -1, KeyCategory.FUNCTION),
            SciCalcKey("sec", "sec()", -1, KeyCategory.FUNCTION),
            SciCalcKey("csc", "csc()", -1, KeyCategory.FUNCTION)
        ),
        listOf(
            SciCalcKey("asin", "arcsin()", -1, KeyCategory.FUNCTION),
            SciCalcKey("acos", "arccos()", -1, KeyCategory.FUNCTION),
            SciCalcKey("atan", "arctan()", -1, KeyCategory.FUNCTION),
            SciCalcKey("exp", "exp()", -1, KeyCategory.FUNCTION),
            SciCalcKey("log", "log()", -1, KeyCategory.FUNCTION)
        ),
        listOf(
            SciCalcKey("sinh", "sinh()", -1, KeyCategory.FUNCTION),
            SciCalcKey("cosh", "cosh()", -1, KeyCategory.FUNCTION),
            SciCalcKey("tanh", "tanh()", -1, KeyCategory.FUNCTION),
            SciCalcKey("Γ", "gamma()", -1, KeyCategory.FUNCTION),
            SciCalcKey("n!", "factorial()", -1, KeyCategory.FUNCTION)
        ),
        listOf(
            SciCalcKey("t", "t", 0, KeyCategory.VARIABLE),
            SciCalcKey("u", "u", 0, KeyCategory.VARIABLE),
            SciCalcKey("v", "v", 0, KeyCategory.VARIABLE),
            SciCalcKey("w", "w", 0, KeyCategory.VARIABLE),
            SciCalcKey("k", "k", 0, KeyCategory.VARIABLE)
        )
    )

    val greekRows: List<List<SciCalcKey>> = listOf(
        listOf(
            SciCalcKey("α", "alpha", 0, KeyCategory.VARIABLE),
            SciCalcKey("β", "beta", 0, KeyCategory.VARIABLE),
            SciCalcKey("γ", "gamma", 0, KeyCategory.VARIABLE),
            SciCalcKey("δ", "delta", 0, KeyCategory.VARIABLE),
            SciCalcKey("ε", "epsilon", 0, KeyCategory.VARIABLE)
        ),
        listOf(
            SciCalcKey("θ", "theta", 0, KeyCategory.VARIABLE),
            SciCalcKey("λ", "lambda", 0, KeyCategory.VARIABLE),
            SciCalcKey("μ", "mu", 0, KeyCategory.VARIABLE),
            SciCalcKey("π", "pi", 0, KeyCategory.VARIABLE),
            SciCalcKey("ρ", "rho", 0, KeyCategory.VARIABLE)
        ),
        listOf(
            SciCalcKey("σ", "sigma", 0, KeyCategory.VARIABLE),
            SciCalcKey("τ", "tau", 0, KeyCategory.VARIABLE),
            SciCalcKey("φ", "phi", 0, KeyCategory.VARIABLE),
            SciCalcKey("ψ", "psi", 0, KeyCategory.VARIABLE),
            SciCalcKey("ω", "omega", 0, KeyCategory.VARIABLE)
        ),
        listOf(
            SciCalcKey("Δ", "Delta", 0, KeyCategory.VARIABLE),
            SciCalcKey("Σ", "Sigma", 0, KeyCategory.VARIABLE),
            SciCalcKey("Ω", "Omega", 0, KeyCategory.VARIABLE),
            SciCalcKey("η", "eta", 0, KeyCategory.VARIABLE),
            SciCalcKey("ξ", "xi", 0, KeyCategory.VARIABLE)
        )
    )
}
