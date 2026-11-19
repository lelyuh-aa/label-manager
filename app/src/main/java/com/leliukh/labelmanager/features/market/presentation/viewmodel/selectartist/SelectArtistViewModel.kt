package com.leliukh.labelmanager.features.market.presentation.viewmodel.selectartist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leliukh.labelmanager.features.label.domain.interactor.LabelInteractor
import com.leliukh.labelmanager.features.label.presentation.mapper.toRosterSections
import com.leliukh.labelmanager.features.label.presentation.models.RosterSectionVO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val KEY_QUERY = "query"
private const val STOP_TIMEOUT_MS = 5_000L

/**
 * Sell an artist, step one: the user's roster by genre, searchable. Artists already on the
 * market are shown but cannot be picked.
 *
 * @param labelInteractor the user's cached roster
 * @param savedStateHandle keeps the search text
 */
@HiltViewModel
class SelectArtistViewModel @Inject constructor(
    labelInteractor: LabelInteractor,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val events = Channel<SelectArtistViewEvent>(Channel.BUFFERED)

    val viewState: StateFlow<SelectArtistViewState> = combine(
        labelInteractor.observeLabel(),
        savedStateHandle.getStateFlow(KEY_QUERY, ""),
    ) { cached, query ->
        SelectArtistViewState(
            isLoading = cached == null,
            query = query,
            sections = cached?.label?.artists?.toRosterSections(query).orEmpty(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = SelectArtistViewState(),
    )

    val uiEventsFlow: Flow<SelectArtistViewEvent> = events.receiveAsFlow()

    /**
     * @param action what the user did
     */
    fun handleUiAction(action: SelectArtistViewAction) {
        when (action) {
            is SelectArtistViewAction.QueryChanged -> savedStateHandle[KEY_QUERY] = action.query

            is SelectArtistViewAction.ArtistClicked ->
                events.trySend(SelectArtistViewEvent.OpenCreateListing(action.artistId))

            SelectArtistViewAction.Back -> events.trySend(SelectArtistViewEvent.Close)
        }
    }
}

/**
 * @property isLoading whether the roster is not cached yet
 * @property query text in the search field
 * @property sections artists matching [query], grouped by genre
 */
data class SelectArtistViewState(
    val isLoading: Boolean = true,
    val query: String = "",
    val sections: List<RosterSectionVO> = emptyList(),
)

/** What the user does when picking an artist to sell. */
sealed interface SelectArtistViewAction {

    /** @property query the new search text */
    data class QueryChanged(val query: String) : SelectArtistViewAction

    /** @property artistId the picked artist */
    data class ArtistClicked(val artistId: String) : SelectArtistViewAction

    data object Back : SelectArtistViewAction
}

/** One-off results of picking an artist to sell. */
sealed interface SelectArtistViewEvent {

    /** @property artistId the artist to list */
    data class OpenCreateListing(val artistId: String) : SelectArtistViewEvent

    data object Close : SelectArtistViewEvent
}
