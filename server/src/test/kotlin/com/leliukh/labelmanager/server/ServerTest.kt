package com.leliukh.labelmanager.server

import com.leliukh.labelmanager.backend.BackendConfig
import com.leliukh.labelmanager.backend.LabelBackendFactory
import com.leliukh.labelmanager.contract.dto.auth.AuthResponseDto
import com.leliukh.labelmanager.contract.dto.auth.RegisterRequestDto
import com.leliukh.labelmanager.contract.dto.common.PageDto
import com.leliukh.labelmanager.contract.dto.error.ErrorCodeDto
import com.leliukh.labelmanager.contract.dto.error.ErrorDto
import com.leliukh.labelmanager.contract.dto.market.CreateListingRequestDto
import com.leliukh.labelmanager.contract.dto.market.ListingDto
import com.leliukh.labelmanager.contract.dto.market.PurchaseResultDto
import com.leliukh.labelmanager.contract.dto.label.LabelDto
import com.leliukh.labelmanager.contract.routes.ApiQuery
import com.leliukh.labelmanager.contract.routes.ApiRoutes
import com.leliukh.labelmanager.contract.serialization.ApiJson
import com.leliukh.labelmanager.server.data.ExposedBackendStore
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.options
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import java.io.File
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ServerTest {

    @get:Rule val folder = TemporaryFolder()

    @Test
    fun `two users trade an artist over HTTP`() = serverTest {
        val seller = register("seller")
        val buyer = register("buyer")
        val sellerLabel = client.label(seller)
        val buyerLabel = client.label(buyer)
        val artist = sellerLabel.artists.first()

        val listing =
            client
                .post(ApiRoutes.LISTINGS) {
                    bearerAuth(seller.accessToken)
                    json(
                        CreateListingRequestDto(artist.id, 1_200_000),
                        CreateListingRequestDto.serializer(),
                    )
                }
                .decode(ListingDto.serializer(), HttpStatusCode.Created)
        val market =
            client
                .get(ApiRoutes.LISTINGS) {
                    bearerAuth(buyer.accessToken)
                    parameter(ApiQuery.LABEL_NAME, sellerLabel.name)
                }
                .decode(PageDto.serializer(ListingDto.serializer()))
        val purchase =
            client
                .post(ApiRoutes.listingPurchase(listing.id)) { bearerAuth(buyer.accessToken) }
                .decode(PurchaseResultDto.serializer())

        assertEquals(listOf(listing.id), market.items.map { it.id })
        assertEquals(buyerLabel.budget - 1_200_000, purchase.budget)
        assertTrue(purchase.artist.marketValue > artist.marketValue)
        assertEquals(sellerLabel.budget + 1_200_000, client.label(seller).budget)
        assertEquals(17, client.label(buyer).artists.size)
    }

    @Test
    fun `of simultaneous buyers exactly one succeeds`() = serverTest {
        val seller = register("seller")
        val buyers = List(5) { register("buyer$it") }
        val artist = client.label(seller).artists.first()
        val listing =
            client
                .post(ApiRoutes.LISTINGS) {
                    bearerAuth(seller.accessToken)
                    json(
                        CreateListingRequestDto(artist.id, 1_000),
                        CreateListingRequestDto.serializer(),
                    )
                }
                .decode(ListingDto.serializer(), HttpStatusCode.Created)

        val responses = coroutineScope {
            buyers
                .map { buyer ->
                    async {
                        client.post(ApiRoutes.listingPurchase(listing.id)) {
                            bearerAuth(buyer.accessToken)
                        }
                    }
                }
                .awaitAll()
        }

        assertEquals(1, responses.count { it.status == HttpStatusCode.OK })
        responses
            .filter { it.status != HttpStatusCode.OK }
            .forEach {
                assertEquals(
                    ErrorCodeDto.LISTING_NOT_AVAILABLE,
                    it.decode(ErrorDto.serializer(), HttpStatusCode.Conflict).code,
                )
            }
    }

    @Test
    fun `errors keep their documented status and body`() = serverTest {
        val unauthorized = client.get(ApiRoutes.LABEL)
        val unknown = client.get("/api/v1/nowhere")
        val malformed =
            client.post(ApiRoutes.REGISTER) {
                contentType(ContentType.Application.Json)
                setBody("{")
            }

        assertEquals(
            ErrorCodeDto.UNAUTHORIZED,
            unauthorized.decode(ErrorDto.serializer(), HttpStatusCode.Unauthorized).code,
        )
        assertEquals(
            ErrorCodeDto.NOT_FOUND,
            unknown.decode(ErrorDto.serializer(), HttpStatusCode.NotFound).code,
        )
        assertEquals(
            ErrorCodeDto.VALIDATION_FAILED,
            malformed.decode(ErrorDto.serializer(), HttpStatusCode.UnprocessableEntity).code,
        )
    }

    @Test
    fun `browsers may call the API from other origins`() = serverTest {
        val preflight =
            client.options(ApiRoutes.LABEL) {
                header(HttpHeaders.Origin, "https://web.example.com")
                header(HttpHeaders.AccessControlRequestMethod, "PATCH")
                header(HttpHeaders.AccessControlRequestHeaders, "authorization,content-type")
            }

        assertEquals(HttpStatusCode.OK, preflight.status)
        assertEquals("*", preflight.headers[HttpHeaders.AccessControlAllowOrigin])
    }

    @Test
    fun `the API documentation is served`() = serverTest {
        val spec = client.get("/openapi.yaml")
        val swagger = client.get("/docs")

        assertEquals(HttpStatusCode.OK, spec.status)
        assertTrue(spec.bodyAsText().startsWith("openapi: 3.1.0"))
        assertEquals(HttpStatusCode.OK, swagger.status)
        assertTrue(swagger.bodyAsText().contains("swagger-ui", ignoreCase = true))
    }

    @Test
    fun `data survives a server restart`() {
        val file = folder.root.resolve("restart.db")
        lateinit var auth: AuthResponseDto

        serverTest(file) { auth = register("producer") }
        serverTest(file) { assertEquals(auth.user.labelId, client.label(auth).id) }
    }

    private fun serverTest(
        file: File = folder.root.resolve("server.db"),
        block: suspend ApplicationTestBuilder.() -> Unit,
    ) = testApplication {
        val backend =
            LabelBackendFactory.create(
                store = ExposedBackendStore.sqlite(file),
                config = BackendConfig(passwordHashIterations = 1_000),
            )
        application { labelManagerModule(backend, simulationInterval = null) }
        block()
    }

    private suspend fun ApplicationTestBuilder.register(name: String): AuthResponseDto =
        client
            .post(ApiRoutes.REGISTER) {
                json(
                    RegisterRequestDto("$name@example.com", "password1"),
                    RegisterRequestDto.serializer(),
                )
            }
            .decode(AuthResponseDto.serializer(), HttpStatusCode.Created)

    private suspend fun HttpClient.label(auth: AuthResponseDto): LabelDto =
        get(ApiRoutes.LABEL) { bearerAuth(auth.accessToken) }.decode(LabelDto.serializer())

    private fun <T> HttpRequestBuilder.json(
        value: T,
        serializer: SerializationStrategy<T>,
    ) {
        contentType(ContentType.Application.Json)
        setBody(ApiJson.encodeToString(serializer, value))
    }

    private suspend fun <T> HttpResponse.decode(
        deserializer: DeserializationStrategy<T>,
        status: HttpStatusCode = HttpStatusCode.OK,
    ): T {
        val text = bodyAsText()
        assertEquals("Unexpected status, body: $text", status, this.status)
        return ApiJson.decodeFromString(deserializer, text)
    }
}
