package com.leliukh.labelmanager.features.label.presentation.viewmodel.roster

import androidx.lifecycle.SavedStateHandle
import com.leliukh.labelmanager.core.network.domain.ApiError
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.session.domain.repository.FakeSessionRepository
import com.leliukh.labelmanager.core.session.domain.repository.testSession
import com.leliukh.labelmanager.core.settings.domain.repository.FakeSettingsRepository
import com.leliukh.labelmanager.features.label.domain.interactor.LabelInteractor
import com.leliukh.labelmanager.features.label.domain.models.CachedLabel
import com.leliukh.labelmanager.features.label.domain.repository.FakeLabelRepository
import com.leliukh.labelmanager.features.label.domain.repository.testArtist
import com.leliukh.labelmanager.features.label.domain.repository.testLabel
import com.leliukh.labelmanager.features.label.presentation.models.StaleDataVO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class RosterViewModelTest {

    private val sessions = FakeSessionRepository(testSession())
    private val labels = FakeLabelRepository()
    private val savedState = SavedStateHandle()
    private val settings = FakeSettingsRepository()
    private val syncedAt = Instant.parse("2026-10-25T14:32:00Z")

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the cached label stays on screen with an offline notice when the refresh fails`() =
        runTest {
            labels.cache.value = mapOf("label-1" to CachedLabel(testLabel(), syncedAt))
            labels.result = ApiResult.Failure(ApiError.Network)

            val state = stateOf(viewModel())

            assertFalse(state.isLoading)
            assertEquals(16, state.artistCount)
            assertEquals(StaleDataVO(isOffline = true, syncedAt = syncedAt), state.staleData)
        }

    @Test
    fun `without a cached label a failed load can be retried`() = runTest {
        labels.result = ApiResult.Failure(ApiError.Network)
        val viewModel = viewModel()
        assertTrue(stateOf(viewModel).isError)

        labels.result = ApiResult.Success(testLabel())
        viewModel.handleUiAction(RosterViewAction.Retry)
        advanceUntilIdle()

        val state = stateOf(viewModel)
        assertFalse(state.isError)
        assertEquals(4, state.sections.size)
    }

    @Test
    fun `search ignores case and accents and reports when nobody matches`() = runTest {
        val artists = listOf(
            testArtist(id = "1", firstName = "Théo", lastName = "Marchand"),
            testArtist(id = "2", firstName = "Aidan", lastName = "Byrne"),
        )
        labels.result = ApiResult.Success(testLabel(artists))
        val viewModel = viewModel()

        viewModel.handleUiAction(RosterViewAction.QueryChanged("THEO"))
        val found = stateOf(viewModel)
        viewModel.handleUiAction(RosterViewAction.QueryChanged("Zidane"))
        val missing = stateOf(viewModel)

        assertEquals(listOf("1"), found.sections.flatMap { it.artists }.map { it.id })
        assertTrue(missing.isNoResults)
    }

    @Test
    fun `the hidden budget is kept in the settings`() = runTest {
        viewModel().handleUiAction(RosterViewAction.ToggleBudget)

        assertTrue(stateOf(viewModel()).isBudgetHidden)
    }

    @Test
    fun `tapping an artist opens it`() = runTest {
        val viewModel = viewModel()

        viewModel.handleUiAction(RosterViewAction.ArtistClicked("artist-7"))

        assertEquals(RosterViewEvent.OpenArtist("artist-7"), viewModel.uiEventsFlow.first())
    }

    private fun viewModel() = RosterViewModel(LabelInteractor(sessions, labels), settings, savedState)

    private fun TestScope.stateOf(viewModel: RosterViewModel): RosterViewState {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.viewState.collect {}
        }
        return viewModel.viewState.value
    }
}
