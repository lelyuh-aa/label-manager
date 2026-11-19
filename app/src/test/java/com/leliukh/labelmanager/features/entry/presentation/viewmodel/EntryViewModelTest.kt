package com.leliukh.labelmanager.features.entry.presentation.viewmodel

import com.leliukh.labelmanager.core.session.domain.models.AuthSession
import com.leliukh.labelmanager.core.session.domain.repository.SessionRepository
import com.leliukh.labelmanager.core.settings.domain.repository.FakeSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class EntryViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `stays loading until the stored session is read`() = runTest {
        val sessions = MutableSharedFlow<AuthSession?>()
        val viewModel = EntryViewModel(FakeSessionRepository(sessions), FakeSettingsRepository())

        assertEquals(loading, viewModel.viewState.value)

        sessions.emit(session)

        assertEquals(signedIn, viewModel.viewState.value)
    }

    @Test
    fun `a missing session starts signed out`() = runTest {
        val sessions = MutableSharedFlow<AuthSession?>()
        val viewModel = EntryViewModel(FakeSessionRepository(sessions), FakeSettingsRepository())

        sessions.emit(null)

        assertEquals(signedOut, viewModel.viewState.value)
    }

    @Test
    fun `an unreadable session starts signed out instead of crashing`() = runTest {
        val viewModel = EntryViewModel(
            FakeSessionRepository(flow { throw IOException("disk error") }),
            FakeSettingsRepository(),
        )

        assertEquals(signedOut, viewModel.viewState.value)
    }

    private val loading = EntryViewState(isLoading = true, isSignedIn = false)
    private val signedIn = EntryViewState(isLoading = false, isSignedIn = true)
    private val signedOut = EntryViewState(isLoading = false, isSignedIn = false)

    private val session = AuthSession(
        userId = "user-1",
        email = "producer@example.com",
        labelId = "label-1",
        accessToken = "access",
        accessTokenExpiresAt = Instant.parse("2026-10-25T12:00:00Z"),
        refreshToken = "refresh",
    )

    private class FakeSessionRepository(override val session: Flow<AuthSession?>) :
        SessionRepository {

        override suspend fun current(): AuthSession? = null

        override suspend fun save(session: AuthSession) = Unit

        override suspend fun clear() = Unit
    }
}
