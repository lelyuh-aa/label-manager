package com.leliukh.labelmanager.features.market.presentation.viewmodel.createlisting

import androidx.lifecycle.SavedStateHandle
import com.leliukh.labelmanager.core.session.domain.repository.FakeSessionRepository
import com.leliukh.labelmanager.core.session.domain.repository.testSession
import com.leliukh.labelmanager.features.activity.domain.repository.activityInteractor
import com.leliukh.labelmanager.features.market.domain.interactor.MarketInteractor
import com.leliukh.labelmanager.features.market.domain.repository.FakeMarketRepository
import com.leliukh.labelmanager.features.label.domain.interactor.LabelInteractor
import com.leliukh.labelmanager.features.label.domain.models.CachedLabel
import com.leliukh.labelmanager.features.label.domain.models.ArtistListing
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
import org.junit.Before
import org.junit.Test
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class CreateListingViewModelTest {

    private val sessions = FakeSessionRepository(testSession())
    private val market = FakeMarketRepository()
    private val labels = FakeLabelRepository().apply {
        val artists = listOf(
            testArtist(id = "p1", marketValue = 1_380_000),
            testArtist(
                id = "p2",
                listing = ArtistListing("l1", 1_600_000, Instant.parse("2026-10-25T10:00:00Z")),
            ),
        )
        cache.value = mapOf("label-1" to CachedLabel(testLabel(artists), now))
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the price starts at the market value with markups offered`() {
        val state = viewModel("p1").viewState.value

        assertEquals(1_380_000L, state.price)
        assertEquals(
            listOf(1_380_000L, 1_518_000L, 1_725_000L, 2_070_000L),
            state.quickPrices.map { it.price },
        )
    }

    @Test
    fun `typed text keeps only the digits`() {
        val viewModel = viewModel("p1")

        viewModel.handleUiAction(CreateListingViewAction.PriceTyped("$1,9a00,000"))

        assertEquals(1_900_000L, viewModel.viewState.value.price)
        assertEquals(520_000L, viewModel.viewState.value.difference)
    }

    @Test
    fun `a zero price cannot be listed`() {
        val viewModel = viewModel("p1")

        viewModel.handleUiAction(CreateListingViewAction.PriceTyped("0"))

        assertFalse(viewModel.viewState.value.isPriceValid)
    }

    @Test
    fun `listing sends the price and closes the flow`() = runTest {
        val viewModel = viewModel("p1")
        viewModel.handleUiAction(CreateListingViewAction.QuickPriceChosen(1_725_000))

        viewModel.handleUiAction(CreateListingViewAction.Submit)

        assertEquals(listOf("p1" to 1_725_000L), market.created)
        assertEquals(CreateListingViewEvent.Close(isListed = true), viewModel.uiEventsFlow.first())
    }

    @Test
    fun `an artist already for sale closes the screen`() = runTest {
        val viewModel = viewModel("p2")

        assertEquals(CreateListingViewEvent.Close(isListed = false), viewModel.uiEventsFlow.first())
    }

    private fun viewModel(artistId: String) = CreateListingViewModel(
        artistId = artistId,
        marketInteractor = MarketInteractor(sessions, market, labels, activityInteractor(labels)),
        labelInteractor = LabelInteractor(sessions, labels),
        savedStateHandle = SavedStateHandle(),
    )
}
