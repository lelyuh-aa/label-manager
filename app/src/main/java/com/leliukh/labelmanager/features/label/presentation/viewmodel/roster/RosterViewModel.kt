package com.leliukh.labelmanager.features.label.presentation.viewmodel.roster

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leliukh.labelmanager.core.formatting.presentation.countryFlagEmoji
import com.leliukh.labelmanager.core.network.domain.ApiError
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.settings.domain.repository.SettingsRepository
import com.leliukh.labelmanager.features.label.domain.interactor.LabelInteractor
import com.leliukh.labelmanager.features.label.domain.models.CachedLabel
import com.leliukh.labelmanager.features.label.presentation.mapper.toRosterSections
import com.leliukh.labelmanager.features.label.presentation.models.RosterSectionVO
import com.leliukh.labelmanager.features.label.presentation.models.StaleDataVO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val KEY_QUERY = "query"
private const val STOP_TIMEOUT_MS = 5_000L
private const val MIN_RETRY_SPINNER_MS = 600L

/**
 * The Roster tab: the user's label with its artists grouped by genre.
 *
 * The screen shows the cached label at once and refreshes it from the server when it opens and
 * on pull-to-refresh. When a refresh fails the cached label stays on screen with a notice; only
 * without any cached label does the failure replace the content. The search query survives
 * process death; the budget mask is a setting and survives restarts.
 *
 * @param labelInteractor the signed-in user's label
 * @param settingsRepository keeps the budget mask
 * @param savedStateHandle keeps the query
 */
@HiltViewModel
class RosterViewModel @Inject constructor(
    private val labelInteractor: LabelInteractor,
    private val settingsRepository: SettingsRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val refreshState = MutableStateFlow(RefreshState())
    private val events = Channel<RosterViewEvent>(Channel.BUFFERED)

    val viewState: StateFlow<RosterViewState> = combine(
        labelInteractor.observeLabel(),
        savedStateHandle.getStateFlow(KEY_QUERY, ""),
        settingsRepository.settings.map { it.isBudgetHidden }.distinctUntilChanged(),
        refreshState,
    ) { cached, query, isBudgetHidden, refresh ->
        toViewState(cached, query, isBudgetHidden, refresh)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), RosterViewState())

    val uiEventsFlow: Flow<RosterViewEvent> = events.receiveAsFlow()

    init {
        refresh(isPull = false)
    }

    /**
     * @param action what the user did
     */
    fun handleUiAction(action: RosterViewAction) {
        when (action) {
            is RosterViewAction.QueryChanged -> savedStateHandle[KEY_QUERY] = action.query

            RosterViewAction.ClearQuery -> savedStateHandle[KEY_QUERY] = ""

            RosterViewAction.ToggleBudget -> viewModelScope.launch {
                settingsRepository.setBudgetHidden(!viewState.value.isBudgetHidden)
            }

            RosterViewAction.PullToRefresh -> refresh(isPull = true)

            RosterViewAction.Retry -> refresh(isPull = false, isRetry = true)

            is RosterViewAction.ArtistClicked ->
                events.trySend(RosterViewEvent.OpenArtist(action.artistId))

            RosterViewAction.EditLabelClicked -> events.trySend(RosterViewEvent.OpenEditLabel)
        }
    }

    private fun refresh(isPull: Boolean, isRetry: Boolean = false) {
        if (refreshState.value.isRunning) return
        refreshState.update { it.copy(isRunning = true, isPull = isPull) }
        viewModelScope.launch {
            // A retry that fails at once would look like a dead button; show the spinner a moment.
            val minimumSpinner = launch { if (isRetry) delay(MIN_RETRY_SPINNER_MS) }
            val result = labelInteractor.refreshLabel()
            minimumSpinner.join()
            refreshState.value = RefreshState(error = (result as? ApiResult.Failure)?.error)
        }
    }

    private fun toViewState(
        cached: CachedLabel?,
        query: String,
        isBudgetHidden: Boolean,
        refresh: RefreshState,
    ): RosterViewState {
        if (cached == null) {
            return RosterViewState(
                isLoading = refresh.isRunning || refresh.error == null,
                isError = !refresh.isRunning && refresh.error != null,
                query = query,
                isBudgetHidden = isBudgetHidden,
            )
        }
        val label = cached.label
        val sections = label.artists.toRosterSections(query)
        return RosterViewState(
            isLoading = false,
            isRefreshing = refresh.isRunning && refresh.isPull,
            isRetrying = refresh.isRunning && !refresh.isPull,
            staleData = refresh.error?.let { error ->
                StaleDataVO(isOffline = error == ApiError.Network, syncedAt = cached.syncedAt)
            },
            labelName = label.name,
            flag = countryFlagEmoji(label.countryCode),
            budget = label.budget,
            rosterValue = label.value,
            artistCount = label.artists.size,
            isBudgetHidden = isBudgetHidden,
            query = query,
            sections = sections,
            isNoResults = query.isNotBlank() && sections.isEmpty(),
        )
    }

    /**
     * @property isRunning whether a refresh is in flight
     * @property isPull whether the running refresh was started by pull-to-refresh
     * @property error why the last refresh failed; null after a success
     */
    private data class RefreshState(
        val isRunning: Boolean = false,
        val isPull: Boolean = false,
        val error: ApiError? = null,
    )
}

/**
 * @property isLoading whether nothing is cached yet and the first load is running
 * @property isError whether nothing is cached and the load failed
 * @property isRefreshing whether a pull-to-refresh is running
 * @property isRetrying whether a retry from the stale data notice is running
 * @property staleData notice shown when the last refresh failed but a cached label is shown
 * @property labelName name of the label
 * @property flag flag emoji of the label's country
 * @property budget budget in whole US dollars
 * @property rosterValue sum of the artists' values
 * @property artistCount number of artists
 * @property isBudgetHidden whether the budget is masked
 * @property query text in the search field
 * @property sections artists matching [query], grouped by genre
 * @property isNoResults whether [query] matches nobody
 */
data class RosterViewState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val isRefreshing: Boolean = false,
    val isRetrying: Boolean = false,
    val staleData: StaleDataVO? = null,
    val labelName: String = "",
    val flag: String = "",
    val budget: Long = 0,
    val rosterValue: Long = 0,
    val artistCount: Int = 0,
    val isBudgetHidden: Boolean = false,
    val query: String = "",
    val sections: List<RosterSectionVO> = emptyList(),
    val isNoResults: Boolean = false,
)

/** What the user does on the Roster tab. */
sealed interface RosterViewAction {

    /** @property query the new search text */
    data class QueryChanged(val query: String) : RosterViewAction

    data object ClearQuery : RosterViewAction

    data object ToggleBudget : RosterViewAction

    data object PullToRefresh : RosterViewAction

    data object Retry : RosterViewAction

    /** @property artistId the tapped artist */
    data class ArtistClicked(val artistId: String) : RosterViewAction

    data object EditLabelClicked : RosterViewAction
}

/** One-off results of the Roster tab. */
sealed interface RosterViewEvent {

    /** @property artistId the artist to show */
    data class OpenArtist(val artistId: String) : RosterViewEvent

    data object OpenEditLabel : RosterViewEvent
}
