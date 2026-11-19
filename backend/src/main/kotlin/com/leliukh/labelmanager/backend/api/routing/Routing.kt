package com.leliukh.labelmanager.backend.api.routing

import com.leliukh.labelmanager.backend.api.ApiRequest
import com.leliukh.labelmanager.backend.api.ApiResponse
import com.leliukh.labelmanager.backend.domain.errors.DomainException
import com.leliukh.labelmanager.backend.security.AccessClaims
import com.leliukh.labelmanager.contract.serialization.ApiJson
import kotlinx.serialization.DeserializationStrategy

/**
 * Everything a route handler needs about the request being handled.
 *
 * @param request the request
 * @param pathParameters values of the `{name}` placeholders of the matched route
 * @param authenticator turns a bearer token into claims or throws
 */
internal class CallContext(
    val request: ApiRequest,
    private val pathParameters: Map<String, String>,
    private val authenticator: suspend (String) -> AccessClaims,
) {

    private val headers = request.headers.mapKeys { it.key.lowercase() }

    /**
     * @param name placeholder name
     * @return the placeholder's value
     */
    fun path(name: String): String = pathParameters.getValue(name)

    /**
     * @param name query parameter name
     * @return the first value, or null when absent or blank
     */
    fun query(name: String): String? =
        request.query[name]?.firstOrNull()?.takeIf(String::isNotBlank)

    /**
     * @return claims of the bearer token in the `Authorization` header
     * @throws DomainException.Unauthorized when the header is missing or the token is invalid
     */
    suspend fun claims(): AccessClaims {
        val header =
            headers["authorization"]
                ?: throw DomainException.Unauthorized("The Authorization header is missing")
        val token =
            header.removePrefix(BEARER_PREFIX).takeIf { header.startsWith(BEARER_PREFIX) }
                ?: throw DomainException.Unauthorized("Use a bearer token")
        return authenticator(token.trim())
    }

    /**
     * @param deserializer how to read the body
     * @return the decoded body
     * @throws DomainException.ValidationFailed when the body is missing or malformed
     */
    fun <T> body(deserializer: DeserializationStrategy<T>): T =
        request.body?.let { body ->
            runCatching { ApiJson.decodeFromString(deserializer, body) }.getOrNull()
        }
            ?: throw DomainException.ValidationFailed(
                mapOf("body" to "is missing or not valid JSON for this endpoint")
            )

    private companion object {
        const val BEARER_PREFIX = "Bearer "
    }
}

/**
 * One endpoint: a method and a path template with `{name}` placeholders.
 *
 * @param method HTTP method, upper case
 * @param template path template, e.g. `/api/v1/artists/{artistId}`
 * @param handler produces the response
 */
internal class Route(
    val method: String,
    template: String,
    val handler: suspend CallContext.() -> ApiResponse,
) {

    private val segments = template.trim('/').split('/')

    /**
     * @param path a request path
     * @return the placeholder values when the path fits the template, else null
     */
    fun match(path: String): Map<String, String>? {
        val parts = path.trim('/').split('/')
        if (parts.size != segments.size) return null
        val parameters = mutableMapOf<String, String>()
        segments.zip(parts).forEach { (segment, part) ->
            when {
                segment.startsWith("{") && segment.endsWith("}") && part.isNotEmpty() ->
                    parameters[segment.removeSurrounding("{", "}")] = part

                segment != part -> return null
            }
        }
        return parameters
    }
}

/** The outcome of looking a request up in a [Router]. */
internal sealed interface RouteMatch {

    /**
     * @param route the matching route
     * @param pathParameters its placeholder values
     */
    class Found(val route: Route, val pathParameters: Map<String, String>) : RouteMatch

    data object MethodNotAllowed : RouteMatch

    data object NotFound : RouteMatch
}

/**
 * Finds the route of a request.
 *
 * @param routes every endpoint of the API
 */
internal class Router(private val routes: List<Route>) {

    /**
     * @param method HTTP method
     * @param path request path
     * @return the matching route, or why there is none
     */
    fun find(method: String, path: String): RouteMatch {
        val candidates = routes.mapNotNull { route -> route.match(path)?.let { route to it } }
        val found = candidates.firstOrNull { it.first.method.equals(method, ignoreCase = true) }
        return when {
            found != null -> RouteMatch.Found(found.first, found.second)

            candidates.isNotEmpty() -> RouteMatch.MethodNotAllowed

            else -> RouteMatch.NotFound
        }
    }
}

/** Collects routes with a small DSL. */
internal class RoutesBuilder {

    private val routes = mutableListOf<Route>()

    /**
     * @param template path template
     * @param handler produces the response
     */
    fun get(template: String, handler: suspend CallContext.() -> ApiResponse) {
        routes += Route("GET", template, handler)
    }

    /**
     * @param template path template
     * @param handler produces the response
     */
    fun post(template: String, handler: suspend CallContext.() -> ApiResponse) {
        routes += Route("POST", template, handler)
    }

    /**
     * @param template path template
     * @param handler produces the response
     */
    fun patch(template: String, handler: suspend CallContext.() -> ApiResponse) {
        routes += Route("PATCH", template, handler)
    }

    /**
     * @param template path template
     * @param handler produces the response
     */
    fun delete(template: String, handler: suspend CallContext.() -> ApiResponse) {
        routes += Route("DELETE", template, handler)
    }

    /** @return a router over the collected routes */
    fun build(): Router = Router(routes.toList())
}
