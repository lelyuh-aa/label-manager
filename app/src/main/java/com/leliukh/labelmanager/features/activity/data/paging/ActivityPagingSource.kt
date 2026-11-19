package com.leliukh.labelmanager.features.activity.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.leliukh.labelmanager.core.network.domain.ApiErrorException
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.features.activity.data.api.ActivityApi
import com.leliukh.labelmanager.features.activity.data.models.toDomain
import com.leliukh.labelmanager.features.activity.domain.models.ActivityEvent

/**
 * Pages of the label's activity, newest first, following the server's cursors. A refresh starts
 * from the newest page, where new events appear.
 *
 * @param api the activity endpoints
 */
class ActivityPagingSource(
    private val api: ActivityApi,
) : PagingSource<String, ActivityEvent>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, ActivityEvent> =
        when (val result = api.getActivity(cursor = params.key, limit = params.loadSize)) {
            is ApiResult.Success -> LoadResult.Page(
                data = result.data.items.map { it.toDomain() },
                prevKey = null,
                nextKey = result.data.nextCursor,
            )

            is ApiResult.Failure -> LoadResult.Error(ApiErrorException(result.error))
        }

    override fun getRefreshKey(state: PagingState<String, ActivityEvent>): String? = null
}
