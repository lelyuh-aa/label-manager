package com.leliukh.labelmanager.features.entry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leliukh.labelmanager.core.session.domain.repository.SessionRepository
import com.leliukh.labelmanager.core.settings.domain.models.ThemeMode
import com.leliukh.labelmanager.core.settings.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Decides where the app starts and notices when the session ends.
 *
 * The splash screen stays up while [EntryViewState.isLoading], so the user never sees the
 * Welcome screen flash before a stored session is read. If the session cannot be read at all,
 * the user starts signed out instead of the app crashing: signing in again is the recovery.
 * The chosen palette is read here too, so the first frame already has the right theme.
 *
 * @param sessionRepository source of the current session
 * @param settingsRepository source of the chosen palette
 */
@HiltViewModel
class EntryViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val isSignedIn: Flow<Boolean> = sessionRepository.session
        .map { session -> session != null }
        .catch { emit(false) }

    val viewState: StateFlow<EntryViewState> = combine(
        isSignedIn,
        settingsRepository.settings,
    ) { isSignedIn, settings ->
        EntryViewState(isLoading = false, isSignedIn = isSignedIn, themeMode = settings.themeMode)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = EntryViewState(isLoading = true, isSignedIn = false),
        )
}

/**
 * @property isLoading whether the stored session is still being read
 * @property isSignedIn whether a session exists
 * @property themeMode the chosen palette
 */
data class EntryViewState(
    val isLoading: Boolean,
    val isSignedIn: Boolean,
    val themeMode: ThemeMode = ThemeMode.System,
)
