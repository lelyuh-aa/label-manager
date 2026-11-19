package com.leliukh.labelmanager.features.artist.presentation.viewmodel.details

import com.leliukh.labelmanager.core.network.domain.ApiError
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.session.domain.repository.FakeSessionRepository
import com.leliukh.labelmanager.core.session.domain.repository.testSession
import com.leliukh.labelmanager.features.activity.domain.repository.activityInteractor
import com.leliukh.labelmanager.features.market.domain.interactor.MarketInteractor
import com.leliukh.labelmanager.features.market.domain.repository.FakeMarketRepository
import com.leliukh.labelmanager.features.artist.domain.interactor.ArtistInteractor
import com.leliukh.labelmanager.features.artist.domain.repository.FakeArtistRepository
import com.leliukh.labelmanager.features.artist.domain.repository.testDetails
import com.leliukh.labelmanager.features.label.domain.interactor.LabelInteractor
import com.leliukh.labelmanager.features.label.domain.repository.FakeLabelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistViewModelTest {

    private val sessions = FakeSessionRepository(testSession())
    private val artists = FakeArtistRepository()
    private val labels = FakeLabelRepository()
    private val viewModel = ArtistViewModel(
        artistId = "artist-1",
        artistInteractor = ArtistInteractor(sessions, artists, labels),
        marketInteractor = MarketInteractor(sessions, FakeMarketRepository(), labels, activityInteractor(labels)),
        labelInteractor = LabelInteractor(sessions, labels),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the profile loads when the screen is shown`() {
        viewModel.handleUiAction(ArtistViewAction.ScreenShown)

        val profile = viewModel.viewState.value.profile
        assertEquals("Marchand", profile?.lastName)
        assertTrue(profile?.isMine == true)
    }

    @Test
    fun `a failed reload keeps the shown profile`() {
        viewModel.handleUiAction(ArtistViewAction.ScreenShown)
        artists.details = ApiResult.Failure(ApiError.Network)

        viewModel.handleUiAction(ArtistViewAction.ScreenShown)

        assertFalse(viewModel.viewState.value.isError)
        assertEquals("Marchand", viewModel.viewState.value.profile?.lastName)
    }

    @Test
    fun `a failed first load shows the error and can be retried`() {
        artists.details = ApiResult.Failure(ApiError.Network)
        viewModel.handleUiAction(ArtistViewAction.ScreenShown)
        assertTrue(viewModel.viewState.value.isError)

        artists.details = ApiResult.Success(testDetails())
        viewModel.handleUiAction(ArtistViewAction.Retry)

        assertFalse(viewModel.viewState.value.isError)
        assertEquals(2, artists.loads)
    }
}
