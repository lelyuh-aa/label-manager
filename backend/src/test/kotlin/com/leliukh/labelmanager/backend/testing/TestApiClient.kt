package com.leliukh.labelmanager.backend.testing

import com.leliukh.labelmanager.backend.api.ApiRequest
import com.leliukh.labelmanager.backend.api.ApiResponse
import com.leliukh.labelmanager.backend.api.LabelBackend
import com.leliukh.labelmanager.contract.dto.auth.AuthResponseDto
import com.leliukh.labelmanager.contract.dto.auth.RegisterRequestDto
import com.leliukh.labelmanager.contract.dto.error.ErrorDto
import com.leliukh.labelmanager.contract.routes.ApiRoutes
import com.leliukh.labelmanager.contract.serialization.ApiJson
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import org.junit.Assert.assertEquals

/**
 * Sends requests to a [LabelBackend] the way an HTTP client would.
 *
 * @param backend the backend under test
 */
class TestApiClient(private val backend: LabelBackend) {

    /**
     * @param method HTTP method
     * @param path request path
     * @param token bearer token, if any
     * @param query query parameters
     * @param body request body as JSON text
     * @return the response
     */
    suspend fun send(
        method: String,
        path: String,
        token: String? = null,
        query: Map<String, String> = emptyMap(),
        body: String? = null,
    ): ApiResponse =
        backend.handle(
            ApiRequest(
                method = method,
                path = path,
                query = query.mapValues { listOf(it.value) },
                headers = token?.let { mapOf("Authorization" to "Bearer $it") }.orEmpty(),
                body = body,
            )
        )

    /**
     * @param request a request with headers of its own
     * @return the response
     */
    suspend fun send(request: ApiRequest): ApiResponse = backend.handle(request)

    /**
     * @param serializer how to write the body
     * @param value the body
     * @return the body as JSON text
     */
    fun <T> json(serializer: SerializationStrategy<T>, value: T): String =
        ApiJson.encodeToString(serializer, value)

    /**
     * @param name local part of the email
     * @return tokens of a new account
     */
    suspend fun register(name: String): AuthResponseDto {
        val body =
            json(
                RegisterRequestDto.serializer(),
                RegisterRequestDto("$name@example.com", "password1"),
            )
        return send("POST", ApiRoutes.REGISTER, body = body)
            .decode(AuthResponseDto.serializer(), 201)
    }
}

/**
 * @param deserializer how to read the body
 * @param status expected status
 * @return the decoded body
 */
fun <T> ApiResponse.decode(deserializer: DeserializationStrategy<T>, status: Int = 200): T {
    assertEquals("Unexpected status, body: $body", status, this.status)
    return ApiJson.decodeFromString(deserializer, requireNotNull(body))
}

/**
 * @param status expected status
 * @return the error body
 */
fun ApiResponse.error(status: Int): ErrorDto = decode(ErrorDto.serializer(), status)
