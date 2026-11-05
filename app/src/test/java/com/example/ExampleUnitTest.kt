package com.example

import com.example.symja.SymjaEngine
import com.example.ui.MathFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    private val engine = SymjaEngine()

    @Test
    fun testSymjaEngineValidatesTrigIdentity() {
        val validation = engine.validate("sin(x)^2 + cos(x)^2 == 1")
        assertTrue(validation.isValid)
        assertTrue(validation.variables.contains("x"))
        assertTrue(validation.constants.contains("2") || validation.constants.contains("1"))
        assertTrue(validation.operators.contains("=="))
    }

    @Test
    fun testSymjaEngineValidatesSuperscriptInput() {
        val validation = engine.validate("sin(x)² + cos(x)² = 1")
        assertTrue(validation.isValid)
        assertTrue(validation.variables.contains("x"))
    }

    @Test
    fun testMathFormatterConversion() {
        val formatted = MathFormatter.toDisplayString("x^2 + 5 * x == 0")
        assertEquals("x² + 5 · x = 0", formatted)
        val parseable = MathFormatter.toParseableString("x² + 5 · x = 0")
        assertEquals("x^2 + 5 * x = 0", parseable)

        val sqrtFormatted = MathFormatter.toDisplayString("sqrt(x) + abs(x)")
        assertEquals("√(x) + |x|", sqrtFormatted)
        val sqrtParseable = MathFormatter.toParseableString("√(x) + |x|")
        assertEquals("sqrt(x) + abs(x)", sqrtParseable)
    }

    @Test
    fun testSymjaEngineValidatesRadicalsAndAbsoluteValue() {
        val validation = engine.validate("√(x) + |x| == 0")
        assertTrue(validation.isValid)
        assertTrue(validation.variables.contains("x"))
    }

    @Test
    fun testSymjaEngineCatchesSyntaxError() {
        val validation = engine.validate("sin(x + ")
        assertFalse(validation.isValid)
        assertNotNull(validation.errorMessage)
    }

    @Test
    fun testSymjaEngineMutatesTokens() {
        val validation = engine.validate("x^2 + 5*x - 3 == 0")
        assertTrue(validation.isValid)

        val mutation = engine.mutate(validation)
        assertNotNull(mutation.mutatedFormula)
        assertFalse(mutation.mutatedFormula.isBlank())
        assertTrue(mutation.records.isNotEmpty())
    }
}
