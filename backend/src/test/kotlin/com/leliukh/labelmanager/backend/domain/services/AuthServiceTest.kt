package com.leliukh.labelmanager.backend.domain.services

import com.leliukh.labelmanager.backend.BackendConfig
import com.leliukh.labelmanager.backend.data.memory.InMemoryBackendStore
import com.leliukh.labelmanager.backend.domain.errors.DomainException
import com.leliukh.labelmanager.backend.domain.ids.IdGenerator
import com.leliukh.labelmanager.backend.generation.LabelGenerator
import com.leliukh.labelmanager.backend.security.PasswordHasher
import com.leliukh.labelmanager.backend.security.TokenService
import com.leliukh.labelmanager.backend.testing.TestClock
import kotlin.random.Random
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class AuthServiceTest {

    private val clock = TestClock()
    private val store = InMemoryBackendStore(snapshotFile = null)
    private val config = BackendConfig(passwordHashIterations = 1_000)
    private val tokens = TokenService(config, clock)
    private val random = Random(7)
    private val auth =
        AuthService(
            store = store,
            clock = clock,
            config = config,
            passwordHasher = PasswordHasher(config.passwordHashIterations),
            tokenService = tokens,
            labelGenerator = LabelGenerator(random, IdGenerator(random)),
            idGenerator = IdGenerator(random),
        )

    @Test
    fun `registering creates the account and its label`() = runTest {
        val session = auth.register("  Producer@Example.com ", "password1")

        assertEquals("producer@example.com", session.user.email)
        val label = store.read { findLabel(session.user.labelId) }
        assertEquals(session.user.id, label?.ownerUserId)
        assertEquals(3_000_000L, label!!.budget)
        assertEquals(16, store.read { artistsOfLabel(label.id) }.size)
        assertEquals(session.user.id, tokens.verifyAccessToken(session.accessToken).userId)
    }

    @Test
    fun `an email can be registered only once, ignoring case`() = runTest {
        auth.register("producer@example.com", "password1")

        expect<DomainException.EmailTaken> { auth.register("PRODUCER@example.com", "password2") }
        assertEquals(1, store.read { allLabels() }.size)
    }

    @Test
    fun `invalid input reports every bad field`() = runTest {
        val error =
            expect<DomainException.ValidationFailed> { auth.register("not-an-email", "short") }

        assertEquals(setOf("email", "password"), error.fieldErrors.keys)
    }

    @Test
    fun `login checks the password`() = runTest {
        val registered = auth.register("producer@example.com", "password1")

        assertEquals(registered.user.id, auth.login("Producer@Example.com", "password1").user.id)
        expect<DomainException.InvalidCredentials> { auth.login("producer@example.com", "password2") }
        expect<DomainException.InvalidCredentials> { auth.login("nobody@example.com", "password1") }
    }

    @Test
    fun `a refresh token works once and rotates`() = runTest {
        val first = auth.register("producer@example.com", "password1")

        val second = auth.refresh(first.refreshToken)

        assertNotEquals(first.refreshToken, second.refreshToken)
        expect<DomainException.Unauthorized> { auth.refresh(first.refreshToken) }
        assertEquals(first.user.id, auth.refresh(second.refreshToken).user.id)
    }

    @Test
    fun `refresh tokens expire after 30 days and logout revokes them`() = runTest {
        val session = auth.register("producer@example.com", "password1")
        val other = auth.login("producer@example.com", "password1")

        auth.logout(session.refreshToken)
        expect<DomainException.Unauthorized> { auth.refresh(session.refreshToken) }

        clock.advanceBy(30.days + 1.minutes)
        expect<DomainException.Unauthorized> { auth.refresh(other.refreshToken) }
    }

    @Test
    fun `access tokens authenticate until they expire`() = runTest {
        val session = auth.register("producer@example.com", "password1")

        assertEquals(session.user.labelId, auth.authenticate(session.accessToken).labelId)

        clock.advanceBy(16.minutes)
        expect<DomainException.Unauthorized> { auth.authenticate(session.accessToken) }
    }

    private suspend inline fun <reified T : Throwable> expect(block: () -> Unit): T {
        val error = runCatching { block() }.exceptionOrNull()
        if (error !is T) fail("Expected ${T::class.simpleName}, got $error")
        return error as T
    }
}
