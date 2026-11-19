package com.leliukh.labelmanager.features.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leliukh.labelmanager.BuildConfig
import com.leliukh.labelmanager.core.formatting.presentation.countryFlagEmoji
import com.leliukh.labelmanager.core.session.domain.repository.SessionRepository
import com.leliukh.labelmanager.core.settings.domain.models.ThemeMode
import com.leliukh.labelmanager.core.settings.domain.repository.SettingsRepository
import com.leliukh.labelmanager.features.auth.domain.interactor.AuthInteractor
import com.leliukh.labelmanager.features.label.domain.interactor.LabelInteractor
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

private const val STOP_TIMEOUT_MS = 5_000L

/**
 * The Profile tab: the account and the label, preferences and signing out.
 *
 * Signing out asks for confirmation first. It clears the session, which sends the app back
 * to Welcome; the label stays on the server.
 *
 * @param sessionRepository the signed-in account
 * @param labelInteractor the user's label
 * @param settingsRepository the preferences
 * @param authInteractor signs the user out
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    labelInteractor: LabelInteractor,
    private val settingsRepository: SettingsRepository,
    private val authInteractor: AuthInteractor,
) : ViewModel() {

    private val signOutState = MutableStateFlow(SignOutState())
    private val events = Channel<ProfileViewEvent>(Channel.BUFFERED)

    val viewState: StateFlow<ProfileViewState> = combine(
        sessionRepository.session,
        labelInteractor.observeLabel(),
        settingsRepository.settings,
        signOutState,
    ) { session, cached, settings, signOut ->
        val label = cached?.label
        ProfileViewState(
            email = session?.email.orEmpty(),
            labelName = label?.name.orEmpty(),
            labelFlag = label?.countryCode?.let(::countryFlagEmoji).orEmpty(),
            artistCount = label?.artists?.size ?: 0,
            rosterValue = label?.value ?: 0,
            themeMode = settings.themeMode,
            isSaleAlertsEnabled = settings.isSaleAlertsEnabled,
            isSignOutConfirmOpen = signOut.isConfirmOpen,
            isSigningOut = signOut.isRunning,
            version = BuildConfig.VERSION_NAME,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = ProfileViewState(),
    )

    val uiEventsFlow: Flow<ProfileViewEvent> = events.receiveAsFlow()

    /**
     * @param action what the user did
     */
    fun handleUiAction(action: ProfileViewAction) {
        when (action) {
            ProfileViewAction.LabelClicked -> events.trySend(ProfileViewEvent.OpenEditLabel)

            is ProfileViewAction.ThemeChosen ->
                viewModelScope.launch { settingsRepository.setThemeMode(action.mode) }

            is ProfileViewAction.SaleAlertsToggled ->
                viewModelScope.launch { settingsRepository.setSaleAlertsEnabled(action.isEnabled) }

            ProfileViewAction.LanguageClicked ->
                events.trySend(ProfileViewEvent.OpenLanguageSettings)

            ProfileViewAction.SignOutClicked ->
                signOutState.update { it.copy(isConfirmOpen = true) }

            ProfileViewAction.DismissSignOut ->
                signOutState.update { it.copy(isConfirmOpen = false) }

            ProfileViewAction.ConfirmSignOut -> signOut()
        }
    }

    private fun signOut() {
        if (signOutState.value.isRunning) return
        signOutState.update { it.copy(isRunning = true) }
        viewModelScope.launch { authInteractor.signOut() }
    }

    /**
     * @property isConfirmOpen whether the sign-out confirmation is shown
     * @property isRunning whether signing out is in progress
     */
    private data class SignOutState(
        val isConfirmOpen: Boolean = false,
        val isRunning: Boolean = false,
    )
}

/**
 * @property email the account's email
 * @property labelName name of the label
 * @property labelFlag flag emoji of the label's country
 * @property artistCount number of artists
 * @property rosterValue sum of the artists' values
 * @property themeMode the chosen palette
 * @property isSaleAlertsEnabled whether sales are announced
 * @property isSignOutConfirmOpen whether the sign-out confirmation is shown
 * @property isSigningOut whether signing out is in progress
 * @property version version name of the app
 */
data class ProfileViewState(
    val email: String = "",
    val labelName: String = "",
    val labelFlag: String = "",
    val artistCount: Int = 0,
    val rosterValue: Long = 0,
    val themeMode: ThemeMode = ThemeMode.System,
    val isSaleAlertsEnabled: Boolean = true,
    val isSignOutConfirmOpen: Boolean = false,
    val isSigningOut: Boolean = false,
    val version: String = "",
)

/** What the user does on the Profile tab. */
sealed interface ProfileViewAction {

    data object LabelClicked : ProfileViewAction

    /** @property mode the chosen palette */
    data class ThemeChosen(val mode: ThemeMode) : ProfileViewAction

    /** @property isEnabled the new state of the switch */
    data class SaleAlertsToggled(val isEnabled: Boolean) : ProfileViewAction

    data object LanguageClicked : ProfileViewAction

    data object SignOutClicked : ProfileViewAction

    data object DismissSignOut : ProfileViewAction

    data object ConfirmSignOut : ProfileViewAction
}

/** One-off results of the Profile tab. */
sealed interface ProfileViewEvent {

    data object OpenEditLabel : ProfileViewEvent

    data object OpenLanguageSettings : ProfileViewEvent
}
