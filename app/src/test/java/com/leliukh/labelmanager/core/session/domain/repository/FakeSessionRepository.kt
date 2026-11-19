package com.leliukh.labelmanager.core.session.domain.repository

import com.leliukh.labelmanager.core.session.domain.models.AuthSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Instant

/**
 * [SessionRepository] kept in memory.
 *
 * @param initial the session to start with
 */
class FakeSessionRepository(initial: AuthSession? = null) : SessionRepository {

    private val mutableState = MutableStateFlow(initial)

    /** The stored session, for assertions. */
    val state: StateFlow<AuthSession?> = mutableState

    override val session = mutableState

    override suspend fun current(): AuthSession? = mutableState.value

    override suspend fun save(session: AuthSession) {
        mutableState.value = session
    }

    override suspend fun clear() {
        mutableState.value = null
    }
}

/**
 * @param access the access token
 * @param refresh the refresh token
 */
fun testSession(access: String = "access", refresh: String = "refresh") = AuthSession(
    userId = "user-1",
    email = "manager@rheinvalley.fm",
    labelId = "label-1",
    accessToken = access,
    accessTokenExpiresAt = Instant.parse("2026-10-26T12:00:00Z"),
    refreshToken = refresh,
)
