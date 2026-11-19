package com.leliukh.labelmanager.backend

import com.leliukh.labelmanager.backend.api.LabelBackend
import com.leliukh.labelmanager.backend.api.endpoints.EndpointServices
import com.leliukh.labelmanager.backend.api.endpoints.apiRouter
import com.leliukh.labelmanager.backend.data.BackendStore
import com.leliukh.labelmanager.backend.data.memory.InMemoryBackendStore
import com.leliukh.labelmanager.backend.domain.ids.IdGenerator
import com.leliukh.labelmanager.backend.domain.services.ActivityService
import com.leliukh.labelmanager.backend.domain.services.AuthService
import com.leliukh.labelmanager.backend.domain.services.MarketService
import com.leliukh.labelmanager.backend.domain.services.ArtistService
import com.leliukh.labelmanager.backend.domain.services.LabelService
import com.leliukh.labelmanager.backend.domain.services.DealExecutor
import com.leliukh.labelmanager.backend.generation.LabelGenerator
import com.leliukh.labelmanager.backend.security.PasswordHasher
import com.leliukh.labelmanager.backend.security.TokenService
import com.leliukh.labelmanager.backend.simulation.RivalLabelsSeeder
import com.leliukh.labelmanager.backend.simulation.RivalMarketSimulator
import java.io.File
import kotlin.random.Random
import kotlin.time.Clock
import kotlinx.datetime.toLocalDateTime

/** Builds a ready-to-use [LabelBackend]. */
object LabelBackendFactory {

    /**
     * In-process backend for the app's mock flavor. The state survives restarts when a snapshot
     * file is given. Nothing is read from disk until the first request.
     *
     * @param snapshotFile where to persist the state; null keeps it in memory only
     * @param clock source of the current time
     * @param random source of randomness for generated data and value increases
     * @param config tunable settings
     * @return the backend
     */
    fun inMemory(
        snapshotFile: File?,
        clock: Clock = Clock.System,
        random: Random = Random.Default,
        config: BackendConfig = BackendConfig(),
    ): LabelBackend = create(InMemoryBackendStore(snapshotFile), clock, random, config)

    /**
     * Backend over any [BackendStore], e.g. a database on the server.
     *
     * @param store persistent state
     * @param clock source of the current time
     * @param random source of randomness for generated data and value increases
     * @param config tunable settings
     * @return the backend
     */
    fun create(
        store: BackendStore,
        clock: Clock = Clock.System,
        random: Random = Random.Default,
        config: BackendConfig = BackendConfig(),
    ): LabelBackend {
        val idGenerator = IdGenerator(random)
        val labelGenerator = LabelGenerator(random, idGenerator)
        val executor = DealExecutor(idGenerator)
        val auth =
            AuthService(
                store = store,
                clock = clock,
                config = config,
                passwordHasher = PasswordHasher(config.passwordHashIterations),
                tokenService = TokenService(config, clock),
                labelGenerator = labelGenerator,
                idGenerator = idGenerator,
            )
        val services =
            EndpointServices(
                auth = auth,
                labels = LabelService(store),
                artists = ArtistService(store),
                market = MarketService(store, clock, executor),
                activity = ActivityService(store),
                today = { clock.now().toLocalDateTime(config.timeZone).date },
            )
        val simulator =
            RivalMarketSimulator(
                store = store,
                clock = clock,
                random = random,
                executor = executor,
                seeder =
                    RivalLabelsSeeder(
                        config.simulation,
                        random,
                        labelGenerator,
                        executor,
                        config.timeZone,
                    ),
                config = config.simulation,
            )
        return LabelBackend(
            router = apiRouter(services),
            authenticator = auth::authenticate,
            simulator = simulator,
        )
    }
}
