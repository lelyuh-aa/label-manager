package com.leliukh.labelmanager.backend.api.responses

import com.leliukh.labelmanager.backend.api.ApiResponse
import com.leliukh.labelmanager.backend.domain.errors.DomainException
import com.leliukh.labelmanager.contract.dto.error.ErrorCodeDto
import com.leliukh.labelmanager.contract.dto.error.ErrorDto
import com.leliukh.labelmanager.contract.serialization.ApiJson
import kotlinx.serialization.SerializationStrategy

private val jsonHeaders = mapOf("Content-Type" to "application/json; charset=utf-8")

/**
 * @param serializer how to write the body
 * @param value the body
 * @param status HTTP status
 * @return a JSON response
 */
internal fun <T> jsonResponse(
    serializer: SerializationStrategy<T>,
    value: T,
    status: Int = HttpStatus.OK,
): ApiResponse = ApiResponse(status, ApiJson.encodeToString(serializer, value), jsonHeaders)

/** @return an empty `204 No Content` response */
internal fun noContentResponse(): ApiResponse = ApiResponse(HttpStatus.NO_CONTENT, body = null)

/**
 * @param status HTTP status
 * @param code machine-readable reason
 * @param message human-readable explanation
 * @param fieldErrors invalid fields
 * @return an error response with an [ErrorDto] body
 */
internal fun errorResponse(
    status: Int,
    code: ErrorCodeDto,
    message: String,
    fieldErrors: Map<String, String> = emptyMap(),
): ApiResponse = jsonResponse(ErrorDto.serializer(), ErrorDto(code, message, fieldErrors), status)

/**
 * Maps a failure to the error response the API documents for it. Anything that is not a
 * [DomainException] is an internal error, reported without details.
 *
 * @return the error response
 */
internal fun Throwable.toErrorResponse(): ApiResponse {
    val message = message.orEmpty()
    return when (this) {
        is DomainException.ValidationFailed ->
            errorResponse(
                HttpStatus.UNPROCESSABLE,
                ErrorCodeDto.VALIDATION_FAILED,
                message,
                fieldErrors,
            )

        is DomainException.EmailTaken ->
            errorResponse(HttpStatus.CONFLICT, ErrorCodeDto.EMAIL_TAKEN, message)

        is DomainException.InvalidCredentials ->
            errorResponse(HttpStatus.UNAUTHORIZED, ErrorCodeDto.INVALID_CREDENTIALS, message)

        is DomainException.Unauthorized ->
            errorResponse(HttpStatus.UNAUTHORIZED, ErrorCodeDto.UNAUTHORIZED, message)

        is DomainException.Forbidden ->
            errorResponse(HttpStatus.FORBIDDEN, ErrorCodeDto.FORBIDDEN, message)

        is DomainException.NotFound ->
            errorResponse(HttpStatus.NOT_FOUND, ErrorCodeDto.NOT_FOUND, message)

        is DomainException.ArtistAlreadyListed ->
            errorResponse(HttpStatus.CONFLICT, ErrorCodeDto.ARTIST_ALREADY_LISTED, message)

        is DomainException.ListingNotAvailable ->
            errorResponse(HttpStatus.CONFLICT, ErrorCodeDto.LISTING_NOT_AVAILABLE, message)

        is DomainException.CannotBuyOwnArtist ->
            errorResponse(HttpStatus.CONFLICT, ErrorCodeDto.CANNOT_BUY_OWN_ARTIST, message)

        is DomainException.InsufficientBudget ->
            errorResponse(HttpStatus.CONFLICT, ErrorCodeDto.INSUFFICIENT_BUDGET, message)

        else ->
            errorResponse(
                HttpStatus.INTERNAL_ERROR,
                ErrorCodeDto.INTERNAL,
                "Unexpected server error",
            )
    }
}

/** HTTP status codes the API uses. */
internal object HttpStatus {
    const val OK = 200
    const val CREATED = 201
    const val NO_CONTENT = 204
    const val UNAUTHORIZED = 401
    const val FORBIDDEN = 403
    const val NOT_FOUND = 404
    const val METHOD_NOT_ALLOWED = 405
    const val CONFLICT = 409
    const val UNPROCESSABLE = 422
    const val INTERNAL_ERROR = 500
}
