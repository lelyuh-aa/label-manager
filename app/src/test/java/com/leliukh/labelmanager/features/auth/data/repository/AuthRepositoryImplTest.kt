package com.leliukh.labelmanager.features.auth.data.repository

import com.leliukh.labelmanager.contract.dto.auth.AuthResponseDto
import com.leliukh.labelmanager.contract.dto.auth.LoginRequestDto
import com.leliukh.labelmanager.contract.dto.auth.UserDto
import com.leliukh.labelmanager.contract.dto.error.ErrorCodeDto
import com.leliukh.labelmanager.contract.dto.error.ErrorDto
import com.leliukh.labelmanager.contract.routes.ApiRoutes
import com.leliukh.labelmanager.contract.serialization.ApiJson
import com.leliukh.labelmanager.core.network.data.ApiEndpoint
import com.leliukh.labelmanager.core.network.data.createApiClient
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.session.domain.repository.FakeSessionRepository
import com.leliukh.labelmanager.features.auth.data.api.AuthApi
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Instant

class AuthRepositoryImplTest {

    @Test
    fun `signing in trims the email and returns the server's session`() = runTest {
        val sentLogins = mutableListOf<LoginRequestDto>()
        val repository = repository { request ->
            val body = request.body.toByteArray().decodeToString()
            sentLogins += ApiJson.decodeFromString(LoginRequestDto.serializer(), body)
            respond(
                content = ApiJson.encodeToString(AuthResponseDto.serializer(), response),
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val result = repository.signIn("  manager@rheinvalley.fm ", "backstage2026")

        assertEquals("manager@rheinvalley.fm", sentLogins.single().email)
        assertEquals("access", (result as ApiResult.Success).data.accessToken)
    }

    @Test
    fun `rejected credentials are sent once, not retried as an expired token`() = runTest {
        var logins = 0
        val repository = repository { request ->
            if (request.url.encodedPath == ApiRoutes.LOGIN) logins++
            respond(
                content = ApiJson.encodeToString(
                    ErrorDto.serializer(),
                    ErrorDto(ErrorCodeDto.INVALID_CREDENTIALS, "Invalid credentials"),
                ),
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        repository.signIn("a@b.cd", "wrong")

        assertEquals(1, logins)
    }

    private fun repository(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): AuthRepositoryImpl {
        val client = createApiClient(
            engine = MockEngine(handler),
            endpoint = ApiEndpoint("http://label.test/"),
            sessionRepository = FakeSessionRepository(),
            isLoggingEnabled = false,
        )
        return AuthRepositoryImpl(AuthApi(client))
    }

    private val response = AuthResponseDto(
        accessToken = "access",
        accessTokenExpiresAt = Instant.parse("2026-10-26T12:00:00Z"),
        refreshToken = "refresh",
        user = UserDto(id = "user-1", email = "manager@rheinvalley.fm", labelId = "label-1"),
    )
}
