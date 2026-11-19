package com.leliukh.labelmanager.backend.api

import com.leliukh.labelmanager.backend.BackendConfig
import com.leliukh.labelmanager.backend.LabelBackendFactory
import com.leliukh.labelmanager.backend.data.memory.InMemoryBackendStore
import com.leliukh.labelmanager.backend.testing.TestApiClient
import com.leliukh.labelmanager.backend.testing.TestClock
import com.leliukh.labelmanager.backend.testing.decode
import com.leliukh.labelmanager.backend.testing.error
import com.leliukh.labelmanager.contract.dto.activity.ActivityPageDto
import com.leliukh.labelmanager.contract.dto.activity.ActivityTypeDto
import com.leliukh.labelmanager.contract.dto.auth.AuthResponseDto
import com.leliukh.labelmanager.contract.dto.auth.LoginRequestDto
import com.leliukh.labelmanager.contract.dto.auth.RefreshRequestDto
import com.leliukh.labelmanager.contract.dto.auth.RegisterRequestDto
import com.leliukh.labelmanager.contract.dto.auth.UserDto
import com.leliukh.labelmanager.contract.dto.common.PageDto
import com.leliukh.labelmanager.contract.dto.error.ErrorCodeDto
import com.leliukh.labelmanager.contract.dto.market.CreateListingRequestDto
import com.leliukh.labelmanager.contract.dto.market.ListingDto
import com.leliukh.labelmanager.contract.dto.market.PurchaseResultDto
import com.leliukh.labelmanager.contract.dto.artist.ArtistDetailsDto
import com.leliukh.labelmanager.contract.dto.artist.ArtistDto
import com.leliukh.labelmanager.contract.dto.artist.GenreDto
import com.leliukh.labelmanager.contract.dto.artist.UpdateArtistRequestDto
import com.leliukh.labelmanager.contract.dto.reference.Countries
import com.leliukh.labelmanager.contract.dto.reference.CountryDto
import com.leliukh.labelmanager.contract.dto.label.LabelDto
import com.leliukh.labelmanager.contract.dto.label.UpdateLabelRequestDto
import com.leliukh.labelmanager.contract.routes.ApiQuery
import com.leliukh.labelmanager.contract.routes.ApiRoutes
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.ListSerializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LabelBackendTest {

    private val clock = TestClock()
    private val api =
        TestApiClient(
            LabelBackendFactory.create(
                store = InMemoryBackendStore(snapshotFile = null),
                clock = clock,
                random = Random(5),
                config = BackendConfig(passwordHashIterations = 1_000),
            )
        )

    @Test
    fun `registration returns tokens and a label of 16 artists`() = runTest {
        val auth = api.register("producer")

        val label = api.send("GET", ApiRoutes.LABEL, auth.accessToken).decode(LabelDto.serializer())

        assertEquals(auth.user.labelId, label.id)
        assertEquals(16, label.artists.size)
        assertEquals(label.artists.sumOf { it.marketValue }, label.value)
        assertEquals(3_000_000L, label.budget)
        assertEquals(
            mapOf(
                GenreDto.POP to 4,
                GenreDto.ROCK to 4,
                GenreDto.HIP_HOP to 4,
                GenreDto.ELECTRONIC to 4,
            ),
            label.artists.groupingBy { it.genre }.eachCount(),
        )
        assertTrue(label.artists.all { it.age in 18..45 && it.marketValue >= 400_000L })
        val me = api.send("GET", ApiRoutes.ME, auth.accessToken).decode(UserDto.serializer())
        assertEquals("producer@example.com", me.email)
    }

    @Test
    fun `auth errors map to their codes`() = runTest {
        api.register("producer")
        val duplicate =
            api.json(
                RegisterRequestDto.serializer(),
                RegisterRequestDto("PRODUCER@example.com", "password1"),
            )
        val wrongPassword =
            api.json(
                LoginRequestDto.serializer(),
                LoginRequestDto("producer@example.com", "nope-nope"),
            )
        val invalid = api.json(RegisterRequestDto.serializer(), RegisterRequestDto("x", "1"))

        assertEquals(
            ErrorCodeDto.EMAIL_TAKEN,
            api.send("POST", ApiRoutes.REGISTER, body = duplicate).error(409).code,
        )
        assertEquals(
            ErrorCodeDto.INVALID_CREDENTIALS,
            api.send("POST", ApiRoutes.LOGIN, body = wrongPassword).error(401).code,
        )
        val validation = api.send("POST", ApiRoutes.REGISTER, body = invalid).error(422)
        assertEquals(ErrorCodeDto.VALIDATION_FAILED, validation.code)
        assertEquals(setOf("email", "password"), validation.fieldErrors.keys)
        assertEquals(ErrorCodeDto.UNAUTHORIZED, api.send("GET", ApiRoutes.LABEL).error(401).code)
        assertEquals(
            ErrorCodeDto.UNAUTHORIZED,
            api.send("GET", ApiRoutes.LABEL, "forged").error(401).code,
        )
    }

    @Test
    fun `refresh rotates tokens and logout revokes them`() = runTest {
        val auth = api.register("producer")
        val refresh = api.json(RefreshRequestDto.serializer(), RefreshRequestDto(auth.refreshToken))

        val renewed =
            api.send("POST", ApiRoutes.REFRESH, body = refresh).decode(AuthResponseDto.serializer())

        assertEquals(
            ErrorCodeDto.UNAUTHORIZED,
            api.send("POST", ApiRoutes.REFRESH, body = refresh).error(401).code,
        )
        val logout =
            api.json(RefreshRequestDto.serializer(), RefreshRequestDto(renewed.refreshToken))
        assertEquals(204, api.send("POST", ApiRoutes.LOGOUT, body = logout).status)
        assertEquals(
            ErrorCodeDto.UNAUTHORIZED,
            api.send("POST", ApiRoutes.REFRESH, body = logout).error(401).code,
        )
    }

    @Test
    fun `a deal between two users over the API`() = runTest {
        val seller = api.register("seller")
        val buyer = api.register("buyer")
        val sellerLabel =
            api.send("GET", ApiRoutes.LABEL, seller.accessToken).decode(LabelDto.serializer())
        val buyerLabel =
            api.send("GET", ApiRoutes.LABEL, buyer.accessToken).decode(LabelDto.serializer())
        val artist = sellerLabel.artists.first()
        val create =
            api.json(
                CreateListingRequestDto.serializer(),
                CreateListingRequestDto(artist.id, 1_750_000),
            )

        val listing =
            api.send("POST", ApiRoutes.LISTINGS, seller.accessToken, body = create)
                .decode(ListingDto.serializer(), 201)
        val market =
            api.send(
                    "GET",
                    ApiRoutes.LISTINGS,
                    buyer.accessToken,
                    query = mapOf(ApiQuery.LABEL_NAME to sellerLabel.name),
                )
                .decode(PageDto.serializer(ListingDto.serializer()))
        clock.advanceBy(1.minutes)
        val purchase =
            api.send("POST", ApiRoutes.listingPurchase(listing.id), buyer.accessToken)
                .decode(PurchaseResultDto.serializer())

        assertEquals(listOf(listing.id), market.items.map { it.id })
        assertEquals(buyerLabel.budget - 1_750_000, purchase.budget)
        assertEquals(buyer.user.labelId, purchase.artist.labelId)
        assertEquals(artist.marketValue, purchase.deal.valueBefore)
        assertTrue(purchase.artist.marketValue > artist.marketValue)
        val sellerAfter =
            api.send("GET", ApiRoutes.LABEL, seller.accessToken).decode(LabelDto.serializer())
        assertEquals(sellerLabel.budget + 1_750_000, sellerAfter.budget)
        assertEquals(15, sellerAfter.artists.size)
        val details =
            api.send("GET", ApiRoutes.artist(artist.id), seller.accessToken)
                .decode(ArtistDetailsDto.serializer())
        assertEquals(buyerLabel.name, details.label.name)
        assertEquals(sellerLabel.name, details.deals.single().from.name)
        assertEquals(
            ErrorCodeDto.LISTING_NOT_AVAILABLE,
            api.send("POST", ApiRoutes.listingPurchase(listing.id), seller.accessToken)
                .error(409)
                .code,
        )
        val activity =
            api.send("GET", ApiRoutes.ACTIVITY, seller.accessToken)
                .decode(ActivityPageDto.serializer())
        assertEquals(ActivityTypeDto.ARTIST_SOLD, activity.items.first().type)
        assertEquals(2, activity.unreadCount)
        val eventRead = ApiRoutes.activityEventRead(activity.items.first().id)
        assertEquals(204, api.send("POST", eventRead, seller.accessToken).status)
        val afterOne =
            api.send("GET", ApiRoutes.ACTIVITY, seller.accessToken)
                .decode(ActivityPageDto.serializer())
        assertEquals(1, afterOne.unreadCount)
        assertTrue(afterOne.items.first().isRead)
        assertEquals(204, api.send("POST", ApiRoutes.ACTIVITY_READ, seller.accessToken).status)
        assertEquals(
            0,
            api.send("GET", ApiRoutes.ACTIVITY, seller.accessToken)
                .decode(ActivityPageDto.serializer())
                .unreadCount,
        )
    }

    @Test
    fun `market rules map to conflict codes`() = runTest {
        val seller = api.register("seller")
        val label = api.send("GET", ApiRoutes.LABEL, seller.accessToken).decode(LabelDto.serializer())
        val create = { artistId: String, price: Long ->
            api.json(CreateListingRequestDto.serializer(), CreateListingRequestDto(artistId, price))
        }
        val own =
            api.send(
                    "POST",
                    ApiRoutes.LISTINGS,
                    seller.accessToken,
                    body = create(label.artists[0].id, 1_000),
                )
                .decode(ListingDto.serializer(), 201)
        val buyer = api.register("buyer")
        val expensive =
            api.send(
                    "POST",
                    ApiRoutes.LISTINGS,
                    seller.accessToken,
                    body = create(label.artists[1].id, 999_000_000),
                )
                .decode(ListingDto.serializer(), 201)

        assertEquals(
            ErrorCodeDto.CANNOT_BUY_OWN_ARTIST,
            api.send("POST", ApiRoutes.listingPurchase(own.id), seller.accessToken).error(409).code,
        )
        assertEquals(
            ErrorCodeDto.INSUFFICIENT_BUDGET,
            api.send("POST", ApiRoutes.listingPurchase(expensive.id), buyer.accessToken)
                .error(409)
                .code,
        )
        assertEquals(
            ErrorCodeDto.ARTIST_ALREADY_LISTED,
            api.send(
                    "POST",
                    ApiRoutes.LISTINGS,
                    seller.accessToken,
                    body = create(label.artists[0].id, 5),
                )
                .error(409)
                .code,
        )
        assertEquals(
            ErrorCodeDto.FORBIDDEN,
            api.send("DELETE", ApiRoutes.listing(own.id), buyer.accessToken).error(403).code,
        )
        assertEquals(204, api.send("DELETE", ApiRoutes.listing(own.id), seller.accessToken).status)
    }

    @Test
    fun `owners edit their label and artists, nobody else`() = runTest {
        val owner = api.register("owner")
        val stranger = api.register("stranger")
        val artist =
            api.send("GET", ApiRoutes.LABEL, owner.accessToken)
                .decode(LabelDto.serializer())
                .artists
                .first()
        val rename =
            api.json(
                UpdateArtistRequestDto.serializer(),
                UpdateArtistRequestDto(firstName = "Luca"),
            )
        val label =
            api.json(
                UpdateLabelRequestDto.serializer(),
                UpdateLabelRequestDto("Tiber Dischi", "IT"),
            )

        val updated =
            api.send("PATCH", ApiRoutes.artist(artist.id), owner.accessToken, body = rename)
                .decode(ArtistDto.serializer())
        val updatedLabel =
            api.send("PATCH", ApiRoutes.LABEL, owner.accessToken, body = label)
                .decode(LabelDto.serializer())

        assertEquals("Luca", updated.firstName)
        assertEquals(artist.lastName, updated.lastName)
        assertEquals("Tiber Dischi", updatedLabel.name)
        assertEquals(
            ErrorCodeDto.FORBIDDEN,
            api.send("PATCH", ApiRoutes.artist(artist.id), stranger.accessToken, body = rename)
                .error(403)
                .code,
        )
    }

    @Test
    fun `malformed requests are rejected with details`() = runTest {
        val auth = api.register("producer")

        val badQuery =
            api.send(
                    "GET",
                    ApiRoutes.LISTINGS,
                    auth.accessToken,
                    query =
                        mapOf(
                            ApiQuery.MIN_PRICE to "cheap",
                            ApiQuery.GENRES to "POP,KEEPER",
                            ApiQuery.SORT to "RANDOM",
                            ApiQuery.LIMIT to "500",
                        ),
                )
                .error(422)

        assertEquals(
            setOf(ApiQuery.MIN_PRICE, ApiQuery.GENRES, ApiQuery.SORT, ApiQuery.LIMIT),
            badQuery.fieldErrors.keys,
        )
        assertEquals(
            setOf("body"),
            api.send("PATCH", ApiRoutes.LABEL, auth.accessToken, body = "{oops")
                .error(422)
                .fieldErrors
                .keys,
        )
        assertEquals(ErrorCodeDto.NOT_FOUND, api.send("GET", "/api/v1/nowhere").error(404).code)
        assertEquals(405, api.send("PUT", ApiRoutes.LABEL, auth.accessToken).status)
        assertEquals(
            ErrorCodeDto.NOT_FOUND,
            api.send("GET", ApiRoutes.artist("missing"), auth.accessToken).error(404).code,
        )
    }

    @Test
    fun `countries need no token and filters accept lower case`() = runTest {
        val countries =
            api.send("GET", ApiRoutes.COUNTRIES).decode(ListSerializer(CountryDto.serializer()))
        val auth = api.register("producer")

        val filtered =
            api.send(
                "GET",
                ApiRoutes.LISTINGS,
                auth.accessToken,
                query =
                    mapOf(ApiQuery.GENRES to "pop", ApiQuery.SORT to "price_ascending"),
            )

        assertEquals(Countries.all, countries)
        assertEquals(200, filtered.status)
    }

    @Test
    fun `header names are case-insensitive and listings are embedded in artists`() = runTest {
        val auth = api.register("producer")
        val artistId =
            api.send("GET", ApiRoutes.LABEL, auth.accessToken)
                .decode(LabelDto.serializer())
                .artists
                .first()
                .id
        val create =
            api.json(CreateListingRequestDto.serializer(), CreateListingRequestDto(artistId, 1_234))
        api.send("POST", ApiRoutes.LISTINGS, auth.accessToken, body = create)

        val label =
            api.send(
                    ApiRequest(
                        method = "GET",
                        path = ApiRoutes.LABEL,
                        query = emptyMap(),
                        headers = mapOf("authorization" to "Bearer ${auth.accessToken}"),
                        body = null,
                    )
                )
                .decode(LabelDto.serializer())
        val listed = label.artists.first { it.id == artistId }

        assertEquals(1_234L, listed.listing?.askingPrice)
        assertNotNull(listed.listing?.id)
        assertNull(label.artists.first { it.id != artistId }.listing)
    }
}
