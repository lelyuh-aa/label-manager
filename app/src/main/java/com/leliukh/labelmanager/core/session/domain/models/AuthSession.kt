package com.leliukh.labelmanager.core.session.domain.models

import kotlin.time.Instant

/**
 * The signed-in account and its tokens.
 *
 * @property userId id of the account
 * @property email email of the account
 * @property labelId id of the label the account owns
 * @property accessToken bearer token for authorised requests
 * @property accessTokenExpiresAt moment the access token stops being accepted
 * @property refreshToken token that exchanges for a new pair
 */
data class AuthSession(
    val userId: String,
    val email: String,
    val labelId: String,
    val accessToken: String,
    val accessTokenExpiresAt: Instant,
    val refreshToken: String,
)
