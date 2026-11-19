package com.leliukh.labelmanager.core.network.data

import com.leliukh.labelmanager.backend.LabelBackendFactory
import com.leliukh.labelmanager.contract.dto.auth.AuthResponseDto
import com.leliukh.labelmanager.contract.dto.auth.RegisterRequestDto
import com.leliukh.labelmanager.contract.dto.common.PageDto
import com.leliukh.labelmanager.contract.dto.error.ErrorCodeDto
import com.leliukh.labelmanager.contract.dto.market.ListingDto
import com.leliukh.labelmanager.contract.dto.label.LabelDto
import com.leliukh.labelmanager.contract.routes.ApiQuery
import com.leliukh.labelmanager.contract.routes.ApiRoutes
import com.leliukh.labelmanager.core.network.domain.ApiError
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.session.data.models.toSession
import com.leliukh.labelmanager.core.session.domain.models.AuthSession
import com.leliukh.labelmanager.core.session.domain.repository.SessionRepository
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class BackendEngineTest {

    @Test
    fun `the app client talks to the in-process backend end to end`() = runTest {
        val sessions = InMemorySessions()
        val client = client(sessions)

        val registered = apiRequest<AuthResponseDto> {
            client.post(ApiRoutes.REGISTER) {
                setBody(RegisterRequestDto("producer@example.com", "secret123"))
            }
        }
        sessions.save((registered as ApiResult.Success).data.toSession())
        val label = apiRequest<LabelDto> { client.get(ApiRoutes.LABEL) }

        val artists = (label as ApiResult.Success).data.artists
        assertEquals(16, artists.size)
        assertTrue(label.data.budget in 2_000_000L..5_000_000L)
    }

    @Test
    fun `query parameters and error bodies cross the engine unchanged`() = runTest {
        val sessions = InMemorySessions()
        val client = client(sessions)
        val registered = apiRequest<AuthResponseDto> {
            client.post(ApiRoutes.REGISTER) {
                setBody(RegisterRequestDto("producer@example.com", "secret123"))
            }
        }
        sessions.save((registered as ApiResult.Success).data.toSession())

        val page = apiRequest<PageDto<ListingDto>> {
            client.get(ApiRoutes.LISTINGS) { parameter(ApiQuery.LIMIT, 5) }
        }
        val duplicate = apiRequest<AuthResponseDto> {
            client.post(ApiRoutes.REGISTER) {
                setBody(RegisterRequestDto("producer@example.com", "secret123"))
            }
        }

        assertTrue((page as ApiResult.Success).data.items.size <= 5)
        val error = (duplicate as ApiResult.Failure).error as ApiError.Server
        assertEquals(ErrorCodeDto.EMAIL_TAKEN, error.code)
    }

    private fun client(sessions: SessionRepository) = createApiClient(
        engine = createBackendEngine(
            backend = LabelBackendFactory.inMemory(snapshotFile = null, random = Random(7)),
            latencyMs = 0L..0L,
        ),
        endpoint = ApiEndpoint("http://label-manager.mock/"),
        sessionRepository = sessions,
        isLoggingEnabled = false,
    )

    private class InMemorySessions : SessionRepository {

        private val state = MutableStateFlow<AuthSession?>(null)

        override val session = state

        override suspend fun current(): AuthSession? = state.value

        override suspend fun save(session: AuthSession) {
            state.value = session
        }

        override suspend fun clear() {
            state.value = null
        }
    }
}
