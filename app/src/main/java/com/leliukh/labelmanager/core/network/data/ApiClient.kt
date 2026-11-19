package com.leliukh.labelmanager.core.network.data

import android.util.Log
import com.leliukh.labelmanager.contract.dto.auth.AuthResponseDto
import com.leliukh.labelmanager.contract.dto.auth.RefreshRequestDto
import com.leliukh.labelmanager.contract.dto.error.ErrorCodeDto
import com.leliukh.labelmanager.contract.routes.ApiRoutes
import com.leliukh.labelmanager.contract.serialization.ApiJson
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.network.domain.serverCode
import com.leliukh.labelmanager.core.session.data.models.toSession
import com.leliukh.labelmanager.core.session.domain.repository.SessionRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.AuthCircuitBreaker
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.RefreshTokensParams
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json

private const val REQUEST_TIMEOUT_MS = 20_000L
private const val CONNECT_TIMEOUT_MS = 10_000L
private const val LOG_TAG = "LabelApi"
private const val AUTH_PATH = "${ApiRoutes.PREFIX}/auth/"

/**
 * Where the API lives. The flavor decides: the in-process backend for `mock`, a real server
 * for `remote`.
 *
 * @property baseUrl scheme, host and port, ending with a slash
 */
data class ApiEndpoint(val baseUrl: String)

/**
 * Builds the one [HttpClient] every repository uses.
 *
 * - JSON through the shared [ApiJson] configuration.
 * - Bearer auth: the access token is attached to every request outside `/auth/`. The tokens
 *   are read from [SessionRepository] for every request and never cached in the client, so a
 *   sign-in or sign-out takes effect without telling the client. On a 401 the refresh token is
 *   exchanged once for a new pair, which is stored, and the request is retried. If the server
 *   rejects the refresh token the session is cleared, which signs the user out; a network
 *   failure during the refresh keeps the session.
 * - `expectSuccess = false`: error statuses reach [apiRequest] as responses.
 *
 * @param engine transport; OkHttp for a real server, a MockEngine for the in-process backend
 * @param endpoint where the API lives
 * @param sessionRepository source and sink of the tokens
 * @param isLoggingEnabled whether to log requests to Logcat, without the Authorization header
 */
fun createApiClient(
    engine: HttpClientEngine,
    endpoint: ApiEndpoint,
    sessionRepository: SessionRepository,
    isLoggingEnabled: Boolean,
): HttpClient = HttpClient(engine) {
    expectSuccess = false

    install(ContentNegotiation) { json(ApiJson) }

    install(HttpTimeout) {
        requestTimeoutMillis = REQUEST_TIMEOUT_MS
        connectTimeoutMillis = CONNECT_TIMEOUT_MS
    }

    defaultRequest {
        url(endpoint.baseUrl)
        contentType(ContentType.Application.Json)
    }

    install(Auth) {
        bearer {
            loadTokens {
                sessionRepository.current()?.let { BearerTokens(it.accessToken, it.refreshToken) }
            }
            refreshTokens { refreshSession(sessionRepository) }
            cacheTokens = false
            sendWithoutRequest { request -> AUTH_PATH !in request.url.buildString() }
        }
    }

    if (isLoggingEnabled) {
        install(Logging) {
            level = LogLevel.INFO
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d(LOG_TAG, message)
                }
            }
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }
    }
}

/**
 * Keeps the auth plugin away from this request: no bearer header, and a 401 is returned as
 * is instead of triggering a token refresh and a retry. For sign-in, sign-up and sign-out,
 * where a 401 means wrong credentials, not an expired token.
 */
fun HttpRequestBuilder.withoutAuth() {
    attributes.put(AuthCircuitBreaker, Unit)
}

/**
 * Exchanges the refresh token for a new pair. When the rejected request carried an older access
 * token than the stored one, another request has already refreshed it: the stored pair is
 * returned instead, because the server accepts each refresh token only once.
 */
private suspend fun RefreshTokensParams.refreshSession(
    sessionRepository: SessionRepository,
): BearerTokens? {
    val session = sessionRepository.current() ?: return null
    val sentHeader = response.call.request.headers[HttpHeaders.Authorization]
    if (sentHeader != "Bearer ${session.accessToken}") {
        return BearerTokens(session.accessToken, session.refreshToken)
    }
    val result = apiRequest<AuthResponseDto> {
        client.post(ApiRoutes.REFRESH) {
            markAsRefreshTokenRequest()
            setBody(RefreshRequestDto(refreshToken = session.refreshToken))
        }
    }
    return when (result) {
        is ApiResult.Success -> {
            val refreshed = result.data.toSession()
            sessionRepository.save(refreshed)
            BearerTokens(refreshed.accessToken, refreshed.refreshToken)
        }

        is ApiResult.Failure -> {
            if (result.error.serverCode == ErrorCodeDto.UNAUTHORIZED) sessionRepository.clear()
            null
        }
    }
}
