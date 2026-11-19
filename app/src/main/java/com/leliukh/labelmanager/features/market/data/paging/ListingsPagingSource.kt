package com.leliukh.labelmanager.features.market.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.leliukh.labelmanager.core.network.domain.ApiErrorException
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.features.market.data.api.MarketApi
import com.leliukh.labelmanager.features.market.data.models.toDomain
import com.leliukh.labelmanager.features.market.domain.models.Listing
import com.leliukh.labelmanager.features.market.domain.models.ListingFilter

/**
 * Pages of the market for one [filter], following the server's cursors.
 *
 * The list changes all the time as labels buy and sell, so a refresh always starts again from
 * the first page instead of guessing a cursor near the old scroll genre.
 *
 * @param api the market endpoints
 * @param filter which listings to load
 * @param myLabelId id of the signed-in user's label, to mark own listings
 */
class ListingsPagingSource(
    private val api: MarketApi,
    private val filter: ListingFilter,
    private val myLabelId: String?,
) : PagingSource<String, Listing>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Listing> =
        when (val result = api.getListings(filter, cursor = params.key, limit = params.loadSize)) {
            is ApiResult.Success -> LoadResult.Page(
                data = result.data.items.map { it.toDomain(myLabelId) },
                prevKey = null,
                nextKey = result.data.nextCursor,
            )

            is ApiResult.Failure -> LoadResult.Error(ApiErrorException(result.error))
        }

    override fun getRefreshKey(state: PagingState<String, Listing>): String? = null
}

