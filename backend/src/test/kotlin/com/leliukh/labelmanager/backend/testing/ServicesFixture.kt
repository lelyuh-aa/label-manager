package com.leliukh.labelmanager.backend.testing

import com.leliukh.labelmanager.backend.BackendConfig
import com.leliukh.labelmanager.backend.data.memory.InMemoryBackendStore
import com.leliukh.labelmanager.backend.domain.ids.IdGenerator
import com.leliukh.labelmanager.backend.domain.services.ActivityService
import com.leliukh.labelmanager.backend.domain.services.AuthService
import com.leliukh.labelmanager.backend.domain.services.MarketService
import com.leliukh.labelmanager.backend.domain.services.ArtistService
import com.leliukh.labelmanager.backend.domain.services.Session
import com.leliukh.labelmanager.backend.domain.services.LabelService
import com.leliukh.labelmanager.backend.domain.services.DealExecutor
import com.leliukh.labelmanager.backend.generation.LabelGenerator
import com.leliukh.labelmanager.backend.security.PasswordHasher
import com.leliukh.labelmanager.backend.security.TokenService
import kotlin.random.Random
import org.junit.Assert.fail

/**
 * Every domain service wired to one in-memory store, a test clock and a seeded random.
 *
 * @param seed seed of the random source
 */
class ServicesFixture(seed: Int = 11) {

    val clock = TestClock()
    val store = InMemoryBackendStore(snapshotFile = null)
    val random = Random(seed)
    val config = BackendConfig(passwordHashIterations = 1_000)
    val idGenerator = IdGenerator(random)
    val executor = DealExecutor(idGenerator)
    val auth =
        AuthService(
            store = store,
            clock = clock,
            config = config,
            passwordHasher = PasswordHasher(config.passwordHashIterations),
            tokenService = TokenService(config, clock),
            labelGenerator = LabelGenerator(random, idGenerator),
            idGenerator = idGenerator,
        )
    val labels = LabelService(store)
    val artists = ArtistService(store)
    val market = MarketService(store, clock, executor)
    val activity = ActivityService(store)

    /**
     * @param name local part of the email
     * @return a session of a new account
     */
    suspend fun register(name: String): Session = auth.register("$name@example.com", "password1")

    /**
     * @param labelId label id
     * @return id of any artist of the label
     */
    suspend fun anyArtistOf(labelId: String): String = store.read {
        artistsOfLabel(labelId).first().id
    }
}

/**
 * Runs a block that must fail with [T].
 *
 * @param block code expected to throw
 * @return the thrown error
 */
suspend inline fun <reified T : Throwable> expectFailure(block: () -> Unit): T {
    val error = runCatching { block() }.exceptionOrNull()
    if (error !is T) fail("Expected ${T::class.simpleName}, got $error")
    return error as T
}
