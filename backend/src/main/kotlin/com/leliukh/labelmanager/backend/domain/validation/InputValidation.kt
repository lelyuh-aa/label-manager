package com.leliukh.labelmanager.backend.domain.validation

import com.leliukh.labelmanager.backend.domain.errors.DomainException
import com.leliukh.labelmanager.contract.dto.reference.Countries
import com.leliukh.labelmanager.contract.rules.ContractLimits

/**
 * Collects field errors of one request and throws them together, so a client learns about every
 * invalid field at once.
 */
class InputValidation {

    private val errors = linkedMapOf<String, String>()

    /**
     * @param field field name reported to the client
     * @param email the email to check
     */
    fun email(field: String, email: String) {
        when {
            email.length > ContractLimits.EMAIL_MAX_LENGTH -> errors[field] = "is too long"

            !EMAIL_PATTERN.matches(email) -> errors[field] = "is not a valid email"
        }
    }

    /**
     * @param field field name reported to the client
     * @param password the password to check
     */
    fun password(field: String, password: String) {
        if (
            password.length !in
                ContractLimits.PASSWORD_MIN_LENGTH..ContractLimits.PASSWORD_MAX_LENGTH
        ) {
            errors[field] =
                "must be ${ContractLimits.PASSWORD_MIN_LENGTH}–" +
                    "${ContractLimits.PASSWORD_MAX_LENGTH} characters long"
        }
    }

    /**
     * @param field field name reported to the client
     * @param value trimmed text to check
     * @param range allowed length
     */
    fun length(field: String, value: String, range: IntRange) {
        if (value.length !in range)
            errors[field] = "must be ${range.first}–${range.last} characters long"
    }

    /**
     * @param field field name reported to the client
     * @param countryCode ISO 3166-1 alpha-2 code to check
     */
    fun country(field: String, countryCode: String) {
        if (Countries.find(countryCode) == null) errors[field] = "is not a supported country code"
    }

    /**
     * @param field field name reported to the client
     * @param price amount in whole US dollars
     */
    fun price(field: String, price: Long) {
        if (price !in ContractLimits.MIN_ASKING_PRICE..ContractLimits.MAX_ASKING_PRICE) {
            errors[field] =
                "must be between ${ContractLimits.MIN_ASKING_PRICE} and " +
                    "${ContractLimits.MAX_ASKING_PRICE}"
        }
    }

    /**
     * @param field field name reported to the client
     * @param message the problem
     */
    fun reject(field: String, message: String) {
        errors[field] = message
    }

    /** @throws DomainException.ValidationFailed when any check failed */
    fun throwIfInvalid() {
        if (errors.isNotEmpty()) throw DomainException.ValidationFailed(errors.toMap())
    }

    private companion object {
        val EMAIL_PATTERN = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    }
}

/**
 * Runs checks and throws their combined field errors.
 *
 * @param checks the checks to run
 * @throws DomainException.ValidationFailed when any check failed
 */
inline fun validate(checks: InputValidation.() -> Unit) {
    InputValidation().apply(checks).throwIfInvalid()
}
