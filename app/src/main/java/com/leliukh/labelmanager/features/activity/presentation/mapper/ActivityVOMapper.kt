package com.leliukh.labelmanager.features.activity.presentation.mapper

import com.leliukh.labelmanager.core.formatting.presentation.formatPercentChange
import com.leliukh.labelmanager.features.activity.domain.models.ActivityEvent
import com.leliukh.labelmanager.features.activity.domain.models.ActivityType
import com.leliukh.labelmanager.features.activity.presentation.models.ActivityFilter
import com.leliukh.labelmanager.features.activity.presentation.models.ActivityItemVO
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun ActivityEvent.toVO(): ActivityItemVO.Event = ActivityItemVO.Event(
    id = id,
    type = type,
    artistId = artistId,
    artistName = artistName,
    genre = genre,
    counterpartyName = counterparty?.name,
    amount = amount,
    valueAfter = valueAfter,
    valueRise = if (valueBefore != null && valueAfter != null && valueAfter > valueBefore) {
        formatPercentChange(valueBefore, valueAfter)
    } else {
        null
    },
    occurredAt = occurredAt,
    isRead = isRead,
)

/**
 * @param filter the chosen chip
 * @return whether the event belongs to it
 */
fun ActivityEvent.matches(filter: ActivityFilter): Boolean = when (filter) {
    ActivityFilter.All -> true

    ActivityFilter.Sales -> type == ActivityType.ArtistSold

    ActivityFilter.Purchases -> type == ActivityType.ArtistBought

    ActivityFilter.Listings ->
        type == ActivityType.ArtistListed || type == ActivityType.ArtistUnlisted
}

/**
 * @param before the event above in the feed; null at the top
 * @param after the event below in the feed; null at the bottom
 * @param timeZone the user's time zone
 * @return a header for the day of [after] when it starts a new day
 */
fun dayHeaderBetween(
    before: ActivityItemVO.Event?,
    after: ActivityItemVO.Event?,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): ActivityItemVO.DayHeader? {
    val afterDay = after?.occurredAt?.toLocalDateTime(timeZone)?.date ?: return null
    val beforeDay: LocalDate? = before?.occurredAt?.toLocalDateTime(timeZone)?.date
    return if (beforeDay == afterDay) null else ActivityItemVO.DayHeader(afterDay)
}
