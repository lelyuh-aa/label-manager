package com.leliukh.labelmanager.features.activity.presentation.models

import com.leliukh.labelmanager.features.activity.domain.models.ActivityType
import com.leliukh.labelmanager.features.label.domain.models.Genre
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

/** Which events the feed shows. */
enum class ActivityFilter {
    All,
    Sales,
    Purchases,
    Listings,
}

/** An item of the activity feed: a day header or an event. */
sealed interface ActivityItemVO {

    /** Key of the item in the list. */
    val key: String

    /** @property date the day the following events happened */
    data class DayHeader(val date: LocalDate) : ActivityItemVO {

        override val key: String
            get() = "day-$date"
    }

    /**
     * @property id unique event id
     * @property type what happened
     * @property artistId id of the artist involved
     * @property artistName the artist's name at that moment
     * @property genre the artist's genre
     * @property counterpartyName the other label of a deal
     * @property amount the deal price or the asking price
     * @property valueAfter market value after a deal
     * @property valueRise growth from a deal, e.g. "35%"
     * @property occurredAt moment it happened
     * @property isRead whether the event was marked read
     */
    data class Event(
        val id: String,
        val type: ActivityType,
        val artistId: String,
        val artistName: String,
        val genre: Genre,
        val counterpartyName: String?,
        val amount: Long?,
        val valueAfter: Long?,
        val valueRise: String?,
        val occurredAt: Instant,
        val isRead: Boolean,
    ) : ActivityItemVO {

        override val key: String
            get() = id
    }
}
