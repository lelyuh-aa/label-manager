package com.leliukh.labelmanager.features.activity.domain.interactor

import androidx.paging.PagingData
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.network.domain.map
import com.leliukh.labelmanager.core.network.domain.onSuccess
import com.leliukh.labelmanager.features.activity.domain.models.ActivityEvent
import com.leliukh.labelmanager.features.activity.domain.models.ActivityType
import com.leliukh.labelmanager.features.activity.domain.repository.ActivityRepository
import com.leliukh.labelmanager.features.activity.domain.repository.LatestActivity
import com.leliukh.labelmanager.features.activity.domain.repository.SaleAlertRepository
import com.leliukh.labelmanager.features.label.domain.repository.LabelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

private const val LATEST_LIMIT = 20

/**
 * The label's activity feed and its unread count.
 *
 * The unread count is shared app-wide so the navigation badge, the feed and the background
 * check all agree. A new event usually means a deal by a rival label, so a check that finds
 * more unread events also refreshes the cached roster.
 *
 * @param activityRepository the activity endpoints
 * @param labelRepository the cached label
 * @param saleAlertRepository which sales were already announced
 */
@Singleton
class ActivityInteractor @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val labelRepository: LabelRepository,
    private val saleAlertRepository: SaleAlertRepository,
) {

    private val mutableUnreadCount = MutableStateFlow(0)

    /** Unread events as of the last check. */
    val unreadCount: StateFlow<Int> = mutableUnreadCount.asStateFlow()

    /** @return the events page by page */
    fun events(): Flow<PagingData<ActivityEvent>> = activityRepository.events()

    /**
     * Loads the newest events and updates [unreadCount].
     *
     * @return the newest events
     */
    suspend fun check(): ApiResult<LatestActivity> {
        val result = activityRepository.latest(LATEST_LIMIT)
        result.onSuccess { latest ->
            if (latest.unreadCount > mutableUnreadCount.value) labelRepository.refreshLabel()
            mutableUnreadCount.value = latest.unreadCount
        }
        return result
    }

    /**
     * Checks the feed for sales of the user's artists that were not announced yet. The first
     * check after installing only remembers where the feed is, so old sales are not announced.
     *
     * @return unread sales since the last check, oldest first
     */
    suspend fun newSales(): ApiResult<List<ActivityEvent>> {
        val lastAlertedAt = saleAlertRepository.lastAlertedAt()
        return check().map { latest ->
            latest.events.firstOrNull()?.let { saleAlertRepository.setLastAlertedAt(it.occurredAt) }
            if (lastAlertedAt == null) return@map emptyList()
            latest.events
                .filter { it.type == ActivityType.ArtistSold && !it.isRead }
                .filter { it.occurredAt > lastAlertedAt }
                .reversed()
        }
    }

    /**
     * Marks one unread event read and takes it off [unreadCount].
     *
     * @param eventId the event
     */
    suspend fun markRead(eventId: String): ApiResult<Unit> {
        val result = activityRepository.markRead(eventId)
        result.onSuccess { mutableUnreadCount.update { (it - 1).coerceAtLeast(0) } }
        return result
    }

    /** Marks every event read and clears [unreadCount]. */
    suspend fun markAllRead(): ApiResult<Unit> {
        val result = activityRepository.markAllRead()
        result.onSuccess { mutableUnreadCount.value = 0 }
        return result
    }
}
