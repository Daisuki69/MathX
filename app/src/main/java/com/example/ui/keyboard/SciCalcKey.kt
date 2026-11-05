package com.example.ui.keyboard

enum class KeyCategory {
    NUMBER,
    OPERATOR,
    VARIABLE,
    FUNCTION,
    ACTION
}

enum class KeyboardTab(val label: String) {
    BASIC("123"),
    FUNCTIONS("f(x)"),
    CALCULUS("d/dx"),
    GREEK("αβγ")
}

data class SciCalcKey(
    val label: String,
    val insertText: String,
    val cursorOffset: Int = 0,
    val category: KeyCategory = KeyCategory.VARIABLE,
    val isWide: Boolean = false,
    val isSubscriptOrSuperscript: Boolean = false,
    val hasAlternate: Boolean = false,
    val altInsertText: String? = null,
    val altCursorOffset: Int = 0,
    val alternates: List<SciCalcKey> = emptyList()
)
