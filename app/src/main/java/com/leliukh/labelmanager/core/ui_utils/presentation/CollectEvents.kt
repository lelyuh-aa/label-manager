package com.leliukh.labelmanager.core.ui_utils.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

/**
 * Collects a view model's one-off events, such as navigation, while the screen is at least
 * started, so an event is never handled by a screen that is in the background.
 *
 * @param events the view model's event flow
 * @param onEvent handles one event
 */
@Composable
fun <T> CollectEvents(events: Flow<T>, onEvent: (T) -> Unit) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentOnEvent by rememberUpdatedState(onEvent)

    LaunchedEffect(events, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            events.collect { currentOnEvent(it) }
        }
    }
}
