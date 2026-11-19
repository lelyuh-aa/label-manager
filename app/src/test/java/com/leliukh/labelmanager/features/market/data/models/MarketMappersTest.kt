package com.leliukh.labelmanager.features.market.data.models

import com.leliukh.labelmanager.contract.routes.ApiQuery
import com.leliukh.labelmanager.features.market.domain.models.ListingFilter
import com.leliukh.labelmanager.features.market.domain.models.ListingSort
import com.leliukh.labelmanager.features.market.domain.models.SearchScope
import com.leliukh.labelmanager.features.label.domain.models.Genre
import org.junit.Assert.assertEquals
import org.junit.Test

class MarketMappersTest {

    @Test
    fun `an empty filter sends only the sort order`() {
        assertEquals(mapOf(ApiQuery.SORT to "NEWEST"), ListingFilter().toQuery())
    }

    @Test
    fun `every set filter becomes a query parameter`() {
        val filter = ListingFilter(
            text = "  lions ",
            scope = SearchScope.Labels,
            maxPrice = 2_000_000,
            genres = setOf(Genre.Electronic, Genre.Pop),
            artistCountry = "IT",
            sort = ListingSort.PriceAscending,
        )

        assertEquals(
            mapOf(
                ApiQuery.LABEL_NAME to "lions",
                ApiQuery.MAX_PRICE to "2000000",
                ApiQuery.GENRES to "POP,ELECTRONIC",
                ApiQuery.ARTIST_COUNTRY to "IT",
                ApiQuery.SORT to "PRICE_ASCENDING",
            ),
            filter.toQuery(),
        )
    }
}
