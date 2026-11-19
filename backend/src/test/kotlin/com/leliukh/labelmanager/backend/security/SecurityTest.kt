package com.leliukh.labelmanager.backend.security

import com.leliukh.labelmanager.backend.BackendConfig
import com.leliukh.labelmanager.backend.domain.errors.DomainException
import com.leliukh.labelmanager.backend.testing.TestClock
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.io.encoding.Base64
import kotlin.time.Duration.Companion.minutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityTest {

    @Test
    fun `a password verifies against its own hash only`() {
        val hasher = PasswordHasher(iterations = 1_000)

        val hash = hasher.hash("correct horse")

        assertTrue(hash.startsWith("pbkdf2-sha256$1000$"))
        assertTrue(hasher.verify("correct horse", hash))
        assertFalse(hasher.verify("wrong horse", hash))
        assertNotEquals(hash, hasher.hash("correct horse"))
    }

    @Test
    fun `a SHA-1 hash from an older device still verifies`() {
        val salt = ByteArray(16) { it.toByte() }
        val spec = PBEKeySpec("secret-pass".toCharArray(), salt, 500, 256)
        val derived =
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded
        val encoded = "pbkdf2-sha1$500$${Base64.encode(salt)}$${Base64.encode(derived)}"

        assertTrue(PasswordHasher(iterations = 1_000).verify("secret-pass", encoded))
    }

    @Test
    fun `malformed hashes never verify`() {
        val hasher = PasswordHasher(iterations = 1_000)

        listOf("", "plain", "pbkdf2-md5\$1\$AA\$AA", "pbkdf2-sha256\$x\$AA\$AA").forEach {
            assertFalse(hasher.verify("anything", it))
        }
    }

    @Test
    fun `an access token carries the ids until it expires`() {
        val clock = TestClock()
        val tokens = TokenService(BackendConfig(), clock)

        val token = tokens.issueAccessToken("user-1", "label-1")

        val claims = tokens.verifyAccessToken(token.token)
        assertEquals("user-1", claims.userId)
        assertEquals("label-1", claims.labelId)
        assertEquals(clock.now + 15.minutes, token.expiresAt)

        clock.advanceBy(15.minutes)
        assertThrows(DomainException.Unauthorized::class.java) {
            tokens.verifyAccessToken(token.token)
        }
    }

    @Test
    fun `forged and foreign tokens are rejected`() {
        val clock = TestClock()
        val tokens = TokenService(BackendConfig(), clock)
        val foreign = TokenService(BackendConfig(tokenSecret = "another-secret"), clock)
        val token = tokens.issueAccessToken("user-1", "label-1").token
        val (header, payload, signature) = token.split(".")
        val forgedPayload =
            Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
                .encode("""{"sub":"user-2","tid":"label-2","exp":9999999999}""".encodeToByteArray())

        listOf(
                "$header.$forgedPayload.$signature",
                foreign.issueAccessToken("user-1", "label-1").token,
                "$header.$payload",
                "garbage",
            )
            .forEach {
                assertThrows(DomainException.Unauthorized::class.java) {
                    tokens.verifyAccessToken(it)
                }
            }
    }

    @Test
    fun `refresh tokens are random and stored only as a hash`() {
        val tokens = TokenService(BackendConfig(), TestClock())

        val first = tokens.issueRefreshToken("user-1")
        val second = tokens.issueRefreshToken("user-1")

        assertNotEquals(first.token, second.token)
        assertEquals(tokens.hashRefreshToken(first.token), first.record.tokenHash)
        assertFalse(first.record.tokenHash.contains(first.token))
    }
}
