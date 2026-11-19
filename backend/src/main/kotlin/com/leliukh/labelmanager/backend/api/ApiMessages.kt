package com.leliukh.labelmanager.backend.api

/**
 * A transport-neutral HTTP request, built by the Ktor server or by the app's in-process engine.
 *
 * @property method HTTP method, upper case
 * @property path request path without the query string, e.g. `/api/v1/label`
 * @property query query parameters by name; a name may repeat
 * @property headers request headers; names are matched case-insensitively
 * @property body request body as text, or null when there is none
 */
data class ApiRequest(
    val method: String,
    val path: String,
    val query: Map<String, List<String>>,
    val headers: Map<String, String>,
    val body: String?,
)

/**
 * A transport-neutral HTTP response.
 *
 * @property status HTTP status code
 * @property body JSON body, or null for an empty response
 * @property headers response headers
 */
data class ApiResponse(
    val status: Int,
    val body: String?,
    val headers: Map<String, String> = emptyMap(),
)
