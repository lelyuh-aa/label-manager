package com.leliukh.labelmanager.features.auth.presentation.screen.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.leliukh.labelmanager.features.auth.presentation.screen.components.AuthFormLayout
import com.leliukh.labelmanager.features.auth.presentation.screen.components.PasswordRules
import com.leliukh.labelmanager.features.auth.presentation.viewmodel.signup.PasswordRuleVO
import com.leliukh.labelmanager.features.auth.presentation.viewmodel.signup.SignUpViewAction
import com.leliukh.labelmanager.features.auth.presentation.viewmodel.signup.SignUpViewEvent
import com.leliukh.labelmanager.features.auth.presentation.viewmodel.signup.SignUpViewModel
import com.leliukh.labelmanager.features.auth.presentation.viewmodel.signup.SignUpViewState

/**
 * Account creation screen: email, a password checked against the rules as it is typed, and
 * the same password again.
 *
 * Both password fields are marked as a new password, so a password manager can suggest a
 * strong one and save it; the app itself never stores it.
 *
 * @param onSignedUp called once the account exists and the session is stored
 * @param onOpenSignIn called by the "Sign in" link
 * @param onClose called by the back button
 * @param viewModel state holder of the screen
 */
@Composable
fun SignUpScreen(
    onSignedUp: () -> Unit,
    onOpenSignIn: () -> Unit,
    onClose: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel(),
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()

    CollectEvents(viewModel.uiEventsFlow) { event ->
        when (event) {
            SignUpViewEvent.OpenLabelReady -> onSignedUp()

            SignUpViewEvent.OpenSignIn -> onOpenSignIn()

            SignUpViewEvent.Close -> onClose()
        }
    }

    SignUpContent(state = state, onAction = viewModel::handleUiAction)
}

@Composable
private fun SignUpContent(state: SignUpViewState, onAction: (SignUpViewAction) -> Unit) {
    AuthFormLayout(
        title = stringResource(R.string.sign_up_title),
        subtitle = stringResource(R.string.sign_up_subtitle),
        message = state.message,
        footerText = stringResource(R.string.sign_up_footer),
        footerAction = stringResource(R.string.sign_up_footer_action),
        onFooterAction = { onAction(SignUpViewAction.SignInClicked) },
        onBack = { onAction(SignUpViewAction.BackClicked) },
    ) {
        AppTextField(
            value = state.email,
            onValueChange = { onAction(SignUpViewAction.EmailChanged(it)) },
            label = stringResource(R.string.auth_email_label),
            placeholder = stringResource(R.string.auth_email_placeholder),
            leadingIcon = AppIcons.Mail,
            error = state.emailErrorRes?.let { stringResource(it) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                autoCorrectEnabled = false,
            ),
            contentType = ContentType.EmailAddress,
            onFocusLost = { onAction(SignUpViewAction.EmailFocusLost) },
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AppTextField(
                value = state.password,
                onValueChange = { onAction(SignUpViewAction.PasswordChanged(it)) },
                label = stringResource(R.string.auth_password_label),
                leadingIcon = AppIcons.Lock,
                isPassword = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                contentType = ContentType.NewPassword,
            )

            PasswordRules(rules = state.passwordRules)
        }

        AppTextField(
            value = state.repeatPassword,
            onValueChange = { onAction(SignUpViewAction.RepeatPasswordChanged(it)) },
            label = stringResource(R.string.auth_repeat_password_label),
            leadingIcon = AppIcons.Lock,
            isPassword = true,
            error = state.repeatPasswordErrorRes?.let { stringResource(it) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onAction(SignUpViewAction.Submit) }),
            contentType = ContentType.NewPassword,
            onFocusLost = { onAction(SignUpViewAction.RepeatPasswordFocusLost) },
        )

        AppButton(
            text = stringResource(R.string.sign_up_submit),
            onClick = { onAction(SignUpViewAction.Submit) },
            enabled = state.isSubmitEnabled,
            isLoading = state.isSubmitting,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Preview(name = "Sign up, empty", widthDp = 412, heightDp = 917)
@Composable
private fun SignUpEmptyPreview() {
    SignUpPreview(SignUpViewState(passwordRules = rules(false, false, false)))
}

@Preview(name = "Sign up, passwords don't match", widthDp = 412, heightDp = 917)
@Composable
private fun SignUpMismatchPreview() {
    SignUpPreview(
        SignUpViewState(
            email = "manager@rheinvalley.fm",
            password = "backstage7",
            repeatPassword = "backstage8",
            repeatPasswordErrorRes = R.string.auth_password_mismatch,
            passwordRules = rules(true, true, true),
        ),
    )
}

@Preview(name = "Sign up, email taken, dark", widthDp = 412, heightDp = 917)
@Composable
private fun SignUpEmailTakenPreview() {
    SignUpPreview(
        state = SignUpViewState(
            email = "manager@rheinvalley.fm",
            password = "backstage7",
            repeatPassword = "backstage7",
            emailErrorRes = R.string.auth_email_taken,
            passwordRules = rules(true, true, true),
        ),
        darkTheme = true,
    )
}

@Preview(name = "Sign up, ready", widthDp = 412, heightDp = 917)
@Composable
private fun SignUpReadyPreview() {
    SignUpPreview(
        SignUpViewState(
            email = "manager@rheinvalley.fm",
            password = "backstage7",
            repeatPassword = "backstage7",
            passwordRules = rules(true, true, true),
            isSubmitEnabled = true,
        ),
    )
}

@Composable
private fun SignUpPreview(state: SignUpViewState, darkTheme: Boolean = false) {
    AppTheme(darkTheme = darkTheme) {
        SignUpContent(state = state, onAction = {})
    }
}

private fun rules(length: Boolean, letter: Boolean, digit: Boolean) = listOf(
    PasswordRuleVO(R.string.auth_password_rule_length, length),
    PasswordRuleVO(R.string.auth_password_rule_letter, letter),
    PasswordRuleVO(R.string.auth_password_rule_digit, digit),
)
