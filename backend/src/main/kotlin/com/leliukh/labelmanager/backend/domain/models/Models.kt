package com.leliukh.labelmanager.backend.domain.models

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/** Playing genre of a [Artist]. */
@Serializable
enum class Genre {
    POP,
    ROCK,
    HIP_HOP,
    ELECTRONIC,
}

/**
 * A registered account.
 *
 * @property id unique account id
 * @property email normalised (trimmed, lower-case) email, unique across accounts
 * @property passwordHash self-describing password hash, see
 *   [com.leliukh.labelmanager.backend.security.PasswordHasher]
 * @property labelId id of the only label the account owns
 * @property createdAt moment of registration
 */
@Serializable
data class User(
    val id: String,
    val email: String,
    val passwordHash: String,
    val labelId: String,
    val createdAt: Instant,
)

/**
 * A label: either owned by a user or a rival label run by the simulation.
 *
 * @property id unique label id
 * @property name label name
 * @property countryCode ISO 3166-1 alpha-2 country
 * @property budget money available for deals, in whole US dollars
 * @property ownerUserId id of the owning account; null for a rival label
 */
@Serializable
data class Label(
    val id: String,
    val name: String,
    val countryCode: String,
    val budget: Long,
    val ownerUserId: String?,
) {

    /** Whether a user owns the label, as opposed to a rival label. */
    val isUserOwned: Boolean
        get() = ownerUserId != null
}

/**
 * An artist of a label.
 *
 * @property id unique artist id
 * @property labelId id of the label the artist belongs to
 * @property firstName first name
 * @property lastName last name
 * @property countryCode ISO 3166-1 alpha-2 nationality
 * @property genre playing genre
 * @property birthDate date of birth
 * @property marketValue current market value in whole US dollars
 * @property hits hits scored in the artist's career
 */
@Serializable
data class Artist(
    val id: String,
    val labelId: String,
    val firstName: String,
    val lastName: String,
    val countryCode: String,
    val genre: Genre,
    val birthDate: LocalDate,
    val marketValue: Long,
    val hits: Int,
) {

    /** First and last name separated by a space. */
    val fullName: String
        get() = "$firstName $lastName"
}

/**
 * An open marketing. A listing is deleted once the artist is sold or withdrawn.
 *
 * @property id unique listing id
 * @property artistId id of the listed artist
 * @property sellerLabelId id of the label selling the artist
 * @property askingPrice price a buyer pays, in whole US dollars
 * @property listedAt moment the listing was created
 */
@Serializable
data class Listing(
    val id: String,
    val artistId: String,
    val sellerLabelId: String,
    val askingPrice: Long,
    val listedAt: Instant,
)

/**
 * A completed deal.
 *
 * @property id unique deal id
 * @property artistId id of the signed artist
 * @property artistName artist's full name at the moment of the deal
 * @property fromLabelId id of the selling label
 * @property toLabelId id of the buying label
 * @property price price paid, in whole US dollars
 * @property valueBefore market value before the deal
 * @property valueAfter market value after the deal
 * @property signedAt moment of the deal
 */
@Serializable
data class Deal(
    val id: String,
    val artistId: String,
    val artistName: String,
    val fromLabelId: String,
    val toLabelId: String,
    val price: Long,
    val valueBefore: Long,
    val valueAfter: Long,
    val signedAt: Instant,
)

/** Kind of an [ActivityEvent], from the point of view of the label that receives it. */
@Serializable
enum class ActivityType {
    ARTIST_BOUGHT,
    ARTIST_SOLD,
    ARTIST_LISTED,
    ARTIST_UNLISTED,
}

/**
 * Something that happened to a user-owned label.
 *
 * @property id unique event id
 * @property labelId id of the label the event belongs to
 * @property type what happened
 * @property occurredAt moment it happened
 * @property artistId id of the artist involved
 * @property artistFirstName artist's first name at that moment
 * @property artistLastName artist's last name at that moment
 * @property artistGenre artist's genre
 * @property counterpartyLabelId id of the other label of a deal
 * @property amount deal price or asking price
 * @property valueBefore market value before a deal
 * @property valueAfter market value after a deal
 * @property isRead whether the owner marked the event read
 */
@Serializable
data class ActivityEvent(
    val id: String,
    val labelId: String,
    val type: ActivityType,
    val occurredAt: Instant,
    val artistId: String,
    val artistFirstName: String,
    val artistLastName: String,
    val artistGenre: Genre,
    val counterpartyLabelId: String? = null,
    val amount: Long? = null,
    val valueBefore: Long? = null,
    val valueAfter: Long? = null,
    val isRead: Boolean = false,
)

/**
 * A refresh token that can still be exchanged. Only its hash is stored.
 *
 * @property tokenHash SHA-256 of the token, hex encoded
 * @property userId id of the account it was issued to
 * @property expiresAt moment it stops being accepted
 */
@Serializable
data class RefreshToken(
    val tokenHash: String,
    val userId: String,
    val expiresAt: Instant,
)
