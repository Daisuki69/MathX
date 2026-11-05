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

    @Test
    fun testMutateAddsEquationWhenVariablesPresent() {
        val validation = engine.validate("3*x + 5")
        assertTrue(validation.isValid)
        val mutation = engine.mutate(validation)
        assertTrue(mutation.mutatedFormula.contains("=="))
        val ans = engine.evaluateAnswer(mutation.mutatedFormula, mutation.variableValues)
        assertTrue(ans.contains("="))
    }

    @Test
    fun testMutateAddsVariableEquationWhenAllValuesPresent() {
        val validation = engine.validate("2 + 3 * 4")
        assertTrue(validation.isValid)
        val mutation = engine.mutate(validation)
        assertTrue(mutation.mutatedFormula.contains("=="))
        val ans = engine.evaluateAnswer(mutation.mutatedFormula, mutation.variableValues)
        assertTrue(ans.contains("="))
    }

    @Test
    fun testMutateDenominatorIsNeverEmpty() {
        for (i in 0 until 10) {
            val validation = engine.validate("x / 2 + 3 / 4")
            assertTrue(validation.isValid)
            val mutation = engine.mutate(validation)
            assertFalse(mutation.mutatedFormula.contains("/()"))
            assertFalse(mutation.mutatedFormula.contains("/( )"))
            assertFalse(Regex("""/\s*(?=[+=\-<>]|$)""").containsMatchIn(mutation.mutatedFormula))
            assertFalse(mutation.mutatedFormula.endsWith("/"))
        }
    }

    @Test
    fun testEvaluateAnswerHandlesVariableOnBothSides() {
        val ans = engine.evaluateAnswer("4*x - 3 == 2*x + 3")
        assertEquals("x = 3", ans)
    }
}
