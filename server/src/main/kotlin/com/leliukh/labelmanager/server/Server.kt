package com.leliukh.labelmanager.server

import com.leliukh.labelmanager.backend.BackendConfig
import com.leliukh.labelmanager.backend.SimulationConfig
import com.leliukh.labelmanager.backend.LabelBackendFactory
import com.leliukh.labelmanager.backend.api.ApiRequest
import com.leliukh.labelmanager.backend.api.ApiResponse
import com.leliukh.labelmanager.backend.api.LabelBackend
import com.leliukh.labelmanager.backend.coroutines.runSuspendCatching
import com.leliukh.labelmanager.server.data.ExposedBackendStore
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlin.time.Duration
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Starts the Label Manager API server with settings from the environment. */
fun main() {
    val config = ServerConfig.fromEnvironment()
    val backend = createBackend(config)
    embeddedServer(Netty, port = config.port) {
            if (config.usesDevelopmentSecret) {
                log.warn("LABEL_JWT_SECRET is not set: tokens are signed with the development key")
            }
            labelManagerModule(
                backend,
                config.simulationInterval.takeIf { config.isSimulationEnabled },
            )
        }
        .start(wait = true)
}

/**
 * @param config server settings
 * @return a backend over the SQLite database of the configuration
 */
fun createBackend(config: ServerConfig): LabelBackend =
    LabelBackendFactory.create(
        store = ExposedBackendStore.sqlite(config.databaseFile),
        config =
            BackendConfig(
                tokenSecret = config.tokenSecret,
                simulation = SimulationConfig(isEnabled = config.isSimulationEnabled),
            ),
    )

private const val DOCS_PATH = "docs"
private const val OPENAPI_RESOURCE = "openapi/documentation.yaml"

/**
 * Serves the API: every request under `/api` goes to [backend]. Adds request logging, CORS for
 * browser front ends, a JSON body for unexpected errors and the API documentation: Swagger UI at
 * `/docs` and the OpenAPI file at `/openapi.yaml`.
 *
 * @param backend handles the API requests
 * @param simulationInterval how often to advance the rival labels simulation; null disables the
 *   timer, requests still advance it
 */
fun Application.labelManagerModule(backend: LabelBackend, simulationInterval: Duration?) {
    install(CallLogging)
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled failure", cause)
            call.respondText(
                text = """{"code":"INTERNAL","message":"Unexpected server error"}""",
                contentType = ContentType.Application.Json,
                status = HttpStatusCode.InternalServerError,
            )
        }
    }
    routing {
        route("/api/{...}") { handle { call.respondApi(backend.handle(call.toApiRequest())) } }
        swaggerUI(path = DOCS_PATH, swaggerFile = OPENAPI_RESOURCE)
        get("/openapi.yaml") {
            val spec =
                ServerConfig::class.java.classLoader.getResource(OPENAPI_RESOURCE)?.readText()
            if (spec == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respondText(spec, ContentType.parse("application/yaml"))
            }
        }
        get("/") { call.respondRedirect("/$DOCS_PATH") }
    }
    if (simulationInterval != null) {
        launch {
            while (isActive) {
                delay(simulationInterval)
                runSuspendCatching { backend.advanceSimulation() }
                    .onFailure { log.error("Rival labels simulation failed", it) }
            }
        }
    }
}

private suspend fun ApplicationCall.toApiRequest(): ApiRequest =
    ApiRequest(
        method = request.httpMethod.value,
        path = request.path(),
        query = request.queryParameters.entries().associate { it.key to it.value },
        headers = request.headers.entries().associate { it.key to it.value.joinToString(",") },
        body = receiveText().ifEmpty { null },
    )

private suspend fun ApplicationCall.respondApi(response: ApiResponse) {
    val status = HttpStatusCode.fromValue(response.status)
    val body = response.body
    if (body == null) {
        respond(status)
    } else {
        respondText(body, ContentType.Application.Json, status)
    }
}
