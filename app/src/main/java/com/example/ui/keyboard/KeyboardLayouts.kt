package com.example.ui.keyboard

object KeyboardLayouts {
    val basicRows: List<List<SciCalcKey>> = listOf(
        listOf(
            SciCalcKey(
                label = "x²",
                insertText = "²",
                cursorOffset = 0,
                category = KeyCategory.FUNCTION,
                hasAlternate = true,
                alternates = listOf(
                    SciCalcKey("x³", "³", 0, KeyCategory.FUNCTION),
                    SciCalcKey("x⁴", "^(4)", -1, KeyCategory.FUNCTION)
                )
            ),
            SciCalcKey(
                label = "xⁿ",
                insertText = "^()",
                cursorOffset = -1,
                category = KeyCategory.FUNCTION,
                hasAlternate = true,
                alternates = listOf(
                    SciCalcKey("10ⁿ", "10^()", -1, KeyCategory.FUNCTION),
                    SciCalcKey("eⁿ", "e^()", -1, KeyCategory.FUNCTION)
                )
            ),
            SciCalcKey(
                label = "√x",
                insertText = "√()",
                cursorOffset = -1,
                category = KeyCategory.FUNCTION,
                hasAlternate = true,
                alternates = listOf(
                    SciCalcKey("∛", "cbrt()", -1, KeyCategory.FUNCTION),
                    SciCalcKey("ⁿ√", "root()", -1, KeyCategory.FUNCTION)
                )
            ),
            SciCalcKey("a/b", "/()", -1, KeyCategory.OPERATOR),
            SciCalcKey("|x|", "| |", -1, KeyCategory.FUNCTION)
        ),
        listOf(
            SciCalcKey("7", "7", 0, KeyCategory.NUMBER),
            SciCalcKey("8", "8", 0, KeyCategory.NUMBER),
            SciCalcKey("9", "9", 0, KeyCategory.NUMBER),
            SciCalcKey(
                label = "÷",
                insertText = "/",
                cursorOffset = 0,
                category = KeyCategory.OPERATOR,
                hasAlternate = true,
                alternates = listOf(
                    SciCalcKey("%", "%", 0, KeyCategory.OPERATOR),
                    SciCalcKey("mod", "mod()", -1, KeyCategory.FUNCTION)
                )
            ),
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
            SciCalcKey("π", "π", 0, KeyCategory.VARIABLE),
            SciCalcKey("+", "+", 0, KeyCategory.OPERATOR),
            SciCalcKey(
                label = "=",
                insertText = "==",
                cursorOffset = 0,
                category = KeyCategory.ACTION,
                hasAlternate = true,
                alternates = listOf(
                    SciCalcKey("≠", "!=", 0, KeyCategory.OPERATOR),
                    SciCalcKey("≤", "<=", 0, KeyCategory.OPERATOR),
                    SciCalcKey("≥", ">=", 0, KeyCategory.OPERATOR)
                )
            )
        )
    )

    val functionRows: List<List<SciCalcKey>> = listOf(
        listOf(
            SciCalcKey(
                label = "sin",
                insertText = "sin()",
                cursorOffset = -1,
                category = KeyCategory.FUNCTION,
                hasAlternate = true,
                alternates = listOf(
                    SciCalcKey("sin⁻¹", "sin⁻¹()", -1, KeyCategory.FUNCTION),
                    SciCalcKey("sinh", "sinh()", -1, KeyCategory.FUNCTION)
                )
            ),
            SciCalcKey(
                label = "cos",
                insertText = "cos()",
                cursorOffset = -1,
                category = KeyCategory.FUNCTION,
                hasAlternate = true,
                alternates = listOf(
                    SciCalcKey("cos⁻¹", "cos⁻¹()", -1, KeyCategory.FUNCTION),
                    SciCalcKey("cosh", "cosh()", -1, KeyCategory.FUNCTION)
                )
            ),
            SciCalcKey(
                label = "tan",
                insertText = "tan()",
                cursorOffset = -1,
                category = KeyCategory.FUNCTION,
                hasAlternate = true,
                alternates = listOf(
                    SciCalcKey("tan⁻¹", "tan⁻¹()", -1, KeyCategory.FUNCTION),
                    SciCalcKey("tanh", "tanh()", -1, KeyCategory.FUNCTION)
                )
            ),
            SciCalcKey(
                label = "ln",
                insertText = "ln()",
                cursorOffset = -1,
                category = KeyCategory.FUNCTION,
                hasAlternate = true,
                alternates = listOf(
                    SciCalcKey("log", "log()", -1, KeyCategory.FUNCTION),
                    SciCalcKey("log₂", "log2()", -1, KeyCategory.FUNCTION)
                )
            ),
            SciCalcKey("log", "log()", -1, KeyCategory.FUNCTION)
        ),
        listOf(
            SciCalcKey("sinh", "sinh()", -1, KeyCategory.FUNCTION),
            SciCalcKey("cosh", "cosh()", -1, KeyCategory.FUNCTION),
            SciCalcKey("tanh", "tanh()", -1, KeyCategory.FUNCTION),
            SciCalcKey("eˣ", "e^()", -1, KeyCategory.FUNCTION),
            SciCalcKey("n!", "!", 0, KeyCategory.OPERATOR)
        ),
        listOf(
            SciCalcKey("π", "π", 0, KeyCategory.VARIABLE),
            SciCalcKey("e", "e", 0, KeyCategory.VARIABLE),
            SciCalcKey("∞", "oo", 0, KeyCategory.VARIABLE),
            SciCalcKey("|x|", "| |", -1, KeyCategory.FUNCTION),
            SciCalcKey(",", ", ", 0, KeyCategory.OPERATOR)
        ),
        listOf(
            SciCalcKey("(", "(", 0, KeyCategory.OPERATOR),
            SciCalcKey(")", ")", 0, KeyCategory.OPERATOR),
            SciCalcKey("a", "a", 0, KeyCategory.VARIABLE),
            SciCalcKey("b", "b", 0, KeyCategory.VARIABLE),
            SciCalcKey("c", "c", 0, KeyCategory.VARIABLE)
        ),
        listOf(
            SciCalcKey(
                label = "<",
                insertText = "<",
                cursorOffset = 0,
                category = KeyCategory.OPERATOR,
                hasAlternate = true,
                alternates = listOf(
                    SciCalcKey("≤", "<=", 0, KeyCategory.OPERATOR)
                )
            ),
            SciCalcKey(
                label = ">",
                insertText = ">",
                cursorOffset = 0,
                category = KeyCategory.OPERATOR,
                hasAlternate = true,
                alternates = listOf(
                    SciCalcKey("≥", ">=", 0, KeyCategory.OPERATOR)
                )
            ),
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
            SciCalcKey("sin⁻¹", "sin⁻¹()", -1, KeyCategory.FUNCTION),
            SciCalcKey("cos⁻¹", "cos⁻¹()", -1, KeyCategory.FUNCTION),
            SciCalcKey("tan⁻¹", "tan⁻¹()", -1, KeyCategory.FUNCTION),
            SciCalcKey("exp", "exp()", -1, KeyCategory.FUNCTION),
            SciCalcKey("log", "log()", -1, KeyCategory.FUNCTION)
        ),
        listOf(
            SciCalcKey("√", "√()", -1, KeyCategory.FUNCTION),
            SciCalcKey("∛", "cbrt()", -1, KeyCategory.FUNCTION),
            SciCalcKey("x²", "²", 0, KeyCategory.FUNCTION),
            SciCalcKey("x³", "³", 0, KeyCategory.FUNCTION),
            SciCalcKey("xⁿ", "^()", -1, KeyCategory.FUNCTION)
        ),
        listOf(
            SciCalcKey("(", "(", 0, KeyCategory.OPERATOR),
            SciCalcKey(")", ")", 0, KeyCategory.OPERATOR),
            SciCalcKey("x", "x", 0, KeyCategory.VARIABLE),
            SciCalcKey("y", "y", 0, KeyCategory.VARIABLE),
            SciCalcKey("t", "t", 0, KeyCategory.VARIABLE)
        )
    )

    val greekRows: List<List<SciCalcKey>> = listOf(
        listOf(
            SciCalcKey("α", "α", 0, KeyCategory.VARIABLE),
            SciCalcKey("β", "β", 0, KeyCategory.VARIABLE),
            SciCalcKey("γ", "γ", 0, KeyCategory.VARIABLE),
            SciCalcKey("δ", "δ", 0, KeyCategory.VARIABLE),
            SciCalcKey("ε", "ε", 0, KeyCategory.VARIABLE)
        ),
        listOf(
            SciCalcKey("θ", "θ", 0, KeyCategory.VARIABLE),
            SciCalcKey("λ", "λ", 0, KeyCategory.VARIABLE),
            SciCalcKey("μ", "μ", 0, KeyCategory.VARIABLE),
            SciCalcKey("π", "π", 0, KeyCategory.VARIABLE),
            SciCalcKey("ρ", "ρ", 0, KeyCategory.VARIABLE)
        ),
        listOf(
            SciCalcKey("σ", "σ", 0, KeyCategory.VARIABLE),
            SciCalcKey("τ", "τ", 0, KeyCategory.VARIABLE),
            SciCalcKey("φ", "φ", 0, KeyCategory.VARIABLE),
            SciCalcKey("ω", "ω", 0, KeyCategory.VARIABLE),
            SciCalcKey("Δ", "Δ", 0, KeyCategory.VARIABLE)
        ),
        listOf(
            SciCalcKey("x", "x", 0, KeyCategory.VARIABLE),
            SciCalcKey("y", "y", 0, KeyCategory.VARIABLE),
            SciCalcKey("z", "z", 0, KeyCategory.VARIABLE),
            SciCalcKey("t", "t", 0, KeyCategory.VARIABLE),
            SciCalcKey("k", "k", 0, KeyCategory.VARIABLE)
        ),
        listOf(
            SciCalcKey("1", "1", 0, KeyCategory.NUMBER),
            SciCalcKey("2", "2", 0, KeyCategory.NUMBER),
            SciCalcKey("3", "3", 0, KeyCategory.NUMBER),
            SciCalcKey("+", "+", 0, KeyCategory.OPERATOR),
            SciCalcKey("=", "==", 0, KeyCategory.ACTION)
        )
    )
}
