package com.leliukh.labelmanager.features.auth.presentation.models

import androidx.annotation.StringRes
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.contract.dto.error.ErrorCodeDto
import com.leliukh.labelmanager.core.network.domain.ApiError
import com.leliukh.labelmanager.core.network.domain.serverCode

/**
 * An error shown in the banner of an auth form.
 *
 * @property titleRes bold first line
 * @property messageRes explanation below it
 */
data class AuthMessageVO(
    @param:StringRes val titleRes: Int,
    @param:StringRes val messageRes: Int,
)

/**
 * Maps a failed sign-in to the banner the user sees. A wrong email or password reads as one
 * error on purpose: telling which one was wrong would reveal which emails have an account.
 */
fun ApiError.toSignInMessage(): AuthMessageVO = when (serverCode) {
    ErrorCodeDto.INVALID_CREDENTIALS -> AuthMessageVO(
        titleRes = R.string.auth_error_credentials_title,
        messageRes = R.string.auth_error_credentials_message,
    )

    else -> toCommonMessage()
}

/**
 * Maps a failed sign-up to the banner the user sees. A taken email is shown on the email field
 * instead, see [isEmailTaken].
 */
fun ApiError.toSignUpMessage(): AuthMessageVO = toCommonMessage()

/** Whether the sign-up failed because the email already has an account. */
val ApiError.isEmailTaken: Boolean
    get() = serverCode == ErrorCodeDto.EMAIL_TAKEN

private fun ApiError.toCommonMessage(): AuthMessageVO = when (this) {
    ApiError.Network ->
        AuthMessageVO(R.string.auth_error_network_title, R.string.auth_error_network_message)

    else ->
        AuthMessageVO(R.string.auth_error_generic_title, R.string.auth_error_generic_message)
}
