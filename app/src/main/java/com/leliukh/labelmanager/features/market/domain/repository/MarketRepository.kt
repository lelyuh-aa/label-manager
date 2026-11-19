package com.leliukh.labelmanager.features.market.domain.repository

import androidx.paging.PagingData
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.features.market.domain.models.Listing
import com.leliukh.labelmanager.features.market.domain.models.ListingFilter
import com.leliukh.labelmanager.features.market.domain.models.Purchase
import kotlinx.coroutines.flow.Flow

/**
 * The market: browsing, buying and selling.
 */
interface MarketRepository {

    /**
     * @param filter which listings to show
     * @param myLabelId id of the signed-in user's label, to mark own listings
     * @return the listings page by page
     */
    fun listings(filter: ListingFilter, myLabelId: String?): Flow<PagingData<Listing>>

    /**
     * @param filter which listings to count
     * @return how many listings match, across all pages
     */
    suspend fun countListings(filter: ListingFilter): ApiResult<Int>

    /**
     * Buys another label's artist for the asking price.
     *
     * @param listingId the listing
     */
    suspend fun purchase(listingId: String): ApiResult<Purchase>

    /**
     * Puts one of the user's artists on the market.
     *
     * @param artistId the artist
     * @param askingPrice price a buyer will pay
     */
    suspend fun createListing(artistId: String, askingPrice: Long): ApiResult<Unit>

    /**
     * Takes one of the user's artists off the market.
     *
     * @param listingId the listing
     */
    suspend fun withdrawListing(listingId: String): ApiResult<Unit>
}
