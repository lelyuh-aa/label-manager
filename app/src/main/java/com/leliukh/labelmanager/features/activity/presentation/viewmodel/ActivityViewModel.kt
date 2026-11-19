package com.leliukh.labelmanager.features.activity.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.insertSeparators
import androidx.paging.map
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.features.activity.domain.interactor.ActivityInteractor
import com.leliukh.labelmanager.features.activity.presentation.mapper.dayHeaderBetween
import com.leliukh.labelmanager.features.activity.presentation.mapper.matches
import com.leliukh.labelmanager.features.activity.presentation.mapper.toVO
import com.leliukh.labelmanager.features.activity.presentation.models.ActivityFilter
import com.leliukh.labelmanager.features.activity.presentation.models.ActivityItemVO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val KEY_FILTER = "filter"
private const val STOP_TIMEOUT_MS = 5_000L

/**
 * The Activity tab: deals and listings of the user's label, newest first, grouped by day and
 * filtered by kind.
 *
 * The pages are loaded once and cached; switching the filter only filters what is loaded,
 * so the chips react at once. Mark all read clears the badge and reloads the feed.
 *
 * @param activityInteractor the feed and its unread count
 * @param savedStateHandle keeps the chosen filter
 */
@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val activityInteractor: ActivityInteractor,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val filter = savedStateHandle.getStateFlow(KEY_FILTER, ActivityFilter.All)
    private val isMarking = MutableStateFlow(false)
    private val readIds = MutableStateFlow(emptySet<String>())
    private val events = Channel<ActivityViewEvent>(Channel.BUFFERED)
    private val pages = activityInteractor.events().cachedIn(viewModelScope)

    val viewState: StateFlow<ActivityViewState> = combine(
        filter,
        activityInteractor.unreadCount,
        isMarking,
    ) { filter, unreadCount, isMarking ->
        ActivityViewState(filter = filter, unreadCount = unreadCount, isMarking = isMarking)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = ActivityViewState(),
    )

    /** The feed for the chosen filter, with a header above every day. */
    val items: Flow<PagingData<ActivityItemVO>> = combine(
        pages,
        filter,
        readIds,
    ) { page, filter, readIds ->
        page.filter { it.matches(filter) }
            .map { event ->
                val item = event.toVO()
                if (item.id in readIds) item.copy(isRead = true) else item
            }
            .insertSeparators { before: ActivityItemVO.Event?, after: ActivityItemVO.Event? ->
                dayHeaderBetween(before, after)
            }
    }

    val uiEventsFlow: Flow<ActivityViewEvent> = events.receiveAsFlow()

    init {
        // The badge is polled app-wide; when it finds new events, reload the feed to show them.
        viewModelScope.launch {
            var knownUnread = activityInteractor.unreadCount.value
            activityInteractor.unreadCount.collect { unread ->
                if (unread > knownUnread) events.trySend(ActivityViewEvent.Reload)
                knownUnread = unread
            }
        }
    }

    /**
     * @param action what the user did
     */
    fun handleUiAction(action: ActivityViewAction) {
        when (action) {
            is ActivityViewAction.FilterChosen -> savedStateHandle[KEY_FILTER] = action.filter

            ActivityViewAction.MarkAllRead -> markAllRead()

            // Back from an artist the user may have listed or sold: check now instead of
            // waiting for the next poll; new events reload the feed through the count.
            ActivityViewAction.Shown -> viewModelScope.launch { activityInteractor.check() }

            is ActivityViewAction.EventClicked -> {
                if (!action.isRead) markRead(action.eventId)
                events.trySend(ActivityViewEvent.OpenArtist(action.artistId))
            }
        }
    }

    // The tapped row turns read at once; the page itself is not reloaded.
    private fun markRead(eventId: String) {
        if (eventId in readIds.value) return
        readIds.update { it + eventId }
        viewModelScope.launch {
            if (activityInteractor.markRead(eventId) is ApiResult.Failure) {
                readIds.update { it - eventId }
            }
        }
    }

    private fun markAllRead() {
        if (isMarking.value) return
        isMarking.value = true
        viewModelScope.launch {
            val result = activityInteractor.markAllRead()
            isMarking.update { false }
            if (result is ApiResult.Success) events.trySend(ActivityViewEvent.Reload)
        }
    }
}

/**
 * @property filter the chosen chip
 * @property unreadCount unread events
 * @property isMarking whether mark-all-read is running
 */
data class ActivityViewState(
    val filter: ActivityFilter = ActivityFilter.All,
    val unreadCount: Int = 0,
    val isMarking: Boolean = false,
)

/** What the user does on the Activity tab. */
sealed interface ActivityViewAction {

    /** The tab came on screen. */
    data object Shown : ActivityViewAction

    /** @property filter the tapped chip */
    data class FilterChosen(val filter: ActivityFilter) : ActivityViewAction

    data object MarkAllRead : ActivityViewAction

    /**
     * @property eventId the tapped event
     * @property artistId the artist of the event
     * @property isRead whether the event was already read
     */
    data class EventClicked(
        val eventId: String,
        val artistId: String,
        val isRead: Boolean,
    ) : ActivityViewAction
}

/** One-off results of the Activity tab. */
sealed interface ActivityViewEvent {

    /** @property artistId the artist to show */
    data class OpenArtist(val artistId: String) : ActivityViewEvent

    /** The feed changed and should load again. */
    data object Reload : ActivityViewEvent
}
