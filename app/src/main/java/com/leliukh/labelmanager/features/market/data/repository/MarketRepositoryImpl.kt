package com.leliukh.labelmanager.features.market.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.leliukh.labelmanager.contract.routes.ApiQuery
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.network.domain.map
import com.leliukh.labelmanager.features.market.data.api.MarketApi
import com.leliukh.labelmanager.features.market.data.models.toDomain
import com.leliukh.labelmanager.features.market.data.paging.ListingsPagingSource
import com.leliukh.labelmanager.features.market.domain.models.Listing
import com.leliukh.labelmanager.features.market.domain.models.ListingFilter
import com.leliukh.labelmanager.features.market.domain.models.Purchase
import com.leliukh.labelmanager.features.market.domain.repository.MarketRepository
import kotlinx.coroutines.flow.Flow

/**
 * [MarketRepository] over the API, paged with Paging 3.
 *
 * @param api the market endpoints
 */
class MarketRepositoryImpl(
    private val api: MarketApi,
) : MarketRepository {

    override fun listings(filter: ListingFilter, myLabelId: String?): Flow<PagingData<Listing>> =
        Pager(
            config = PagingConfig(
                pageSize = ApiQuery.DEFAULT_LIMIT,
                initialLoadSize = ApiQuery.DEFAULT_LIMIT,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { ListingsPagingSource(api, filter, myLabelId) },
        ).flow

    override suspend fun countListings(filter: ListingFilter): ApiResult<Int> =
        api.getListings(filter, cursor = null, limit = 1).map { it.totalCount }

    override suspend fun purchase(listingId: String): ApiResult<Purchase> =
        api.purchase(listingId).map { it.toDomain() }

    override suspend fun createListing(artistId: String, askingPrice: Long): ApiResult<Unit> =
        api.createListing(artistId, askingPrice).map { }

    override suspend fun withdrawListing(listingId: String): ApiResult<Unit> =
        api.withdrawListing(listingId)
}
