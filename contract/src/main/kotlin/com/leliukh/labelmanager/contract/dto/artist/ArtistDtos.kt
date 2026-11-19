package com.leliukh.labelmanager.contract.dto.artist

import com.leliukh.labelmanager.contract.dto.label.LabelSummaryDto
import com.leliukh.labelmanager.contract.dto.deal.DealDto
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Music genre. A new label signs four artists of each.
 */
@Serializable
public enum class GenreDto {
    POP,
    ROCK,
    HIP_HOP,
    ELECTRONIC,
}

/**
 * An artist as seen in lists and in the owner's roster.
 *
 * @property id unique artist id
 * @property labelId id of the label that owns the artist
 * @property firstName first name, editable by the owner
 * @property lastName last name, editable by the owner
 * @property countryCode ISO 3166-1 alpha-2 nationality, editable by the owner
 * @property genre playing genre
 * @property birthDate date of birth
 * @property age age in full years on the server's current date, between 18 and 40 at creation
 * @property marketValue current market value in whole US dollars
 * @property hits hits scored in the artist's career
 * @property listing the artist's open marketing, if any
 */
@Serializable
public data class ArtistDto(
    val id: String,
    val labelId: String,
    val firstName: String,
    val lastName: String,
    val countryCode: String,
    val genre: GenreDto,
    val birthDate: LocalDate,
    val age: Int,
    val marketValue: Long,
    val hits: Int,
    val listing: ListingSummaryDto? = null,
)

/**
 * Short form of a marketing, embedded in [ArtistDto].
 *
 * @property id unique listing id
 * @property askingPrice price a buyer pays, in whole US dollars
 * @property listedAt moment the artist was put on the market
 */
@Serializable
public data class ListingSummaryDto(
    val id: String,
    val askingPrice: Long,
    val listedAt: Instant,
)

/**
 * Response of [com.leliukh.labelmanager.contract.routes.ApiRoutes.ARTIST].
 *
 * @property artist the artist
 * @property label the label that owns the artist
 * @property deals the artist's deals, newest first
 */
@Serializable
public data class ArtistDetailsDto(
    val artist: ArtistDto,
    val label: LabelSummaryDto,
    val deals: List<DealDto>,
)

/**
 * Body of a `PATCH` to [com.leliukh.labelmanager.contract.routes.ApiRoutes.ARTIST]. Only the
 * owner may edit an artist; absent fields stay as they are.
 *
 * @property firstName new first name
 * @property lastName new last name
 * @property countryCode new ISO 3166-1 alpha-2 nationality
 */
@Serializable
public data class UpdateArtistRequestDto(
    val firstName: String? = null,
    val lastName: String? = null,
    val countryCode: String? = null,
)
