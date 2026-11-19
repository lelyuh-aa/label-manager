package com.leliukh.labelmanager.features.activity.domain.repository

import androidx.paging.PagingData
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.features.activity.domain.interactor.ActivityInteractor
import com.leliukh.labelmanager.features.activity.domain.models.ActivityEvent
import com.leliukh.labelmanager.features.activity.domain.models.ActivityType
import com.leliukh.labelmanager.features.artist.domain.models.LabelSummary
import com.leliukh.labelmanager.features.label.domain.models.Genre
import com.leliukh.labelmanager.features.label.domain.repository.LabelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Instant

/**
 * [ActivityRepository] over a list of events, newest first.
 */
class FakeActivityRepository : ActivityRepository {

    var events: List<ActivityEvent> = emptyList()

    override fun events(): Flow<PagingData<ActivityEvent>> = flowOf(PagingData.from(events))

    override suspend fun latest(limit: Int): ApiResult<LatestActivity> = ApiResult.Success(
        LatestActivity(events = events.take(limit), unreadCount = events.count { !it.isRead }),
    )

    override suspend fun markAllRead(): ApiResult<Unit> {
        events = events.map { it.copy(isRead = true) }
        return ApiResult.Success(Unit)
    }

    override suspend fun markRead(eventId: String): ApiResult<Unit> {
        events = events.map { if (it.id == eventId) it.copy(isRead = true) else it }
        return ApiResult.Success(Unit)
    }
}

/** [SaleAlertRepository] in memory. */
class FakeSaleAlertRepository : SaleAlertRepository {

    var lastAlertedAt: Instant? = null

    override suspend fun lastAlertedAt(): Instant? = lastAlertedAt

    override suspend fun setLastAlertedAt(instant: Instant) {
        lastAlertedAt = instant
    }
}

/**
 * @param id event id
 * @param type what happened
 * @param at moment it happened
 * @param isRead whether it was read
 */
fun testEvent(
    id: String,
    type: ActivityType = ActivityType.ArtistSold,
    at: String = "2026-10-26T10:00:00Z",
    isRead: Boolean = false,
) = ActivityEvent(
    id = id,
    type = type,
    occurredAt = Instant.parse(at),
    artistId = "artist-$id",
    artistName = "Jonas Vermeulen",
    genre = Genre.Pop,
    counterparty = LabelSummary(id = "label-2", name = "Lagos Sound", countryCode = "NG"),
    amount = 1_600_000,
    valueBefore = 1_000_000,
    valueAfter = 1_370_000,
    isRead = isRead,
)

/**
 * @param labels the cached label the interactor refreshes
 * @return an [ActivityInteractor] over an empty feed
 */
fun activityInteractor(labels: LabelRepository) =
    ActivityInteractor(FakeActivityRepository(), labels, FakeSaleAlertRepository())
