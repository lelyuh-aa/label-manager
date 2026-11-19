package com.leliukh.labelmanager.core.session.data.models

import com.leliukh.labelmanager.core.session.domain.models.AuthSession
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Content of the encrypted session file.
 *
 * @property session the stored session; null while signed out
 */
@Serializable
data class SessionFileNO(
    val session: SessionNO? = null,
)

/**
 * Stored form of [AuthSession].
 *
 * @property userId id of the account
 * @property email email of the account
 * @property labelId id of the label the account owns
 * @property accessToken bearer token
 * @property accessTokenExpiresAt expiry of the access token
 * @property refreshToken refresh token
 */
@Serializable
data class SessionNO(
    val userId: String,
    val email: String,
    val labelId: String,
    val accessToken: String,
    val accessTokenExpiresAt: Instant,
    val refreshToken: String,
)

fun SessionNO.toDomain(): AuthSession = AuthSession(
    userId = userId,
    email = email,
    labelId = labelId,
    accessToken = accessToken,
    accessTokenExpiresAt = accessTokenExpiresAt,
    refreshToken = refreshToken,
)

fun AuthSession.toStored(): SessionNO = SessionNO(
    userId = userId,
    email = email,
    labelId = labelId,
    accessToken = accessToken,
    accessTokenExpiresAt = accessTokenExpiresAt,
    refreshToken = refreshToken,
)
