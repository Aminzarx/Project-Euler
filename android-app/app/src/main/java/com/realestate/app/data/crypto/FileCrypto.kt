package com.realestate.app.data.crypto

import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Password-based encryption for files meant to move between two independent installs of this app,
 * with no server in between to broker a key. That constraint is what rules out Android's
 * Keystore/EncryptedFile: a Keystore key is generated inside — and never leaves — the device's
 * secure hardware, by design. It can encrypt data this device will later decrypt itself, but it
 * cannot produce a file a *second* phone can open, because that phone has no way to ever obtain
 * the same key. The only thing two independent devices can agree on without a server is something
 * a human carries between them — a password/PIN — so a password-derived key is not a fallback
 * here, it is the only approach that actually satisfies the requirement.
 *
 * Algorithm choices:
 * - AES-256-GCM: authenticated encryption — a wrong password or a single flipped byte in transit
 *   both fail loudly on decrypt (via the GCM tag check) instead of silently returning garbage that
 *   looks like it might be valid data.
 * - PBKDF2WithHmacSHA256, 210,000 iterations: OWASP's 2023 minimum recommendation for PBKDF2-SHA256.
 *   Deliberately slow, so guessing a short PIN by brute force costs real time even with the salt
 *   captured (it's stored alongside the ciphertext, same as the IV — neither is secret, only the
 *   password is). PBKDF2 is used over a memory-hard KDF like Argon2 because it's already present in
 *   both the JVM and Android's crypto providers — no new dependency for what has to work
 *   identically on whatever device opens the file.
 *
 * Deliberately free of any android.content.Context — this is pure JVM code (javax.crypto is part
 * of the standard library on both the JVM and Android), so it runs and is tested exactly the same
 * way in a local unit test as it does on-device; nothing here needs an emulator to verify.
 */
object FileCrypto {
    private val MAGIC = byteArrayOf('R'.code.toByte(), 'E'.code.toByte(), 'C'.code.toByte(), 'X'.code.toByte())
    private const val FORMAT_VERSION: Byte = 1
    private const val PBKDF2_ITERATIONS = 210_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16
    private const val GCM_IV_LENGTH_BYTES = 12
    private const val GCM_TAG_LENGTH_BITS = 128
    private val HEADER_LENGTH = MAGIC.size + 1 + SALT_LENGTH_BYTES + GCM_IV_LENGTH_BYTES

    class DecryptionException(message: String, cause: Throwable? = null) : Exception(message, cause)

    /**
     * Encrypts [plaintext] under a key derived from [password]. Output layout: magic (4 bytes) |
     * format version (1 byte) | salt (16 bytes) | IV (12 bytes) | ciphertext+GCM tag. The salt and
     * IV are plain, unencrypted prefix bytes — that's correct; they're inputs to key derivation and
     * the cipher, not secrets themselves, and both must travel with the file for decrypt to be
     * possible at all. A fresh random salt and IV are generated on every call, even for the same
     * password, so two exports of the same data never produce identical ciphertext.
     */
    fun encrypt(plaintext: ByteArray, password: CharArray): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH_BYTES).also { random.nextBytes(it) }
        val iv = ByteArray(GCM_IV_LENGTH_BYTES).also { random.nextBytes(it) }
        val key = deriveKey(password, salt)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        val ciphertext = cipher.doFinal(plaintext)

        return MAGIC + byteArrayOf(FORMAT_VERSION) + salt + iv + ciphertext
    }

    /**
     * Reverses [encrypt]. Throws [DecryptionException] — never lets a raw crypto exception or an
     * ArrayIndexOutOfBounds from a truncated file escape — for every failure mode: not this app's
     * file format, an unsupported format version, or (indistinguishably, by design — see the class
     * doc) a wrong password vs. a tampered file.
     */
    fun decrypt(envelope: ByteArray, password: CharArray): ByteArray {
        if (envelope.size < HEADER_LENGTH) {
            throw DecryptionException("فایل خیلی کوچک یا ناقص است")
        }
        if (!envelope.copyOfRange(0, MAGIC.size).contentEquals(MAGIC)) {
            throw DecryptionException("این فایل یک بسته معتبر از این اپلیکیشن نیست")
        }
        val version = envelope[MAGIC.size]
        if (version != FORMAT_VERSION) {
            throw DecryptionException("نسخه فرمت این فایل با این نسخه اپ سازگار نیست")
        }

        var offset = MAGIC.size + 1
        val salt = envelope.copyOfRange(offset, offset + SALT_LENGTH_BYTES)
        offset += SALT_LENGTH_BYTES
        val iv = envelope.copyOfRange(offset, offset + GCM_IV_LENGTH_BYTES)
        offset += GCM_IV_LENGTH_BYTES
        val ciphertext = envelope.copyOfRange(offset, envelope.size)

        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))

        return try {
            cipher.doFinal(ciphertext)
        } catch (e: AEADBadTagException) {
            // GCM's authentication tag failed to verify. A wrong password derives a wrong key,
            // which fails this exact same check as a byte flipped by tampering — there is no way
            // to tell those two apart, and that's the point: an attacker trying passwords can't
            // learn "close, but not quite" from a different error.
            throw DecryptionException("رمز عبور اشتباه است یا فایل دستکاری شده است", e)
        } catch (e: Exception) {
            throw DecryptionException("امکان رمزگشایی این فایل وجود ندارد", e)
        }
    }

    private fun deriveKey(password: CharArray, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password, salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val keyBytes = try {
            factory.generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
        return SecretKeySpec(keyBytes, "AES")
    }
}
