package com.leliukh.labelmanager.features.auth.presentation.viewmodel.signin

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.network.domain.onFailure
import com.leliukh.labelmanager.core.network.domain.onSuccess
import com.leliukh.labelmanager.features.auth.domain.interactor.AuthInteractor
import com.leliukh.labelmanager.features.auth.domain.validation.CredentialsRules
import com.leliukh.labelmanager.features.auth.presentation.models.AuthMessageVO
import com.leliukh.labelmanager.features.auth.presentation.models.toSignInMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val EMAIL_KEY = "email"

/**
 * Sign-in form.
 *
 * The email survives process death through [SavedStateHandle]; the password lives only in
 * memory and is cleared after a successful sign-in. The email is validated once the user
 * leaves the field, not on every keystroke.
 *
 * @param authInteractor signs the user in
 * @param savedStateHandle keeps the typed email
 */
@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authInteractor: AuthInteractor,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val mutableState = MutableStateFlow(
        SignInViewState(email = savedStateHandle[EMAIL_KEY] ?: "").withSubmitAvailability(),
    )
    private val events = Channel<SignInViewEvent>(Channel.BUFFERED)
    private var isEmailTouched = false

    val viewState: StateFlow<SignInViewState> = mutableState.asStateFlow()

    val uiEventsFlow: Flow<SignInViewEvent> = events.receiveAsFlow()

    /**
     * @param action what the user did
     */
    fun handleUiAction(action: SignInViewAction) {
        when (action) {
            is SignInViewAction.EmailChanged -> onEmailChanged(action.email)

            is SignInViewAction.PasswordChanged -> updateState {
                copy(password = action.password, message = null)
            }

            SignInViewAction.EmailFocusLost -> {
                isEmailTouched = mutableState.value.email.isNotBlank()
                updateState { copy(emailErrorRes = emailError(email)) }
            }

            SignInViewAction.Submit -> submit()

            SignInViewAction.CreateAccountClicked -> events.trySend(SignInViewEvent.OpenSignUp)

            SignInViewAction.BackClicked -> events.trySend(SignInViewEvent.Close)
        }
    }

    private fun onEmailChanged(email: String) {
        savedStateHandle[EMAIL_KEY] = email
        // While typing, an error may only go away; a new one waits for the field to lose focus.
        if (mutableState.value.emailErrorRes == null) isEmailTouched = false
        updateState {
            copy(
                email = email,
                emailErrorRes = if (isEmailTouched) emailError(email) else null,
                message = null,
            )
        }
    }

    private fun submit() {
        val state = mutableState.value
        if (!state.isSubmitEnabled) return
        if (!CredentialsRules.isValidEmail(state.email)) {
            isEmailTouched = true
            updateState { copy(emailErrorRes = R.string.auth_email_invalid) }
            return
        }
        updateState { copy(isSubmitting = true, message = null) }
        viewModelScope.launch {
            authInteractor.signIn(state.email, state.password)
                .onSuccess {
                    updateState { copy(password = "", isSubmitting = false) }
                    events.send(SignInViewEvent.OpenHome)
                }
                .onFailure { error ->
                    updateState { copy(isSubmitting = false, message = error.toSignInMessage()) }
                }
        }
    }

    private fun updateState(transform: SignInViewState.() -> SignInViewState) {
        mutableState.update { it.transform().withSubmitAvailability() }
    }

    @StringRes
    private fun emailError(email: String): Int? = when {
        email.isBlank() -> null

        CredentialsRules.isValidEmail(email) -> null

        else -> R.string.auth_email_invalid
    }
}

/**
 * @property email the typed email
 * @property password the typed password, never persisted
 * @property emailErrorRes error under the email field
 * @property message banner above the form after a failed attempt
 * @property isSubmitting whether a request is in flight
 * @property isSubmitEnabled whether the sign-in button reacts
 */
data class SignInViewState(
    val email: String = "",
    val password: String = "",
    @param:StringRes val emailErrorRes: Int? = null,
    val message: AuthMessageVO? = null,
    val isSubmitting: Boolean = false,
    val isSubmitEnabled: Boolean = false,
)

/** What the user does on the sign-in screen. */
sealed interface SignInViewAction {

    /**
     * @property email the new text of the email field
     */
    data class EmailChanged(val email: String) : SignInViewAction

    /**
     * @property password the new text of the password field
     */
    data class PasswordChanged(val password: String) : SignInViewAction

    data object EmailFocusLost : SignInViewAction

    data object Submit : SignInViewAction

    data object CreateAccountClicked : SignInViewAction

    data object BackClicked : SignInViewAction
}

/** One-off results of the sign-in screen. */
sealed interface SignInViewEvent {

    data object OpenHome : SignInViewEvent

    data object OpenSignUp : SignInViewEvent

    data object Close : SignInViewEvent
}

private fun SignInViewState.withSubmitAvailability(): SignInViewState = copy(
    isSubmitEnabled = email.isNotBlank() && password.isNotEmpty() && !isSubmitting,
)
