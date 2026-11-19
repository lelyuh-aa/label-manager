package com.leliukh.labelmanager.backend

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlinx.datetime.TimeZone

/**
 * Tunable settings of the backend. The defaults suit the in-app mock and local development.
 *
 * @property tokenSecret HMAC key that signs access tokens; must be private in production
 * @property accessTokenTtl how long an access token is accepted
 * @property refreshTokenTtl how long a refresh token can be exchanged
 * @property passwordHashIterations PBKDF2 iterations per password hash
 * @property timeZone time zone that defines "today" for artist ages
 * @property simulation settings of the rival labels on the market
 */
data class BackendConfig(
    val tokenSecret: String = DEVELOPMENT_TOKEN_SECRET,
    val accessTokenTtl: Duration = 15.minutes,
    val refreshTokenTtl: Duration = 30.days,
    val passwordHashIterations: Int = 120_000,
    val timeZone: TimeZone = TimeZone.UTC,
    val simulation: SimulationConfig = SimulationConfig(),
) {

    companion object {
        const val DEVELOPMENT_TOKEN_SECRET: String =
            "label-manager-development-secret-do-not-use-in-production"
    }
}

/**
 * Settings of the rival labels that keep the market alive.
 *
 * @property isEnabled whether rival labels exist and trade at all
 * @property rivalLabelCount number of rival labels created on first start
 * @property initialListings open listings rival labels start with
 * @property historicalDeals past deals between rival labels created on first start, so
 *   artists already have a deal history
 * @property tick simulated time between two rounds of rival activity
 * @property maxCatchUpTicks most rounds replayed at once after a long pause; older time is skipped
 * @property interestDelay how long a user's listing is on the market before rivals consider it
 * @property userListingChance chance per tick that a rival buys a user's listing priced at or below
 *   the market value; pricier listings get a fraction of it
 */
data class SimulationConfig(
    val isEnabled: Boolean = true,
    val rivalLabelCount: Int = 24,
    val initialListings: Int = 30,
    val historicalDeals: Int = 40,
    val tick: Duration = 2.minutes,
    val maxCatchUpTicks: Int = 90,
    val interestDelay: Duration = 2.minutes,
    val userListingChance: Double = 0.35,
)
