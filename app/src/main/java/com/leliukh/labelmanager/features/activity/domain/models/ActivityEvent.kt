package com.leliukh.labelmanager.features.activity.domain.models

import com.leliukh.labelmanager.features.artist.domain.models.LabelSummary
import com.leliukh.labelmanager.features.label.domain.models.Genre
import kotlin.time.Instant

/** What happened, from the point of view of the user's label. */
enum class ActivityType {
    ArtistBought,
    ArtistSold,
    ArtistListed,
    ArtistUnlisted,
}

/**
 * Something that happened to the user's label.
 *
 * @property id unique event id
 * @property type what happened
 * @property occurredAt moment it happened
 * @property artistId id of the artist involved
 * @property artistName the artist's name at that moment
 * @property genre the artist's genre
 * @property counterparty the other label of a deal; null for listing events
 * @property amount the deal price or the asking price
 * @property valueBefore market value before a deal
 * @property valueAfter market value after a deal
 * @property isRead whether the event was marked read
 */
data class ActivityEvent(
    val id: String,
    val type: ActivityType,
    val occurredAt: Instant,
    val artistId: String,
    val artistName: String,
    val genre: Genre,
    val counterparty: LabelSummary?,
    val amount: Long?,
    val valueBefore: Long?,
    val valueAfter: Long?,
    val isRead: Boolean,
)
