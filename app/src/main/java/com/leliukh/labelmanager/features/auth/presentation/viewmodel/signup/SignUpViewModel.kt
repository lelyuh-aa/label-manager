package com.leliukh.labelmanager.features.auth.presentation.viewmodel.signup

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
import com.leliukh.labelmanager.features.auth.presentation.models.isEmailTaken
import com.leliukh.labelmanager.features.auth.presentation.models.toSignUpMessage
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
 * Account creation form.
 *
 * The password is typed twice and checked against the rules as the user types. Both
 * passwords live only in memory and are cleared once the account exists; the email survives
 * process death through [SavedStateHandle]. Field errors appear once the user leaves a field,
 * and disappear as soon as the value becomes valid.
 *
 * @param authInteractor creates the account
 * @param savedStateHandle keeps the typed email
 */
@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authInteractor: AuthInteractor,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private var isEmailTouched = false
    private var isRepeatTouched = false
    private var takenEmail: String? = null
    private val mutableState = MutableStateFlow(
        recompute(SignUpViewState(email = savedStateHandle[EMAIL_KEY] ?: "")),
    )
    private val events = Channel<SignUpViewEvent>(Channel.BUFFERED)

    val viewState: StateFlow<SignUpViewState> = mutableState.asStateFlow()

    val uiEventsFlow: Flow<SignUpViewEvent> = events.receiveAsFlow()

    /**
     * @param action what the user did
     */
    fun handleUiAction(action: SignUpViewAction) {
        when (action) {
            is SignUpViewAction.EmailChanged -> {
                savedStateHandle[EMAIL_KEY] = action.email
                // While typing, an error may only go away; a new one waits for the field to lose focus.
                if (mutableState.value.emailErrorRes == null) isEmailTouched = false
                updateState { copy(email = action.email, message = null) }
            }

            is SignUpViewAction.PasswordChanged ->
                updateState { copy(password = action.password, message = null) }

            is SignUpViewAction.RepeatPasswordChanged -> {
                if (mutableState.value.repeatPasswordErrorRes == null) isRepeatTouched = false
                updateState { copy(repeatPassword = action.password, message = null) }
            }

            SignUpViewAction.EmailFocusLost -> {
                isEmailTouched = mutableState.value.email.isNotBlank()
                updateState { this }
            }

            SignUpViewAction.RepeatPasswordFocusLost -> {
                isRepeatTouched = mutableState.value.repeatPassword.isNotEmpty()
                updateState { this }
            }

            SignUpViewAction.Submit -> submit()

            SignUpViewAction.SignInClicked -> events.trySend(SignUpViewEvent.OpenSignIn)

            SignUpViewAction.BackClicked -> events.trySend(SignUpViewEvent.Close)
        }
    }

    private fun submit() {
        val state = mutableState.value
        if (!state.isSubmitEnabled) return
        updateState { copy(isSubmitting = true, message = null) }
        viewModelScope.launch {
            authInteractor.signUp(state.email, state.password)
                .onSuccess {
                    updateState { copy(password = "", repeatPassword = "", isSubmitting = false) }
                    events.send(SignUpViewEvent.OpenLabelReady)
                }
                .onFailure { error ->
                    if (error.isEmailTaken) {
                        takenEmail = state.email.trim()
                        isEmailTouched = true
                        updateState { copy(isSubmitting = false) }
                    } else {
                        updateState {
                            copy(isSubmitting = false, message = error.toSignUpMessage())
                        }
                    }
                }
        }
    }

    private fun updateState(transform: SignUpViewState.() -> SignUpViewState) {
        mutableState.update { recompute(it.transform()) }
    }

    private fun recompute(state: SignUpViewState): SignUpViewState {
        val checks = CredentialsRules.checkNewPassword(state.password)
        val isEmailValid = CredentialsRules.isValidEmail(state.email)
        val isEmailTaken = takenEmail != null && takenEmail == state.email.trim()
        val passwordsMatch = state.repeatPassword == state.password
        // A repeat that is still a beginning of the password may just be unfinished.
        val repeatMayMatch = state.repeatPassword.length < state.password.length &&
            state.password.startsWith(state.repeatPassword)
        return state.copy(
            emailErrorRes = when {
                isEmailTaken -> R.string.auth_email_taken

                isEmailTouched && !isEmailValid -> R.string.auth_email_invalid

                else -> null
            },
            repeatPasswordErrorRes = if (
                !passwordsMatch &&
                state.repeatPassword.isNotEmpty() &&
                (isRepeatTouched || !repeatMayMatch)
            ) {
                R.string.auth_password_mismatch
            } else {
                null
            },
            passwordRules = listOf(
                PasswordRuleVO(R.string.auth_password_rule_length, checks.hasMinLength),
                PasswordRuleVO(R.string.auth_password_rule_letter, checks.hasLetter),
                PasswordRuleVO(R.string.auth_password_rule_digit, checks.hasDigit),
            ),
            isSubmitEnabled = isEmailValid &&
                !isEmailTaken &&
                checks.isValid &&
                passwordsMatch &&
                !state.isSubmitting,
        )
    }
}

/**
 * @property email the typed email
 * @property password the typed password, never persisted
 * @property repeatPassword the password typed again, never persisted
 * @property emailErrorRes error under the email field
 * @property repeatPasswordErrorRes error under the repeat field
 * @property passwordRules the password requirements and whether each is met
 * @property message banner above the form after a failed attempt
 * @property isSubmitting whether a request is in flight
 * @property isSubmitEnabled whether the create button reacts
 */
data class SignUpViewState(
    val email: String = "",
    val password: String = "",
    val repeatPassword: String = "",
    @param:StringRes val emailErrorRes: Int? = null,
    @param:StringRes val repeatPasswordErrorRes: Int? = null,
    val passwordRules: List<PasswordRuleVO> = emptyList(),
    val message: AuthMessageVO? = null,
    val isSubmitting: Boolean = false,
    val isSubmitEnabled: Boolean = false,
)

/**
 * One password requirement.
 *
 * @property labelRes what is required
 * @property isMet whether the current password meets it
 */
data class PasswordRuleVO(
    @param:StringRes val labelRes: Int,
    val isMet: Boolean,
)

/** What the user does on the sign-up screen. */
sealed interface SignUpViewAction {

    /**
     * @property email the new text of the email field
     */
    data class EmailChanged(val email: String) : SignUpViewAction

    /**
     * @property password the new text of the password field
     */
    data class PasswordChanged(val password: String) : SignUpViewAction

    /**
     * @property password the new text of the repeat field
     */
    data class RepeatPasswordChanged(val password: String) : SignUpViewAction

    data object EmailFocusLost : SignUpViewAction

    data object RepeatPasswordFocusLost : SignUpViewAction

    data object Submit : SignUpViewAction

    data object SignInClicked : SignUpViewAction

    data object BackClicked : SignUpViewAction
}

/** One-off results of the sign-up screen. */
sealed interface SignUpViewEvent {

    data object OpenLabelReady : SignUpViewEvent

    data object OpenSignIn : SignUpViewEvent

    data object Close : SignUpViewEvent
}
