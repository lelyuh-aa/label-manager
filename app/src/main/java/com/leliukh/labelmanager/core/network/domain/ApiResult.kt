package com.leliukh.labelmanager.core.network.domain

import com.leliukh.labelmanager.contract.dto.error.ErrorCodeDto

/**
 * Outcome of an API call. Repositories return it instead of throwing, and callers chain
 * [onSuccess] and [onFailure].
 */
sealed interface ApiResult<out T> {

    /**
     * @property data the response body
     */
    data class Success<T>(val data: T) : ApiResult<T>

    /**
     * @property error why the call failed
     */
    data class Failure(val error: ApiError) : ApiResult<Nothing>
}

/**
 * Why an API call failed.
 */
sealed interface ApiError {

    /**
     * The server answered with an error body.
     *
     * @property code machine-readable reason
     * @property message server message, for logs only
     * @property httpStatus HTTP status code
     * @property fieldErrors field name to problem, for validation errors
     */
    data class Server(
        val code: ErrorCodeDto,
        val message: String,
        val httpStatus: Int,
        val fieldErrors: Map<String, String> = emptyMap(),
    ) : ApiError

    /** The request never reached the server or the connection dropped. */
    data object Network : ApiError

    /**
     * Anything else, such as a body that does not match the contract.
     *
     * @property message description for logs
     */
    data class Unexpected(val message: String) : ApiError
}

/**
 * @param action called with the data of a successful result
 */
inline fun <T> ApiResult<T>.onSuccess(action: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) action(data)
    return this
}

/**
 * @param action called with the error of a failed result
 */
inline fun <T> ApiResult<T>.onFailure(action: (ApiError) -> Unit): ApiResult<T> {
    if (this is ApiResult.Failure) action(error)
    return this
}

/**
 * @param transform maps the data of a successful result
 */
inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(data))

    is ApiResult.Failure -> this
}

/**
 * The [ErrorCodeDto] of a server error, or null for network and unexpected errors.
 */
val ApiError.serverCode: ErrorCodeDto?
    get() = (this as? ApiError.Server)?.code
