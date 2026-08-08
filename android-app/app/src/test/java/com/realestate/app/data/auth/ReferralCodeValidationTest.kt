package com.realestate.app.data.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReferralCodeValidationTest {

    @Test
    fun `a well-formed 8-character uppercase code is valid`() {
        assertTrue(isValidReferralCodeFormat("ABCD1234"))
    }

    @Test
    fun `lowercase input is normalized to uppercase and becomes valid`() {
        val normalized = normalizeReferralCodeInput("abcd1234")
        assertEquals("ABCD1234", normalized)
        assertTrue(isValidReferralCodeFormat(normalized))
    }

    @Test
    fun `leading and trailing spaces are trimmed`() {
        assertEquals("ABCD1234", normalizeReferralCodeInput("  ABCD1234  "))
    }

    @Test
    fun `a code with an internal space fails format validation`() {
        assertFalse(isValidReferralCodeFormat(normalizeReferralCodeInput("ABCD 1234")))
    }

    @Test
    fun `a code shorter than 8 characters fails format validation`() {
        assertFalse(isValidReferralCodeFormat("ABCD123"))
    }

    @Test
    fun `a code longer than 8 characters fails format validation`() {
        assertFalse(isValidReferralCodeFormat("ABCD12345"))
    }

    @Test
    fun `a code with invalid characters fails format validation`() {
        assertFalse(isValidReferralCodeFormat("ABCD-234"))
        assertFalse(isValidReferralCodeFormat("ABCD#234"))
    }

    @Test
    fun `an empty code fails format validation`() {
        assertFalse(isValidReferralCodeFormat(normalizeReferralCodeInput("")))
    }
}
