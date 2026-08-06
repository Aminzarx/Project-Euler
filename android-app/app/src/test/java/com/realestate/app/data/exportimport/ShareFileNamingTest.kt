package com.realestate.app.data.exportimport

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareFileNamingTest {
    private val fixedTime = 1_722_000_000_000L // an arbitrary fixed instant for determinism

    @Test
    fun `blank exporter name falls back to the generic prefix`() {
        val name = buildShareFileName("", fixedTime)
        assertTrue(name.startsWith("Properties_"))
        assertTrue(name.endsWith(".enc"))
    }

    @Test
    fun `a Persian agency name is preserved, not stripped to the fallback`() {
        val name = buildShareFileName("املاک آریا", fixedTime)
        assertTrue(name.startsWith("املاک_آریا_"))
    }

    @Test
    fun `path separators and filesystem-reserved characters are stripped`() {
        val name = buildShareFileName("""Office/Name:*?"<>|""", fixedTime)
        assertFalse(name.contains("/"))
        assertFalse(name.contains(":"))
        assertFalse(name.contains("*"))
        assertFalse(name.contains("?"))
        assertFalse(name.contains("\""))
        assertFalse(name.contains("<"))
        assertFalse(name.contains(">"))
        assertFalse(name.contains("|"))
    }

    @Test
    fun `whitespace collapses to a single underscore`() {
        val name = buildShareFileName("My   Office   Name", fixedTime)
        assertTrue(name.startsWith("My_Office_Name_"))
    }

    @Test
    fun `the date segment is always yyyy-MM-dd`() {
        val name = buildShareFileName("Test", fixedTime)
        assertTrue(Regex("""Test_\d{4}-\d{2}-\d{2}\.enc""").matches(name))
    }
}

private fun assertTrue(condition: Boolean) = org.junit.Assert.assertTrue(condition)
