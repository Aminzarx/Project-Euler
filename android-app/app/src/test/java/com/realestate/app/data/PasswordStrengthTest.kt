package com.realestate.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordStrengthTest {

    @Test
    fun `empty password is weak and below minimum`() {
        val result = evaluatePasswordStrength("")
        assertEquals(PasswordStrengthLevel.WEAK, result.level)
        assertFalse(result.meetsMinimum)
        assertEquals(0, result.score)
    }

    @Test
    fun `password shorter than minimum never meets minimum regardless of variety`() {
        val result = evaluatePasswordStrength("Ab1!")
        assertFalse(result.meetsMinimum)
    }

    @Test
    fun `exactly the minimum length with no variety meets minimum but is not strong`() {
        val result = evaluatePasswordStrength("aaaaaaaa") // 8 lowercase-only chars
        assertTrue(result.meetsMinimum)
        assertTrue(result.level == PasswordStrengthLevel.WEAK || result.level == PasswordStrengthLevel.MEDIUM)
    }

    @Test
    fun `long password with full character variety is very strong`() {
        val result = evaluatePasswordStrength("Tr@ns-2026-Estate!")
        assertTrue(result.meetsMinimum)
        assertEquals(PasswordStrengthLevel.VERY_STRONG, result.level)
    }

    @Test
    fun `a long but low-variety password cannot reach very strong on length alone`() {
        val result = evaluatePasswordStrength("aaaaaaaaaaaaaaaaaaaaaaaaaa") // 26 identical lowercase chars
        assertTrue(result.meetsMinimum)
        assertTrue(result.level != PasswordStrengthLevel.VERY_STRONG)
    }

    @Test
    fun `score never leaves the 0-100 range`() {
        val samples = listOf("", "a", "aA1!", "aA1!aA1!aA1!aA1!aA1!aA1!aA1!aA1!")
        samples.forEach { pw ->
            val score = evaluatePasswordStrength(pw).score
            assertTrue("score for '$pw' was $score", score in 0..100)
        }
    }

    @Test
    fun `helper text names the exact character shortfall`() {
        val short = evaluatePasswordStrength("ab1")
        assertTrue(passwordStrengthHelperText(short, "ab1").contains("3"))
    }

    @Test
    fun `helper text confirms a strong password instead of nagging further`() {
        val strong = evaluatePasswordStrength("Tr@ns-2026-Estate!")
        assertEquals("رمز عبور مناسبی است", passwordStrengthHelperText(strong, "Tr@ns-2026-Estate!"))
    }
}
