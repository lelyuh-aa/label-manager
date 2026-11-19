package com.leliukh.labelmanager.core.network.data

import com.leliukh.labelmanager.backend.api.ApiRequest
import com.leliukh.labelmanager.backend.api.LabelBackend
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

/**
 * Network delay the in-process backend imitates, so loading states are as visible as against
 * a real server.
 */
val DefaultSimulatedLatencyMs: LongRange = 150L..450L

/**
 * A Ktor engine that hands every request to the in-process [LabelBackend] instead of the
 * network. The app's client, serialization, auth and error handling run exactly as against
 * the real server; only the transport is replaced.
 *
 * @param backend the in-process API
 * @param latencyMs range of the simulated delay per request; `0L..0L` in tests
 * @param random source of the delay
 */
fun createBackendEngine(
    backend: LabelBackend,
    latencyMs: LongRange = DefaultSimulatedLatencyMs,
    random: Random = Random.Default,
): HttpClientEngine = MockEngine { request ->
    if (latencyMs.last > 0) {
        delay(random.nextLong(latencyMs.first, latencyMs.last + 1).milliseconds)
    }
    val response = backend.handle(request.toApiRequest())
    val body = response.body
    val headers = response.headers
        .mapValues { (_, value) -> listOf(value) } +
            if (body != null) {
                mapOf(HttpHeaders.ContentType to listOf(ContentType.Application.Json.toString()))
            } else {
                emptyMap()
            }
    respond(
        content = body.orEmpty(),
        status = HttpStatusCode.fromValue(response.status),
        headers = headersOf(*headers.toList().toTypedArray()),
    )
}

private suspend fun HttpRequestData.toApiRequest(): ApiRequest = ApiRequest(
    method = method.value,
    path = url.encodedPath,
    query = url.parameters.entries().associate { (name, values) -> name to values },
    headers = headers.entries().associate { (name, values) -> name to values.joinToString(",") },
    body = body.toByteArray().takeIf { it.isNotEmpty() }?.decodeToString(),
)
