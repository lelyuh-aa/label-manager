package com.leliukh.labelmanager.backend.api

import com.leliukh.labelmanager.backend.api.responses.HttpStatus
import com.leliukh.labelmanager.backend.api.responses.errorResponse
import com.leliukh.labelmanager.backend.api.responses.toErrorResponse
import com.leliukh.labelmanager.backend.api.routing.CallContext
import com.leliukh.labelmanager.backend.api.routing.RouteMatch
import com.leliukh.labelmanager.backend.api.routing.Router
import com.leliukh.labelmanager.backend.coroutines.runSuspendCatching
import com.leliukh.labelmanager.backend.security.AccessClaims
import com.leliukh.labelmanager.backend.simulation.RivalMarketSimulator
import com.leliukh.labelmanager.contract.dto.error.ErrorCodeDto

/**
 * The Label Manager REST API, independent of any HTTP library.
 *
 * Both transports call [handle]: the Ktor server for real HTTP requests and the app's mock flavor
 * from an in-process HTTP client engine. Every failure becomes the documented error response;
 * nothing is thrown to the caller except cancellation.
 *
 * Create one with [com.leliukh.labelmanager.backend.LabelBackendFactory].
 *
 * Before every request the rival labels simulation catches up with the time that passed, so the
 * market moves even without a background timer.
 *
 * @param router finds the endpoint of a request
 * @param authenticator verifies bearer tokens
 * @param simulator rival labels trading on the market
 */
class LabelBackend
internal constructor(
    private val router: Router,
    private val authenticator: suspend (String) -> AccessClaims,
    private val simulator: RivalMarketSimulator,
) {

    /**
     * Runs the rival labels simulation up to now, creating the rival labels on the first call. A
     * server calls it on a timer; requests call it on their own.
     */
    suspend fun advanceSimulation() {
        simulator.catchUp()
    }

    /**
     * @param request the HTTP request
     * @return the HTTP response
     */
    suspend fun handle(request: ApiRequest): ApiResponse = runSuspendCatching {
        simulator.catchUp()
        when (val match = router.find(request.method, request.path)) {
            is RouteMatch.Found ->
                CallContext(request, match.pathParameters, authenticator).run {
                    match.route.handler(this)
                }

            RouteMatch.MethodNotAllowed ->
                errorResponse(
                    HttpStatus.METHOD_NOT_ALLOWED,
                    ErrorCodeDto.NOT_FOUND,
                    "${request.method} is not supported for ${request.path}",
                )

            RouteMatch.NotFound ->
                errorResponse(
                    HttpStatus.NOT_FOUND,
                    ErrorCodeDto.NOT_FOUND,
                    "No endpoint at ${request.path}",
                )
        }
    }
        .getOrElse { it.toErrorResponse() }
}
