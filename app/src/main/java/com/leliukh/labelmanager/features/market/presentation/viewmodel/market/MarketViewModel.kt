package com.leliukh.labelmanager.features.market.presentation.viewmodel.market

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.contract.dto.error.ErrorCodeDto
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.network.domain.serverCode
import com.leliukh.labelmanager.core.settings.domain.repository.SettingsRepository
import com.leliukh.labelmanager.core.ui_utils.presentation.messageRes
import com.leliukh.labelmanager.features.market.domain.interactor.MarketInteractor
import com.leliukh.labelmanager.features.market.domain.models.ListingFilter
import com.leliukh.labelmanager.features.market.domain.models.ListingSort
import com.leliukh.labelmanager.features.market.domain.models.SearchScope
import com.leliukh.labelmanager.features.market.presentation.mapper.toChips
import com.leliukh.labelmanager.features.market.presentation.mapper.toRoute
import com.leliukh.labelmanager.features.market.presentation.mapper.toVO
import com.leliukh.labelmanager.features.market.presentation.mapper.without
import com.leliukh.labelmanager.features.market.presentation.models.BuyConfirmVO
import com.leliukh.labelmanager.features.market.presentation.models.FilterChipVO
import com.leliukh.labelmanager.features.market.presentation.models.MarketListingVO
import com.leliukh.labelmanager.features.market.presentation.navigation.DealDoneRoute
import com.leliukh.labelmanager.features.label.domain.interactor.LabelInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val KEY_QUERY = "query"
private const val KEY_SCOPE = "scope"
private const val SEARCH_DEBOUNCE_MS = 300L

/**
 * The Market tab: other labels' artists on the market, searched by artist or label name,
 * filtered, sorted and bought.
 *
 * Typing is debounced before a new search; every change of the search or the filters starts
 * the paged list again from the first page and recounts the matches. The filter sheet edits a
 * draft and shows how many artists it would match before it is applied.
 *
 * @param marketInteractor the market
 * @param labelInteractor the user's label, for the budget
 * @param settingsRepository keeps the budget mask
 * @param savedStateHandle keeps the search text and the scope
 */
@HiltViewModel
class MarketViewModel @Inject constructor(
    private val marketInteractor: MarketInteractor,
    private val labelInteractor: LabelInteractor,
    private val settingsRepository: SettingsRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val mutableState = MutableStateFlow(
        MarketViewState(
            query = savedStateHandle[KEY_QUERY] ?: "",
            scope = savedStateHandle.get<String>(KEY_SCOPE)
                ?.let(SearchScope::valueOf)
                ?: SearchScope.Artists,
        ),
    )
    private var draftCountJob: Job? = null
    private val events = Channel<MarketViewEvent>(Channel.BUFFERED)

    @OptIn(FlowPreview::class)
    private val appliedFilter: Flow<ListingFilter> = mutableState
        .map { it.appliedFilter }
        .distinctUntilChanged()
        .debounce(SEARCH_DEBOUNCE_MS)

    val viewState: StateFlow<MarketViewState> = mutableState.asStateFlow()

    val uiEventsFlow: Flow<MarketViewEvent> = events.receiveAsFlow()

    /** The listings for the current search and filters, page by page. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val listings: Flow<PagingData<MarketListingVO>> = appliedFilter
        .flatMapLatest { filter -> marketInteractor.listings(filter) }
        .map { page -> page.map { it.toVO() } }
        .cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            labelInteractor.observeLabel().filterNotNull().collect { cached ->
                mutableState.update { it.copy(budget = cached.label.budget) }
            }
        }
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                mutableState.update { it.copy(isBudgetHidden = settings.isBudgetHidden) }
            }
        }
        viewModelScope.launch {
            appliedFilter.collectLatest { filter ->
                val count = (marketInteractor.countListings(filter) as? ApiResult.Success)?.data
                mutableState.update { it.copy(totalCount = count) }
            }
        }
    }

    /**
     * @param action what the user did
     */
    fun handleUiAction(action: MarketViewAction) {
        when (action) {
            is MarketViewAction.QueryChanged -> {
                savedStateHandle[KEY_QUERY] = action.query
                mutableState.update { it.copy(query = action.query) }
            }

            is MarketViewAction.ScopeChanged -> {
                savedStateHandle[KEY_SCOPE] = action.scope.name
                mutableState.update { it.copy(scope = action.scope) }
            }

            is MarketViewAction.SortChosen ->
                mutableState.update { it.copy(filter = it.filter.copy(sort = action.sort)) }

            MarketViewAction.ToggleBudget -> viewModelScope.launch {
                settingsRepository.setBudgetHidden(!mutableState.value.isBudgetHidden)
            }

            is MarketViewAction.ChipRemoved -> updateFilter { without(action.chip) }

            MarketViewAction.ClearFilters -> updateFilter { ListingFilter(sort = sort) }

            MarketViewAction.OpenFilters -> openDraft()

            is MarketViewAction.DraftChanged -> changeDraft(action.draft)

            MarketViewAction.ResetDraft ->
                changeDraft(ListingFilter(sort = mutableState.value.filter.sort))

            MarketViewAction.ApplyDraft -> applyDraft()

            MarketViewAction.CloseFilters -> mutableState.update { it.copy(draft = null) }

            is MarketViewAction.ListingClicked ->
                events.trySend(MarketViewEvent.OpenArtist(action.listing.artistId))

            is MarketViewAction.BuyClicked -> openBuy(action.listing)

            MarketViewAction.ConfirmPurchase -> purchase()

            MarketViewAction.DismissBuy -> mutableState.update { it.copy(buyConfirm = null) }

            MarketViewAction.SellClicked -> {
                mutableState.update { it.copy(buyConfirm = null) }
                events.trySend(MarketViewEvent.OpenSellArtist)
            }
        }
    }

    private fun updateFilter(transform: ListingFilter.() -> ListingFilter) {
        mutableState.update { it.copy(filter = it.filter.transform()) }
    }

    private fun openDraft() {
        val filter = mutableState.value.filter
        mutableState.update { it.copy(draft = filter, draftCount = it.totalCount) }
    }

    private fun changeDraft(draft: ListingFilter) {
        mutableState.update { it.copy(draft = draft, draftCount = null) }
        draftCountJob?.cancel()
        draftCountJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            val withSearch = mutableState.value.appliedFilter.let { applied ->
                draft.copy(text = applied.text, scope = applied.scope)
            }
            val count = (marketInteractor.countListings(withSearch) as? ApiResult.Success)?.data
            mutableState.update { it.copy(draftCount = count) }
        }
    }

    private fun applyDraft() {
        val draft = mutableState.value.draft ?: return
        mutableState.update { it.copy(filter = draft, draft = null) }
    }

    private fun openBuy(listing: MarketListingVO) {
        val budget = mutableState.value.budget ?: return
        mutableState.update {
            it.copy(buyConfirm = BuyConfirmVO(listing = listing, budget = budget))
        }
    }

    private fun purchase() {
        val confirm = mutableState.value.buyConfirm ?: return
        if (confirm.isBuying || !confirm.canAfford) return
        mutableState.update { it.copy(buyConfirm = confirm.copy(isBuying = true, errorRes = null)) }
        viewModelScope.launch {
            when (val result = marketInteractor.purchase(confirm.listing.listingId)) {
                is ApiResult.Success -> {
                    mutableState.update { it.copy(buyConfirm = null) }
                    events.trySend(MarketViewEvent.ListingsChanged)
                    events.trySend(MarketViewEvent.OpenDealDone(result.data.toRoute()))
                }

                is ApiResult.Failure -> {
                    val isGone = result.error.serverCode == ErrorCodeDto.LISTING_NOT_AVAILABLE
                    if (isGone) events.trySend(MarketViewEvent.ListingsChanged)
                    val errorRes = if (isGone) R.string.market_buy_gone else result.error.messageRes
                    mutableState.update {
                        it.copy(buyConfirm = confirm.copy(isBuying = false, errorRes = errorRes))
                    }
                }
            }
        }
    }
}

/**
 * @property budget the user's budget; null until the label is loaded
 * @property isBudgetHidden whether the budget is masked
 * @property query text in the search field
 * @property scope what [query] is matched against
 * @property filter the applied filters and sort order, without the search text
 * @property totalCount how many listings match; null while counting or offline
 * @property draft filters being edited in the open filter sheet; null while it is closed
 * @property draftCount how many listings [draft] would match; null while counting
 * @property buyConfirm the open buy confirmation; null while it is closed
 */
data class MarketViewState(
    val budget: Long? = null,
    val isBudgetHidden: Boolean = false,
    val query: String = "",
    val scope: SearchScope = SearchScope.Artists,
    val filter: ListingFilter = ListingFilter(),
    val totalCount: Int? = null,
    val draft: ListingFilter? = null,
    val draftCount: Int? = null,
    val buyConfirm: BuyConfirmVO? = null,
) {

    /** The filters as chips under the search field. */
    val chips: List<FilterChipVO>
        get() = filter.toChips()

    /** The search and the filters together, as sent to the server. */
    val appliedFilter: ListingFilter
        get() = filter.copy(text = query, scope = scope)
}

/** What the user does on the Market tab. */
sealed interface MarketViewAction {

    /** @property query the new search text */
    data class QueryChanged(val query: String) : MarketViewAction

    /** @property scope what the search text is matched against */
    data class ScopeChanged(val scope: SearchScope) : MarketViewAction

    /** @property sort the new order */
    data class SortChosen(val sort: ListingSort) : MarketViewAction

    data object ToggleBudget : MarketViewAction

    /** @property chip the removed filter */
    data class ChipRemoved(val chip: FilterChipVO) : MarketViewAction

    data object ClearFilters : MarketViewAction

    data object OpenFilters : MarketViewAction

    /** @property draft the edited filters */
    data class DraftChanged(val draft: ListingFilter) : MarketViewAction

    data object ResetDraft : MarketViewAction

    data object ApplyDraft : MarketViewAction

    data object CloseFilters : MarketViewAction

    /** @property listing the tapped row */
    data class ListingClicked(val listing: MarketListingVO) : MarketViewAction

    /** @property listing the listing to buy */
    data class BuyClicked(val listing: MarketListingVO) : MarketViewAction

    data object ConfirmPurchase : MarketViewAction

    data object DismissBuy : MarketViewAction

    data object SellClicked : MarketViewAction
}

/** One-off results of the Market tab. */
sealed interface MarketViewEvent {

    /** @property artistId the artist to show */
    data class OpenArtist(val artistId: String) : MarketViewEvent

    /** @property route what the deal-done moment shows */
    data class OpenDealDone(val route: DealDoneRoute) : MarketViewEvent

    data object OpenSellArtist : MarketViewEvent

    /** The market changed under the list, which should load again. */
    data object ListingsChanged : MarketViewEvent
}
