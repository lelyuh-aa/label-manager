package com.leliukh.labelmanager.features.auth.domain.validation

import com.leliukh.labelmanager.contract.rules.ContractLimits

private val EmailPattern = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$")

/**
 * Which password requirements a candidate password meets.
 *
 * @property hasMinLength at least [ContractLimits.PASSWORD_MIN_LENGTH] characters
 * @property hasLetter at least one letter
 * @property hasDigit at least one digit
 */
data class PasswordChecks(
    val hasMinLength: Boolean,
    val hasLetter: Boolean,
    val hasDigit: Boolean,
) {

    /** Whether every requirement is met. */
    val isValid: Boolean
        get() = hasMinLength && hasLetter && hasDigit
}

/**
 * Client-side checks of credentials, run before a request is sent.
 *
 * New passwords must have a letter and a digit on top of the server's minimum length. Sign-in
 * does not check them: an account may have been created by another client with only the
 * server's rules.
 */
object CredentialsRules {

    /**
     * @param email the email as typed
     * @return whether it looks like an email address
     */
    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        return trimmed.length <= ContractLimits.EMAIL_MAX_LENGTH && EmailPattern.matches(trimmed)
    }

    /**
     * @param password a candidate new password
     */
    fun checkNewPassword(password: String): PasswordChecks = PasswordChecks(
        hasMinLength = password.length in
            ContractLimits.PASSWORD_MIN_LENGTH..ContractLimits.PASSWORD_MAX_LENGTH,
        hasLetter = password.any(Char::isLetter),
        hasDigit = password.any(Char::isDigit),
    )
}
