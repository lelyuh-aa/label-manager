package com.leliukh.labelmanager.features.market.presentation.viewmodel.market

import androidx.lifecycle.SavedStateHandle
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.contract.dto.error.ErrorCodeDto
import com.leliukh.labelmanager.core.network.domain.ApiError
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.session.domain.repository.FakeSessionRepository
import com.leliukh.labelmanager.core.session.domain.repository.testSession
import com.leliukh.labelmanager.core.settings.domain.repository.FakeSettingsRepository
import com.leliukh.labelmanager.features.activity.domain.repository.activityInteractor
import com.leliukh.labelmanager.features.market.domain.interactor.MarketInteractor
import com.leliukh.labelmanager.features.market.domain.models.ListingFilter
import com.leliukh.labelmanager.features.market.domain.repository.FakeMarketRepository
import com.leliukh.labelmanager.features.market.domain.repository.testListing
import com.leliukh.labelmanager.features.market.presentation.mapper.toVO
import com.leliukh.labelmanager.features.market.presentation.models.FilterChipVO
import com.leliukh.labelmanager.features.label.domain.interactor.LabelInteractor
import com.leliukh.labelmanager.features.label.domain.models.CachedLabel
import com.leliukh.labelmanager.features.label.domain.models.Genre
import com.leliukh.labelmanager.features.label.domain.repository.FakeLabelRepository
import com.leliukh.labelmanager.features.label.domain.repository.testLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MarketViewModelTest {

    private val sessions = FakeSessionRepository(testSession())
    private val market = FakeMarketRepository()
    private val labels = FakeLabelRepository().apply {
        cache.value = mapOf("label-1" to CachedLabel(testLabel(), now))
    }
    private val viewModel by lazy {
        MarketViewModel(
            marketInteractor = MarketInteractor(sessions, market, labels, activityInteractor(labels)),
            labelInteractor = LabelInteractor(sessions, labels),
            settingsRepository = FakeSettingsRepository(),
            savedStateHandle = SavedStateHandle(),
        )
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
    fun `the search and filters are counted once typing settles`() = runTest {
        viewModel.handleUiAction(MarketViewAction.QueryChanged("O"))
        viewModel.handleUiAction(MarketViewAction.QueryChanged("Oka"))
        advanceUntilIdle()

        assertEquals(listOf("Oka"), market.counted.map { it.text })
        assertEquals(1, viewModel.viewState.value.totalCount)
    }

    @Test
    fun `an applied draft shows as chips and a chip removes its filter`() = runTest {
        viewModel.handleUiAction(MarketViewAction.OpenFilters)
        viewModel.handleUiAction(
            MarketViewAction.DraftChanged(
                ListingFilter(maxPrice = 2_000_000, genres = setOf(Genre.Electronic)),
            ),
        )
        viewModel.handleUiAction(MarketViewAction.ApplyDraft)

        val chips = viewModel.viewState.value.chips
        assertEquals(2, chips.size)
        assertNull(viewModel.viewState.value.draft)

        viewModel.handleUiAction(MarketViewAction.ChipRemoved(chips.first()))
        assertEquals(
            listOf<FilterChipVO>(FilterChipVO.GenreChip(Genre.Electronic)),
            viewModel.viewState.value.chips,
        )
    }

    @Test
    fun `a confirmed purchase refreshes the list and opens the deal-done moment`() = runTest {
        viewModel.handleUiAction(MarketViewAction.BuyClicked(testListing().toVO()))
        assertTrue(viewModel.viewState.value.buyConfirm!!.canAfford)

        viewModel.handleUiAction(MarketViewAction.ConfirmPurchase)

        val events = viewModel.uiEventsFlow.take(2).toList()
        assertEquals(MarketViewEvent.ListingsChanged, events[0])
        assertEquals(1_350_000L, (events[1] as MarketViewEvent.OpenDealDone).route.valueAfter)
        assertNull(viewModel.viewState.value.buyConfirm)
    }

    @Test
    fun `a listing over the budget cannot be bought`() = runTest {
        val listing = testListing(askingPrice = 9_000_000).toVO()
        viewModel.handleUiAction(MarketViewAction.BuyClicked(listing))

        val confirm = viewModel.viewState.value.buyConfirm!!
        assertFalse(confirm.canAfford)
        assertEquals(3_000_000L - 9_000_000L, confirm.budgetAfter)
    }

    @Test
    fun `a listing sold to someone else explains what happened`() = runTest {
        market.purchaseResult = ApiResult.Failure(
            ApiError.Server(ErrorCodeDto.LISTING_NOT_AVAILABLE, "Sold", 409),
        )
        viewModel.handleUiAction(MarketViewAction.BuyClicked(testListing().toVO()))

        viewModel.handleUiAction(MarketViewAction.ConfirmPurchase)

        assertEquals(R.string.market_buy_gone, viewModel.viewState.value.buyConfirm?.errorRes)
    }
}
