package com.leliukh.labelmanager.core.network.data

import com.leliukh.labelmanager.contract.dto.error.ErrorDto
import com.leliukh.labelmanager.contract.serialization.ApiJson
import com.leliukh.labelmanager.core.coroutines.domain.runSuspendCatching
import com.leliukh.labelmanager.core.network.domain.ApiError
import com.leliukh.labelmanager.core.network.domain.ApiResult
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.io.IOException

/**
 * Runs one API call and turns every outcome into an [ApiResult]: a 2xx body becomes
 * [ApiResult.Success], an error body becomes [ApiError.Server], a dropped connection becomes
 * [ApiError.Network]. Cancellation is rethrown, never reported as a failure.
 *
 * The client must be created with `expectSuccess = false` so error statuses reach this
 * function as responses instead of exceptions.
 *
 * @param block sends the request
 */
suspend inline fun <reified T> apiRequest(crossinline block: suspend () -> HttpResponse): ApiResult<T> =
    runSuspendCatching {
        val response = block()
        if (response.status.isSuccess()) {
            ApiResult.Success(response.body<T>())
        } else {
            ApiResult.Failure(response.toServerError())
        }
    }.getOrElse { throwable ->
        ApiResult.Failure(
            when (throwable) {
                is IOException -> ApiError.Network

                else -> ApiError.Unexpected(throwable.message ?: throwable::class.java.name)
            },
        )
    }

/**
 * Reads the [ErrorDto] of a non-2xx response.
 */
suspend fun HttpResponse.toServerError(): ApiError {
    val text = bodyAsText()
    return runSuspendCatching { ApiJson.decodeFromString(ErrorDto.serializer(), text) }
        .map { error ->
            ApiError.Server(
                code = error.code,
                message = error.message,
                httpStatus = status.value,
                fieldErrors = error.fieldErrors,
            )
        }
        .getOrElse { ApiError.Unexpected("HTTP ${status.value}: $text") }
}
