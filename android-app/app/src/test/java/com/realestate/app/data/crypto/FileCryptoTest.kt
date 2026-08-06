package com.realestate.app.data.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class FileCryptoTest {

    @Test
    fun `round trip returns the original plaintext`() {
        val plaintext = """{"schemaVersion":1,"properties":[]}""".toByteArray(Charsets.UTF_8)
        val password = "correct-horse-battery-staple".toCharArray()

        val encrypted = FileCrypto.encrypt(plaintext, password)
        val decrypted = FileCrypto.decrypt(encrypted, password)

        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun `two encryptions of the same plaintext produce different bytes`() {
        // A fresh random salt and IV every call is the whole point — proves neither is being
        // accidentally reused or hardcoded.
        val plaintext = "همان داده".toByteArray(Charsets.UTF_8)
        val password = "1234".toCharArray()

        val first = FileCrypto.encrypt(plaintext, password)
        val second = FileCrypto.encrypt(plaintext, password.copyOf())

        assertNotEquals(first.toList(), second.toList())
    }

    @Test
    fun `wrong password fails to decrypt`() {
        val plaintext = "secret".toByteArray(Charsets.UTF_8)
        val encrypted = FileCrypto.encrypt(plaintext, "right-password".toCharArray())

        assertThrows(FileCrypto.DecryptionException::class.java) {
            FileCrypto.decrypt(encrypted, "wrong-password".toCharArray())
        }
    }

    @Test
    fun `tampered ciphertext is rejected instead of returning garbage`() {
        val plaintext = "secret".toByteArray(Charsets.UTF_8)
        val password = "a-password".toCharArray()
        val encrypted = FileCrypto.encrypt(plaintext, password)

        // Flip one bit deep in the ciphertext, past the header (magic+version+salt+iv).
        val tampered = encrypted.copyOf()
        val lastIndex = tampered.size - 1
        tampered[lastIndex] = (tampered[lastIndex].toInt() xor 0x01).toByte()

        assertThrows(FileCrypto.DecryptionException::class.java) {
            FileCrypto.decrypt(tampered, password)
        }
    }

    @Test
    fun `a file from a different app is rejected as not a valid bundle`() {
        val notOurFile = "just some random file content, not encrypted by us".toByteArray(Charsets.UTF_8)

        assertThrows(FileCrypto.DecryptionException::class.java) {
            FileCrypto.decrypt(notOurFile, "any-password".toCharArray())
        }
    }

    @Test
    fun `a truncated header is rejected without throwing an unrelated exception`() {
        val tooShort = byteArrayOf('R'.code.toByte(), 'E'.code.toByte())

        assertThrows(FileCrypto.DecryptionException::class.java) {
            FileCrypto.decrypt(tooShort, "any-password".toCharArray())
        }
    }

    @Test
    fun `empty plaintext still round trips`() {
        val password = "pw".toCharArray()
        val encrypted = FileCrypto.encrypt(ByteArray(0), password)
        val decrypted = FileCrypto.decrypt(encrypted, password)

        assertTrue(decrypted.isEmpty())
    }
}
