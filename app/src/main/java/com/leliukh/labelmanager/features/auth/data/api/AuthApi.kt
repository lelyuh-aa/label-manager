package com.leliukh.labelmanager.features.auth.data.api

import com.leliukh.labelmanager.contract.dto.auth.AuthResponseDto
import com.leliukh.labelmanager.contract.dto.auth.LoginRequestDto
import com.leliukh.labelmanager.contract.dto.auth.RefreshRequestDto
import com.leliukh.labelmanager.contract.dto.auth.RegisterRequestDto
import com.leliukh.labelmanager.contract.routes.ApiRoutes
import com.leliukh.labelmanager.core.network.data.apiRequest
import com.leliukh.labelmanager.core.network.data.withoutAuth
import com.leliukh.labelmanager.core.network.domain.ApiResult
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The `auth` endpoints of the API. They bypass the bearer auth plugin: a 401 here means wrong
 * credentials and must reach the caller as is.
 *
 * @param client the shared API client
 */
@Singleton
class AuthApi @Inject constructor(
    private val client: HttpClient,
) {

    /**
     * @param email the new account's email
     * @param password the new account's password
     */
    suspend fun register(email: String, password: String): ApiResult<AuthResponseDto> =
        apiRequest {
            client.post(ApiRoutes.REGISTER) {
                withoutAuth()
                setBody(RegisterRequestDto(email, password))
            }
        }

    /**
     * @param email the account's email
     * @param password the account's password
     */
    suspend fun login(email: String, password: String): ApiResult<AuthResponseDto> =
        apiRequest {
            client.post(ApiRoutes.LOGIN) {
                withoutAuth()
                setBody(LoginRequestDto(email, password))
            }
        }

    /**
     * @param refreshToken the refresh token to revoke
     */
    suspend fun logout(refreshToken: String): ApiResult<Unit> =
        apiRequest {
            client.post(ApiRoutes.LOGOUT) {
                withoutAuth()
                setBody(RefreshRequestDto(refreshToken))
            }
        }
}
