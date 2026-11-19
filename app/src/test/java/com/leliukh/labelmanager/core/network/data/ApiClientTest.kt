package com.leliukh.labelmanager.core.network.data

import com.leliukh.labelmanager.contract.dto.auth.AuthResponseDto
import com.leliukh.labelmanager.contract.dto.auth.UserDto
import com.leliukh.labelmanager.contract.dto.error.ErrorCodeDto
import com.leliukh.labelmanager.contract.dto.error.ErrorDto
import com.leliukh.labelmanager.contract.dto.reference.CountryDto
import com.leliukh.labelmanager.contract.routes.ApiRoutes
import com.leliukh.labelmanager.contract.serialization.ApiJson
import com.leliukh.labelmanager.core.network.domain.ApiError
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.session.domain.repository.FakeSessionRepository
import com.leliukh.labelmanager.core.session.domain.repository.SessionRepository
import com.leliukh.labelmanager.core.session.domain.repository.testSession
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

class ApiClientTest {

    @Test
    fun `a successful response is decoded into Success`() = runTest {
        val client = client { respondJson(ListSerializer(CountryDto.serializer()), countries) }

        val result = apiRequest<List<CountryDto>> { client.get(ApiRoutes.COUNTRIES) }

        assertEquals(ApiResult.Success(countries), result)
    }

    @Test
    fun `an error body is mapped to a server error with its code`() = runTest {
        val client = client {
            respondJson(
                ErrorDto.serializer(),
                ErrorDto(ErrorCodeDto.INSUFFICIENT_BUDGET, "Budget too low"),
                HttpStatusCode.Conflict,
            )
        }

        val result = apiRequest<Unit> { client.get(ApiRoutes.LABEL) }

        val error = (result as ApiResult.Failure).error as ApiError.Server
        assertEquals(ErrorCodeDto.INSUFFICIENT_BUDGET, error.code)
        assertEquals(409, error.httpStatus)
    }

    @Test
    fun `a dropped connection is a network error`() = runTest {
        val client = client { throw IOException("connection reset") }

        val result = apiRequest<Unit> { client.get(ApiRoutes.LABEL) }

        assertEquals(ApiResult.Failure(ApiError.Network), result)
    }

    @Test
    fun `an expired access token is refreshed once and the request retried`() = runTest {
        val sessions = FakeSessionRepository(testSession("old-access", "old-refresh"))
        val authorizations = mutableListOf<String?>()
        val client = client(sessions) { request ->
            when (request.url.encodedPath) {
                ApiRoutes.REFRESH -> respondJson(AuthResponseDto.serializer(), refreshed)

                else -> {
                    authorizations += request.headers[HttpHeaders.Authorization]
                    if (request.headers[HttpHeaders.Authorization] == "Bearer old-access") {
                        respondUnauthorized()
                    } else {
                        respondJson(ListSerializer(CountryDto.serializer()), countries)
                    }
                }
            }
        }

        val result = apiRequest<List<CountryDto>> { client.get(ApiRoutes.LABEL) }

        assertTrue(result is ApiResult.Success)
        assertEquals(listOf("Bearer old-access", "Bearer new-access"), authorizations)
        assertEquals("new-refresh", sessions.state.value?.refreshToken)
    }

    @Test
    fun `a request rejected with an outdated token is retried without another refresh`() =
        runTest {
            val sessions = FakeSessionRepository(testSession("old-access", "old-refresh"))
            val authorizations = mutableListOf<String?>()
            var refreshes = 0
            val client = client(sessions) { request ->
                when (request.url.encodedPath) {
                    ApiRoutes.REFRESH -> {
                        refreshes++
                        respondUnauthorized()
                    }

                    else -> {
                        authorizations += request.headers[HttpHeaders.Authorization]
                        if (request.headers[HttpHeaders.Authorization] == "Bearer old-access") {
                            sessions.save(testSession("new-access", "new-refresh"))
                            respondUnauthorized()
                        } else {
                            respondJson(ListSerializer(CountryDto.serializer()), countries)
                        }
                    }
                }
            }

            val result = apiRequest<List<CountryDto>> { client.get(ApiRoutes.LABEL) }

            assertTrue(result is ApiResult.Success)
            assertEquals(0, refreshes)
            assertEquals(listOf("Bearer old-access", "Bearer new-access"), authorizations)
        }

    @Test
    fun `a rejected refresh token signs the user out`() = runTest {
        val sessions = FakeSessionRepository(testSession("old-access", "old-refresh"))
        val client = client(sessions) { respondUnauthorized() }

        val result = apiRequest<Unit> { client.get(ApiRoutes.LABEL) }

        assertTrue(result is ApiResult.Failure)
        assertNull(sessions.state.value)
    }

    private val countries = listOf(CountryDto("DE", "Germany"), CountryDto("FR", "France"))

    private val refreshed = AuthResponseDto(
        accessToken = "new-access",
        accessTokenExpiresAt = Instant.parse("2026-10-25T12:15:00Z"),
        refreshToken = "new-refresh",
        user = UserDto(id = "user-1", email = "producer@example.com", labelId = "label-1"),
    )

    private fun client(
        sessions: SessionRepository = FakeSessionRepository(),
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ) = createApiClient(
        engine = MockEngine(handler),
        endpoint = ApiEndpoint("http://label.test/"),
        sessionRepository = sessions,
        isLoggingEnabled = false,
    )

    private fun <T> MockRequestHandleScope.respondJson(
        serializer: KSerializer<T>,
        body: T,
        status: HttpStatusCode = HttpStatusCode.OK,
    ): HttpResponseData = respond(
        content = ApiJson.encodeToString(serializer, body),
        status = status,
        headers = headersOf(HttpHeaders.ContentType, "application/json"),
    )

    private fun MockRequestHandleScope.respondUnauthorized(): HttpResponseData = respondJson(
        ErrorDto.serializer(),
        ErrorDto(ErrorCodeDto.UNAUTHORIZED, "Token expired"),
        HttpStatusCode.Unauthorized,
    )
}
