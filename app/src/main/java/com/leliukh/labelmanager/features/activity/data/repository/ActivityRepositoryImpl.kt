package com.leliukh.labelmanager.features.activity.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.leliukh.labelmanager.contract.routes.ApiQuery
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.network.domain.map
import com.leliukh.labelmanager.features.activity.data.api.ActivityApi
import com.leliukh.labelmanager.features.activity.data.models.toDomain
import com.leliukh.labelmanager.features.activity.data.paging.ActivityPagingSource
import com.leliukh.labelmanager.features.activity.domain.models.ActivityEvent
import com.leliukh.labelmanager.features.activity.domain.repository.ActivityRepository
import com.leliukh.labelmanager.features.activity.domain.repository.LatestActivity
import kotlinx.coroutines.flow.Flow

/**
 * [ActivityRepository] over the API, paged with Paging 3.
 *
 * @param api the activity endpoints
 */
class ActivityRepositoryImpl(
    private val api: ActivityApi,
) : ActivityRepository {

    override fun events(): Flow<PagingData<ActivityEvent>> = Pager(
        config = PagingConfig(
            pageSize = ApiQuery.DEFAULT_LIMIT,
            initialLoadSize = ApiQuery.DEFAULT_LIMIT,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = { ActivityPagingSource(api) },
    ).flow

    override suspend fun latest(limit: Int): ApiResult<LatestActivity> =
        api.getActivity(cursor = null, limit = limit).map { page ->
            LatestActivity(events = page.items.map { it.toDomain() }, unreadCount = page.unreadCount)
        }

    override suspend fun markAllRead(): ApiResult<Unit> = api.markAllRead()

    override suspend fun markRead(eventId: String): ApiResult<Unit> = api.markRead(eventId)
}
