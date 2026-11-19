package com.leliukh.labelmanager.features.market.domain.repository

import androidx.paging.PagingData
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.features.market.domain.models.Listing
import com.leliukh.labelmanager.features.market.domain.models.ListingFilter
import com.leliukh.labelmanager.features.market.domain.models.Purchase
import com.leliukh.labelmanager.features.artist.domain.models.LabelSummary
import com.leliukh.labelmanager.features.artist.domain.repository.testDeal
import com.leliukh.labelmanager.features.label.domain.repository.testArtist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Instant

/**
 * [MarketRepository] with preset answers that remembers what it was asked.
 */
class FakeMarketRepository : MarketRepository {

    /** Listings every filter returns. */
    var listings: List<Listing> = listOf(testListing())

    var purchaseResult: ApiResult<Purchase> = ApiResult.Success(testPurchase())
    var createResult: ApiResult<Unit> = ApiResult.Success(Unit)
    var withdrawResult: ApiResult<Unit> = ApiResult.Success(Unit)

    /** Filters passed to [countListings]. */
    val counted = mutableListOf<ListingFilter>()

    /** Artist ids and prices passed to [createListing]. */
    val created = mutableListOf<Pair<String, Long>>()

    override fun listings(filter: ListingFilter, myLabelId: String?): Flow<PagingData<Listing>> =
        flowOf(PagingData.from(listings))

    override suspend fun countListings(filter: ListingFilter): ApiResult<Int> {
        counted += filter
        return ApiResult.Success(listings.size)
    }

    override suspend fun purchase(listingId: String): ApiResult<Purchase> = purchaseResult

    override suspend fun createListing(artistId: String, askingPrice: Long): ApiResult<Unit> {
        created += artistId to askingPrice
        return createResult
    }

    override suspend fun withdrawListing(listingId: String): ApiResult<Unit> = withdrawResult
}

/**
 * @param id listing id
 * @param askingPrice price a buyer pays
 */
fun testListing(id: String = "listing-1", askingPrice: Long = 1_450_000) = Listing(
    id = id,
    artist = testArtist(id = "artist-9", firstName = "Chidi", lastName = "Okafor"),
    seller = LabelSummary(id = "label-2", name = "Lagos Sound", countryCode = "NG"),
    askingPrice = askingPrice,
    listedAt = Instant.parse("2026-10-25T11:48:00Z"),
    isMine = false,
)

/** Chidi Okafor bought for $1.45M, value $1.18M → $1.6166M. */
fun testPurchase() = Purchase(
    deal = testDeal().copy(
        price = 1_450_000,
        valueBefore = 1_180_000,
        valueAfter = 1_350_000,
    ),
    artist = testArtist(
        id = "artist-9",
        firstName = "Chidi",
        lastName = "Okafor",
        marketValue = 1_350_000,
    ),
    budget = 2_032_150,
)
