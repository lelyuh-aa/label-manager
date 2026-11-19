package com.leliukh.labelmanager.features.market.data.paging

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.testing.TestPager
import com.leliukh.labelmanager.contract.dto.common.PageDto
import com.leliukh.labelmanager.contract.dto.market.ListingDto
import com.leliukh.labelmanager.contract.dto.artist.ArtistDto
import com.leliukh.labelmanager.contract.dto.artist.GenreDto
import com.leliukh.labelmanager.contract.dto.label.LabelSummaryDto
import com.leliukh.labelmanager.contract.routes.ApiQuery
import com.leliukh.labelmanager.contract.serialization.ApiJson
import com.leliukh.labelmanager.core.network.data.ApiEndpoint
import com.leliukh.labelmanager.core.network.data.createApiClient
import com.leliukh.labelmanager.core.network.domain.ApiError
import com.leliukh.labelmanager.core.network.domain.ApiErrorException
import com.leliukh.labelmanager.core.session.domain.repository.FakeSessionRepository
import com.leliukh.labelmanager.features.market.data.api.MarketApi
import com.leliukh.labelmanager.features.market.domain.models.Listing
import com.leliukh.labelmanager.features.market.domain.models.ListingFilter
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

class ListingsPagingSourceTest {

    private val cursors = mutableListOf<String?>()

    @Test
    fun `pages follow the server cursors and own listings are marked`() = runTest {
        val pager = pager { cursor ->
            when (cursor) {
                null -> page(
                    items = listOf(listing("l1", "label-1"), listing("l2", "label-2")),
                    next = "c2",
                )

                else -> page(listOf(listing("l3", "label-2")), next = null)
            }
        }

        val first = pager.refresh() as PagingSource.LoadResult.Page
        val second = pager.append() as PagingSource.LoadResult.Page

        assertEquals(listOf(null, "c2"), cursors)
        assertEquals(listOf(true, false), first.data.map { it.isMine })
        assertNull(second.nextKey)
    }

    @Test
    fun `a failed page carries the API error`() = runTest {
        val pager = pager { throw IOException("offline") }

        val result = pager.refresh() as PagingSource.LoadResult.Error

        assertTrue(result.throwable is ApiErrorException)
        assertEquals(ApiError.Network, (result.throwable as ApiErrorException).error)
    }

    private fun pager(
        respond: (cursor: String?) -> PageDto<ListingDto>,
    ): TestPager<String, Listing> {
        val client = createApiClient(
            engine = MockEngine { request ->
                val cursor = request.url.parameters[ApiQuery.CURSOR]
                cursors += cursor
                respond(
                    content = ApiJson.encodeToString(
                        PageDto.serializer(ListingDto.serializer()),
                        respond(cursor),
                    ),
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            },
            endpoint = ApiEndpoint("http://label.test/"),
            sessionRepository = FakeSessionRepository(),
            isLoggingEnabled = false,
        )
        val source = ListingsPagingSource(MarketApi(client), ListingFilter(), myLabelId = "label-1")
        return TestPager(PagingConfig(pageSize = 2), source)
    }

    private fun page(items: List<ListingDto>, next: String?) =
        PageDto(items = items, nextCursor = next, totalCount = 3)

    private fun listing(id: String, sellerId: String) = ListingDto(
        id = id,
        artist = ArtistDto(
            id = "p-$id",
            labelId = sellerId,
            firstName = "Chidi",
            lastName = "Okafor",
            countryCode = "NG",
            genre = GenreDto.HIP_HOP,
            birthDate = LocalDate(2003, 1, 9),
            age = 23,
            marketValue = 1_180_000,
            hits = 14,
        ),
        seller = LabelSummaryDto(id = sellerId, name = "Lagos Sound", countryCode = "NG"),
        askingPrice = 1_450_000,
        listedAt = Instant.parse("2026-10-25T11:48:00Z"),
    )
}
