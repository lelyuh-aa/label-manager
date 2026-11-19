package com.leliukh.labelmanager.contract.dto.error

import kotlinx.serialization.Serializable

/**
 * Machine-readable reason of a failed request. Clients branch on this, never on
 * [ErrorDto.message].
 */
@Serializable
public enum class ErrorCodeDto {
    /** 422: a field is missing or out of range, see [ErrorDto.fieldErrors]. */
    VALIDATION_FAILED,

    /** 409: an account with this email already exists. */
    EMAIL_TAKEN,

    /** 401: wrong email or password. */
    INVALID_CREDENTIALS,

    /** 401: the access token is missing, malformed or expired, or the refresh token is invalid. */
    UNAUTHORIZED,

    /** 403: the resource belongs to another label. */
    FORBIDDEN,

    /** 404: no resource with this id. */
    NOT_FOUND,

    /** 409: the artist already has an open listing. */
    ARTIST_ALREADY_LISTED,

    /** 409: the listing was sold or withdrawn in the meantime. */
    LISTING_NOT_AVAILABLE,

    /** 409: the caller tried to buy their own artist. */
    CANNOT_BUY_OWN_ARTIST,

    /** 409: the caller's budget is below the asking price. */
    INSUFFICIENT_BUDGET,

    /** 500: unexpected server failure. */
    INTERNAL,
}

/**
 * Body of every non-2xx response.
 *
 * @property code machine-readable reason
 * @property message human-readable explanation in English, for logs
 * @property fieldErrors field name to problem description, for [ErrorCodeDto.VALIDATION_FAILED]
 */
@Serializable
public data class ErrorDto(
    val code: ErrorCodeDto,
    val message: String,
    val fieldErrors: Map<String, String> = emptyMap(),
)
