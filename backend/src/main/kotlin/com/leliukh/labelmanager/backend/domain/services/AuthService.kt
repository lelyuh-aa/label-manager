package com.leliukh.labelmanager.backend.domain.services

import com.leliukh.labelmanager.backend.BackendConfig
import com.leliukh.labelmanager.backend.data.BackendStore
import com.leliukh.labelmanager.backend.data.StoreWriter
import com.leliukh.labelmanager.backend.domain.errors.DomainException
import com.leliukh.labelmanager.backend.domain.ids.IdGenerator
import com.leliukh.labelmanager.backend.domain.models.User
import com.leliukh.labelmanager.backend.domain.validation.validate
import com.leliukh.labelmanager.backend.generation.LabelGenerator
import com.leliukh.labelmanager.backend.security.AccessClaims
import com.leliukh.labelmanager.backend.security.PasswordHasher
import com.leliukh.labelmanager.backend.security.TokenService
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.toLocalDateTime

/**
 * A signed-in account with a fresh token pair.
 *
 * @property user the account
 * @property accessToken bearer token for authorised requests
 * @property accessTokenExpiresAt moment the access token stops being accepted
 * @property refreshToken token that exchanges for the next pair
 */
data class Session(
    val user: User,
    val accessToken: String,
    val accessTokenExpiresAt: Instant,
    val refreshToken: String,
)

/**
 * Registration, sign-in and token management.
 *
 * Registration creates the account together with its generated label in one transaction.
 *
 * @param store persistent state
 * @param clock source of the current time
 * @param config time zone for artist ages
 * @param passwordHasher hashes and verifies passwords
 * @param tokenService issues and verifies tokens
 * @param labelGenerator creates the new account's label
 * @param idGenerator source of account ids
 */
class AuthService(
    private val store: BackendStore,
    private val clock: Clock,
    private val config: BackendConfig,
    private val passwordHasher: PasswordHasher,
    private val tokenService: TokenService,
    private val labelGenerator: LabelGenerator,
    private val idGenerator: IdGenerator,
) {

    private val dummyHash: String by lazy { passwordHasher.hash("not-a-real-password") }

    /**
     * @param email the new account's email
     * @param password the new account's password
     * @return a session of the new account
     * @throws DomainException.ValidationFailed for a malformed email or a bad password length
     * @throws DomainException.EmailTaken when the email is registered already
     */
    suspend fun register(email: String, password: String): Session {
        val normalizedEmail = normalize(email)
        validate {
            email("email", normalizedEmail)
            password("password", password)
        }
        val passwordHash = passwordHasher.hash(password)
        return store.write {
            if (findUserByEmail(normalizedEmail) != null) throw DomainException.EmailTaken()
            val userId = idGenerator.next()
            val today = clock.now().toLocalDateTime(config.timeZone).date
            val takenNames = allLabels().map { it.name }.toSet()
            val (label, artists) = labelGenerator.generate(userId, takenNames, today)
            val user = User(userId, normalizedEmail, passwordHash, label.id, clock.now())
            saveLabel(label)
            artists.forEach(::saveArtist)
            saveUser(user)
            openSession(user)
        }
    }

    /**
     * @param email the account's email
     * @param password the account's password
     * @return a session of the account
     * @throws DomainException.InvalidCredentials when no account matches
     */
    suspend fun login(email: String, password: String): Session {
        val user = store.read { findUserByEmail(normalize(email)) }
        val matches = passwordHasher.verify(password, user?.passwordHash ?: dummyHash)
        if (user == null || !matches) throw DomainException.InvalidCredentials()
        return store.write { openSession(user) }
    }

    /**
     * Exchanges a refresh token for a new pair. The old refresh token stops working.
     *
     * @param refreshToken a refresh token from an earlier session
     * @return a new session
     * @throws DomainException.Unauthorized when the token is unknown, used or expired
     */
    suspend fun refresh(refreshToken: String): Session = store.write {
        val hash = tokenService.hashRefreshToken(refreshToken)
        val record =
            findRefreshToken(hash)?.takeIf { clock.now() < it.expiresAt }
                ?: throw DomainException.Unauthorized("The refresh token is invalid")
        deleteRefreshToken(hash)
        val user =
            findUser(record.userId)
                ?: throw DomainException.Unauthorized("The account no longer exists")
        openSession(user)
    }

    /**
     * Revokes a refresh token. Unknown tokens are ignored.
     *
     * @param refreshToken the token to revoke
     */
    suspend fun logout(refreshToken: String) {
        store.write { deleteRefreshToken(tokenService.hashRefreshToken(refreshToken)) }
    }

    /**
     * @param accessToken token from an `Authorization` header
     * @return the token's claims
     * @throws DomainException.Unauthorized when the token is invalid or its account is gone
     */
    suspend fun authenticate(accessToken: String): AccessClaims {
        val claims = tokenService.verifyAccessToken(accessToken)
        store.read { findUser(claims.userId) }
            ?: throw DomainException.Unauthorized("The account no longer exists")
        return claims
    }

    /**
     * @param userId id of a signed-in account
     * @return the account
     * @throws DomainException.Unauthorized when the account is gone
     */
    suspend fun user(userId: String): User =
        store.read { findUser(userId) }
            ?: throw DomainException.Unauthorized("The account no longer exists")

    private fun StoreWriter.openSession(user: User): Session {
        val access = tokenService.issueAccessToken(user.id, user.labelId)
        val refresh = tokenService.issueRefreshToken(user.id)
        saveRefreshToken(refresh.record)
        return Session(user, access.token, access.expiresAt, refresh.token)
    }

    private fun normalize(email: String): String = email.trim().lowercase()
}
