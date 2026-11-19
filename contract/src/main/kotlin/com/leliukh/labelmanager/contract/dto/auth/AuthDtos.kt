package com.leliukh.labelmanager.contract.dto.auth

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Body of [com.leliukh.labelmanager.contract.routes.ApiRoutes.REGISTER].
 *
 * @property email the account's unique email, case-insensitive
 * @property password plain-text password, see
 * [com.leliukh.labelmanager.contract.rules.ContractLimits] for its length
 */
@Serializable
public data class RegisterRequestDto(
    val email: String,
    val password: String,
)

/**
 * Body of [com.leliukh.labelmanager.contract.routes.ApiRoutes.LOGIN].
 *
 * @property email the account's email
 * @property password the account's password
 */
@Serializable
public data class LoginRequestDto(
    val email: String,
    val password: String,
)

/**
 * Body of [com.leliukh.labelmanager.contract.routes.ApiRoutes.REFRESH] and
 * [com.leliukh.labelmanager.contract.routes.ApiRoutes.LOGOUT].
 *
 * @property refreshToken the refresh token issued with the last [AuthResponseDto]
 */
@Serializable
public data class RefreshRequestDto(
    val refreshToken: String,
)

/**
 * Tokens issued after a successful registration, login or refresh.
 *
 * A refresh rotates the refresh token: the old one stops working.
 *
 * @property accessToken short-lived bearer token for every authorised request
 * @property accessTokenExpiresAt moment the access token stops being accepted
 * @property refreshToken long-lived token that exchanges for a new pair
 * @property user the signed-in account
 */
@Serializable
public data class AuthResponseDto(
    val accessToken: String,
    val accessTokenExpiresAt: Instant,
    val refreshToken: String,
    val user: UserDto,
)

/**
 * A user account. Each account owns exactly one label.
 *
 * @property id unique account id
 * @property email the account's email
 * @property labelId id of the label the account owns
 */
@Serializable
public data class UserDto(
    val id: String,
    val email: String,
    val labelId: String,
)
