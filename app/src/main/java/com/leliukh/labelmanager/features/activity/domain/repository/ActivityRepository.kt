package com.leliukh.labelmanager.features.activity.domain.repository

import androidx.paging.PagingData
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.features.activity.domain.models.ActivityEvent
import kotlinx.coroutines.flow.Flow

/**
 * What happened to the user's label: deals and listings, newest first.
 */
interface ActivityRepository {

    /** @return the events page by page */
    fun events(): Flow<PagingData<ActivityEvent>>

    /**
     * @param limit how many of the newest events to return
     * @return the newest events and the number of unread ones
     */
    suspend fun latest(limit: Int): ApiResult<LatestActivity>

    /** Marks every event read. */
    suspend fun markAllRead(): ApiResult<Unit>

    /** @param eventId the event to mark read */
    suspend fun markRead(eventId: String): ApiResult<Unit>
}

/**
 * The top of the activity feed.
 *
 * @property events the newest events
 * @property unreadCount unread events across the whole feed
 */
data class LatestActivity(
    val events: List<ActivityEvent>,
    val unreadCount: Int,
)
