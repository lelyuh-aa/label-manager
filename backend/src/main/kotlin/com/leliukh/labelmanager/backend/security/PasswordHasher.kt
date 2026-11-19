package com.leliukh.labelmanager.backend.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.io.encoding.Base64

/**
 * Hashes passwords with salted PBKDF2.
 *
 * The result is self-describing, `pbkdf2-<algorithm>$<iterations>$<salt>$<hash>`, so hashes made
 * with other settings keep verifying. HMAC-SHA256 is used where the platform has it and HMAC-SHA1
 * otherwise: Android added PBKDF2WithHmacSHA256 only in API 26.
 *
 * @param iterations PBKDF2 iterations for new hashes
 * @param secureRandom source of salts
 */
class PasswordHasher(
    private val iterations: Int,
    private val secureRandom: SecureRandom = SecureRandom(),
) {

    private val algorithm: String = PREFERRED_ALGORITHMS.first(::isAvailable)

    /**
     * @param password plain-text password
     * @return the encoded hash to store
     */
    fun hash(password: String): String {
        val salt = ByteArray(SALT_BYTES).also(secureRandom::nextBytes)
        val hash = derive(password, salt, algorithm, iterations)
        return listOf(
                PREFIX + ALGORITHM_NAMES.getValue(algorithm),
                iterations.toString(),
                Base64.encode(salt),
                Base64.encode(hash),
            )
            .joinToString(SEPARATOR)
    }

    /**
     * @param password plain-text password to check
     * @param encoded a hash produced by [hash]
     * @return whether the password matches; false for a malformed hash
     */
    fun verify(password: String, encoded: String): Boolean {
        val parts = encoded.split(SEPARATOR)
        if (parts.size != PARTS) return false
        val algorithm =
            ALGORITHM_NAMES.entries.firstOrNull { PREFIX + it.value == parts[0] }?.key
                ?: return false
        val iterations = parts[1].toIntOrNull() ?: return false
        val salt = runCatching { Base64.decode(parts[2]) }.getOrNull() ?: return false
        val expected = runCatching { Base64.decode(parts[3]) }.getOrNull() ?: return false
        return MessageDigest.isEqual(expected, derive(password, salt, algorithm, iterations))
    }

    private fun derive(
        password: String,
        salt: ByteArray,
        algorithm: String,
        iterations: Int,
    ): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_BITS)
        return SecretKeyFactory.getInstance(algorithm).generateSecret(spec).encoded
    }

    private fun isAvailable(algorithm: String): Boolean = runCatching {
        SecretKeyFactory.getInstance(algorithm)
    }.isSuccess

    private companion object {
        const val SHA256 = "PBKDF2WithHmacSHA256"
        const val SHA1 = "PBKDF2WithHmacSHA1"
        val PREFERRED_ALGORITHMS = listOf(SHA256, SHA1)
        val ALGORITHM_NAMES = mapOf(SHA256 to "sha256", SHA1 to "sha1")
        const val PREFIX = "pbkdf2-"
        const val SEPARATOR = "$"
        const val PARTS = 4
        const val SALT_BYTES = 16
        const val KEY_BITS = 256
    }
}
