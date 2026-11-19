package com.leliukh.labelmanager.contract.dto.activity

import com.leliukh.labelmanager.contract.dto.artist.GenreDto
import com.leliukh.labelmanager.contract.dto.label.LabelSummaryDto
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Kind of an [ActivityEventDto], always from the point of view of the caller's label.
 */
@Serializable
public enum class ActivityTypeDto {
    ARTIST_BOUGHT,
    ARTIST_SOLD,
    ARTIST_LISTED,
    ARTIST_UNLISTED,
}

/**
 * Something that happened to the caller's label.
 *
 * @property id unique event id
 * @property type what happened
 * @property occurredAt moment it happened
 * @property artist the artist involved
 * @property counterparty the other label of a deal; null for listing events
 * @property amount money involved: the deal price or the asking price
 * @property valueBefore artist's market value before a deal
 * @property valueAfter artist's market value after a deal
 * @property isRead whether the event was marked read
 */
@Serializable
public data class ActivityEventDto(
    val id: String,
    val type: ActivityTypeDto,
    val occurredAt: Instant,
    val artist: ActivityArtistDto,
    val counterparty: LabelSummaryDto? = null,
    val amount: Long? = null,
    val valueBefore: Long? = null,
    val valueAfter: Long? = null,
    val isRead: Boolean,
)

/**
 * The artist an [ActivityEventDto] is about, as they were when it happened.
 *
 * @property id unique artist id
 * @property firstName first name
 * @property lastName last name
 * @property genre playing genre
 */
@Serializable
public data class ActivityArtistDto(
    val id: String,
    val firstName: String,
    val lastName: String,
    val genre: GenreDto,
)

/**
 * Response of [com.leliukh.labelmanager.contract.routes.ApiRoutes.ACTIVITY].
 *
 * @property items events of this page, newest first
 * @property nextCursor value for the `cursor` query parameter of the next page; null on the
 * last page
 * @property unreadCount number of unread events across all pages
 */
@Serializable
public data class ActivityPageDto(
    val items: List<ActivityEventDto>,
    val nextCursor: String? = null,
    val unreadCount: Int,
)
