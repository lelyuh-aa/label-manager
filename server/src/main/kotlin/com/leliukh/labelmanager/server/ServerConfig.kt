package com.leliukh.labelmanager.server

import com.leliukh.labelmanager.backend.BackendConfig
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Settings of the API server, read from environment variables with development defaults.
 *
 * @property port HTTP port, `PORT`
 * @property databaseFile SQLite file, `LABEL_DB_PATH`
 * @property tokenSecret access token signing key, `LABEL_JWT_SECRET`
 * @property isSimulationEnabled whether rival labels trade, `LABEL_SIMULATION` (`on` or `off`)
 * @property simulationInterval how often the server advances the rival labels simulation
 */
data class ServerConfig(
    val port: Int = DEFAULT_PORT,
    val databaseFile: File = File(DEFAULT_DATABASE_PATH),
    val tokenSecret: String = BackendConfig.DEVELOPMENT_TOKEN_SECRET,
    val isSimulationEnabled: Boolean = true,
    val simulationInterval: Duration = 30.seconds,
) {

    /** Whether the token secret is still the public development default. */
    val usesDevelopmentSecret: Boolean
        get() = tokenSecret == BackendConfig.DEVELOPMENT_TOKEN_SECRET

    companion object {
        const val DEFAULT_PORT: Int = 8080
        const val DEFAULT_DATABASE_PATH: String = "data/label-manager.db"

        /**
         * @param environment variables to read, the process environment by default
         * @return the configuration; missing or invalid values fall back to the defaults
         */
        fun fromEnvironment(environment: Map<String, String> = System.getenv()): ServerConfig =
            ServerConfig(
                port = environment["PORT"]?.toIntOrNull() ?: DEFAULT_PORT,
                databaseFile = File(environment["LABEL_DB_PATH"] ?: DEFAULT_DATABASE_PATH),
                tokenSecret =
                    environment["LABEL_JWT_SECRET"]?.takeIf(String::isNotBlank)
                        ?: BackendConfig.DEVELOPMENT_TOKEN_SECRET,
                isSimulationEnabled = environment["LABEL_SIMULATION"]?.lowercase() != "off",
            )
    }
}
