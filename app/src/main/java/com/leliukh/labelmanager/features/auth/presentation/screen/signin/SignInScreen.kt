package com.leliukh.labelmanager.features.auth.presentation.screen.signin

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.design_system.presentation.components.button.AppButton
import com.leliukh.labelmanager.core.design_system.presentation.components.field.AppTextField
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppTheme
import com.leliukh.labelmanager.core.ui_utils.presentation.CollectEvents
import com.leliukh.labelmanager.features.auth.presentation.models.AuthMessageVO
import com.leliukh.labelmanager.features.auth.presentation.screen.components.AuthFormLayout
import com.leliukh.labelmanager.features.auth.presentation.viewmodel.signin.SignInViewAction
import com.leliukh.labelmanager.features.auth.presentation.viewmodel.signin.SignInViewEvent
import com.leliukh.labelmanager.features.auth.presentation.viewmodel.signin.SignInViewModel
import com.leliukh.labelmanager.features.auth.presentation.viewmodel.signin.SignInViewState

/**
 * Sign-in screen.
 *
 * @param onSignedIn called once the session is stored
 * @param onOpenSignUp called by the "Create account" link
 * @param onClose called by the back button
 * @param viewModel state holder of the screen
 */
@Composable
fun SignInScreen(
    onSignedIn: () -> Unit,
    onOpenSignUp: () -> Unit,
    onClose: () -> Unit,
    viewModel: SignInViewModel = hiltViewModel(),
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()

    CollectEvents(viewModel.uiEventsFlow) { event ->
        when (event) {
            SignInViewEvent.OpenHome -> onSignedIn()

            SignInViewEvent.OpenSignUp -> onOpenSignUp()

            SignInViewEvent.Close -> onClose()
        }
    }

    SignInContent(state = state, onAction = viewModel::handleUiAction)
}

@Composable
private fun SignInContent(state: SignInViewState, onAction: (SignInViewAction) -> Unit) {
    AuthFormLayout(
        title = stringResource(R.string.sign_in_title),
        subtitle = stringResource(R.string.sign_in_subtitle),
        message = state.message,
        footerText = stringResource(R.string.sign_in_footer),
        footerAction = stringResource(R.string.sign_in_footer_action),
        onFooterAction = { onAction(SignInViewAction.CreateAccountClicked) },
        onBack = { onAction(SignInViewAction.BackClicked) },
    ) {
        AppTextField(
            value = state.email,
            onValueChange = { onAction(SignInViewAction.EmailChanged(it)) },
            label = stringResource(R.string.auth_email_label),
            placeholder = stringResource(R.string.auth_email_placeholder),
            leadingIcon = AppIcons.Mail,
            error = state.emailErrorRes?.let { stringResource(it) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                autoCorrectEnabled = false,
            ),
            contentType = ContentType.Username + ContentType.EmailAddress,
            onFocusLost = { onAction(SignInViewAction.EmailFocusLost) },
        )

        AppTextField(
            value = state.password,
            onValueChange = { onAction(SignInViewAction.PasswordChanged(it)) },
            label = stringResource(R.string.auth_password_label),
            leadingIcon = AppIcons.Lock,
            isPassword = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onAction(SignInViewAction.Submit) }),
            contentType = ContentType.Password,
        )

        AppButton(
            text = stringResource(R.string.sign_in_submit),
            onClick = { onAction(SignInViewAction.Submit) },
            enabled = state.isSubmitEnabled,
            isLoading = state.isSubmitting,
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}

@Preview(name = "Sign in, filled", widthDp = 412, heightDp = 917)
@Composable
private fun SignInFilledPreview() {
    SignInPreview(
        SignInViewState(
            email = "manager@rheinvalley.fm",
            password = "backstage2026",
            isSubmitEnabled = true,
        ),
    )
}

@Preview(name = "Sign in, invalid email", widthDp = 412, heightDp = 917)
@Composable
private fun SignInInvalidEmailPreview() {
    SignInPreview(
        SignInViewState(
            email = "manager@rheinvalley",
            password = "backstage2026",
            emailErrorRes = R.string.auth_email_invalid,
            isSubmitEnabled = true,
        ),
    )
}

@Preview(name = "Sign in, wrong credentials, dark", widthDp = 412, heightDp = 917)
@Composable
private fun SignInServerErrorPreview() {
    SignInPreview(
        state = SignInViewState(
            email = "manager@rheinvalley.fm",
            password = "backstage2026",
            message = AuthMessageVO(
                R.string.auth_error_credentials_title,
                R.string.auth_error_credentials_message,
            ),
            isSubmitEnabled = true,
        ),
        darkTheme = true,
    )
}

@Preview(name = "Sign in, submitting", widthDp = 412, heightDp = 917)
@Composable
private fun SignInSubmittingPreview() {
    SignInPreview(
        SignInViewState(
            email = "manager@rheinvalley.fm",
            password = "backstage2026",
            isSubmitting = true,
        ),
    )
}

@Composable
private fun SignInPreview(state: SignInViewState, darkTheme: Boolean = false) {
    AppTheme(darkTheme = darkTheme) {
        SignInContent(state = state, onAction = {})
    }
}
