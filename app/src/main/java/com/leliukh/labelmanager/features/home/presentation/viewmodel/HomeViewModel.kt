package com.leliukh.labelmanager.features.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leliukh.labelmanager.features.activity.domain.interactor.ActivityInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

private val POLL_INTERVAL = 30.seconds
private const val STOP_TIMEOUT_MS = 5_000L

/**
 * The signed-in home around the tabs: keeps the Activity badge current.
 *
 * The feed is checked every [POLL_INTERVAL] while Home is on screen; collecting with the
 * lifecycle stops the checks in the background, where the worker takes over.
 *
 * @param activityInteractor the activity feed
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val activityInteractor: ActivityInteractor,
) : ViewModel() {

    private val polling = flow {
        while (true) {
            activityInteractor.check()
            emit(Unit)
            delay(POLL_INTERVAL)
        }
    }

    val viewState: StateFlow<HomeViewState> =
        combine(activityInteractor.unreadCount, polling) { unreadCount, _ ->
            HomeViewState(activityBadge = unreadCount)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = HomeViewState(),
        )
}

/**
 * @property activityBadge unread events, shown on the Activity tab
 */
data class HomeViewState(
    val activityBadge: Int = 0,
)
