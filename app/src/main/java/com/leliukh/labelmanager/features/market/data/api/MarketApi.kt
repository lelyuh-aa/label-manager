package com.leliukh.labelmanager.features.market.data.api

import com.leliukh.labelmanager.contract.dto.common.PageDto
import com.leliukh.labelmanager.contract.dto.market.CreateListingRequestDto
import com.leliukh.labelmanager.contract.dto.market.ListingDto
import com.leliukh.labelmanager.contract.dto.market.PurchaseResultDto
import com.leliukh.labelmanager.contract.routes.ApiQuery
import com.leliukh.labelmanager.contract.routes.ApiRoutes
import com.leliukh.labelmanager.core.network.data.apiRequest
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.features.market.data.models.toQuery
import com.leliukh.labelmanager.features.market.domain.models.ListingFilter
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The `market` endpoints of the API.
 *
 * @param client the shared API client
 */
@Singleton
class MarketApi @Inject constructor(
    private val client: HttpClient,
) {

    /**
     * @param filter which listings to return
     * @param cursor cursor of the page; null for the first one
     * @param limit page size
     */
    suspend fun getListings(
        filter: ListingFilter,
        cursor: String?,
        limit: Int,
    ): ApiResult<PageDto<ListingDto>> = apiRequest {
        client.get(ApiRoutes.LISTINGS) {
            filter.toQuery().forEach { (name, value) -> parameter(name, value) }
            parameter(ApiQuery.CURSOR, cursor)
            parameter(ApiQuery.LIMIT, limit)
        }
    }

    /**
     * @param artistId id of one of the caller's artists that is not listed yet
     * @param askingPrice price a buyer will pay
     */
    suspend fun createListing(artistId: String, askingPrice: Long): ApiResult<ListingDto> =
        apiRequest {
            client.post(ApiRoutes.LISTINGS) {
                setBody(CreateListingRequestDto(artistId, askingPrice))
            }
        }

    /**
     * @param listingId id of one of the caller's listings
     */
    suspend fun withdrawListing(listingId: String): ApiResult<Unit> =
        apiRequest { client.delete(listingPath(ApiRoutes.LISTING, listingId)) }

    /**
     * @param listingId id of another label's listing
     */
    suspend fun purchase(listingId: String): ApiResult<PurchaseResultDto> =
        apiRequest { client.post(listingPath(ApiRoutes.LISTING_PURCHASE, listingId)) }

    private fun listingPath(route: String, listingId: String): String =
        route.replace("{${ApiRoutes.LISTING_ID}}", listingId)
}
