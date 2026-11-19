package com.leliukh.labelmanager.features.artist.presentation.viewmodel.edit

import androidx.lifecycle.SavedStateHandle
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.network.domain.ApiError
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.session.domain.repository.FakeSessionRepository
import com.leliukh.labelmanager.core.session.domain.repository.testSession
import com.leliukh.labelmanager.features.artist.domain.interactor.ArtistInteractor
import com.leliukh.labelmanager.features.artist.domain.repository.FakeArtistRepository
import com.leliukh.labelmanager.features.label.domain.interactor.LabelInteractor
import com.leliukh.labelmanager.features.label.domain.models.CachedLabel
import com.leliukh.labelmanager.features.label.domain.repository.FakeLabelRepository
import com.leliukh.labelmanager.features.label.domain.repository.testArtist
import com.leliukh.labelmanager.features.label.domain.repository.testLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditArtistViewModelTest {

    private val sessions = FakeSessionRepository(testSession())
    private val artists = FakeArtistRepository()
    private val labels = FakeLabelRepository().apply {
        val artist = testArtist(id = "p1", firstName = "Théo", lastName = "Marchand")
        cache.value = mapOf("label-1" to CachedLabel(testLabel(listOf(artist)), now))
    }
    private val savedState = SavedStateHandle()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `save stays off until a name changes and both names are valid`() {
        val viewModel = viewModel()
        assertFalse(viewModel.viewState.value.isSaveEnabled)

        viewModel.handleUiAction(EditArtistViewAction.LastNameChanged("  "))
        assertTrue(viewModel.viewState.value.isLastNameInvalid)
        assertFalse(viewModel.viewState.value.isSaveEnabled)

        viewModel.handleUiAction(EditArtistViewAction.LastNameChanged("Marchand-Roux"))
        assertTrue(viewModel.viewState.value.isSaveEnabled)
    }

    @Test
    fun `a saved edit closes the screen`() = runTest {
        val viewModel = viewModel()
        viewModel.handleUiAction(EditArtistViewAction.CountryChosen("BE"))

        viewModel.handleUiAction(EditArtistViewAction.Save)

        assertEquals(listOf("p1", "Théo", "Marchand", "BE"), artists.updates.single())
        assertEquals(EditArtistViewEvent.Close, viewModel.uiEventsFlow.first())
    }

    @Test
    fun `a failed save keeps the form with a message`() {
        artists.updateResult = ApiResult.Failure(ApiError.Network)
        val viewModel = viewModel()
        viewModel.handleUiAction(EditArtistViewAction.FirstNameChanged("Theo"))

        viewModel.handleUiAction(EditArtistViewAction.Save)

        val state = viewModel.viewState.value
        assertFalse(state.isSaving)
        assertEquals(R.string.common_error_network, state.errorRes)
        assertEquals("Theo", state.firstName)
    }

    @Test
    fun `typed names survive a new view model`() {
        viewModel().handleUiAction(EditArtistViewAction.FirstNameChanged("Theo"))

        assertEquals("Theo", viewModel().viewState.value.firstName)
    }

    private fun viewModel() = EditArtistViewModel(
        artistId = "p1",
        artistInteractor = ArtistInteractor(sessions, artists, labels),
        labelInteractor = LabelInteractor(sessions, labels),
        savedStateHandle = savedState,
    )
}
