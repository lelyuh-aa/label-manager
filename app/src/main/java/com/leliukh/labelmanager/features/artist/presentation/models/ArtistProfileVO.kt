package com.leliukh.labelmanager.features.artist.presentation.models

import com.leliukh.labelmanager.features.label.domain.models.Genre
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

/**
 * Everything the artist profile shows.
 *
 * @property artistId id of the artist
 * @property firstName first name
 * @property lastName last name
 * @property initials letters for the avatar
 * @property genre playing genre
 * @property flag flag emoji of the nationality
 * @property countryName localised nationality
 * @property age age in full years
 * @property birthDate date of birth
 * @property hits career hits
 * @property marketValue market value in whole US dollars
 * @property valueRise growth since the last deal, e.g. "45%"; null without growth
 * @property labelName name of the owning label
 * @property labelFlag flag emoji of the owning label's country
 * @property isMine whether the artist belongs to the user's label
 * @property listing the open marketing, if the artist is for sale
 * @property history deals, newest first, ending with the founding roster entry
 */
data class ArtistProfileVO(
    val artistId: String,
    val firstName: String,
    val lastName: String,
    val initials: String,
    val genre: Genre,
    val flag: String,
    val countryName: String,
    val age: Int,
    val birthDate: LocalDate,
    val hits: Int,
    val marketValue: Long,
    val valueRise: String?,
    val labelName: String,
    val labelFlag: String,
    val isMine: Boolean,
    val listing: ListingVO?,
    val history: List<HistoryEntryVO>,
)

/**
 * @property listingId id of the listing
 * @property askingPrice price a buyer pays
 * @property listedAt moment of listing
 * @property priceVsValue how the price compares with the market value
 */
data class ListingVO(
    val listingId: String,
    val askingPrice: Long,
    val listedAt: Instant,
    val priceVsValue: PriceComparisonVO,
)

/**
 * An asking price compared with the market value.
 *
 * @property percent the difference, e.g. "60%"; empty when they are equal
 * @property isAbove whether the price is above the value
 * @property isEqual whether the price equals the value
 */
data class PriceComparisonVO(
    val percent: String,
    val isAbove: Boolean,
    val isEqual: Boolean,
)

/** A line of the deal history. */
sealed interface HistoryEntryVO {

    /**
     * A completed deal.
     *
     * @property signedAt moment of the deal
     * @property fromName selling label
     * @property fromFlag flag of the selling label
     * @property toName buying label
     * @property toFlag flag of the buying label
     * @property price price paid
     * @property valueBefore market value before
     * @property valueAfter market value after
     * @property rise growth from the deal, e.g. "45%"
     */
    data class Move(
        val signedAt: Instant,
        val fromName: String,
        val fromFlag: String,
        val toName: String,
        val toFlag: String,
        val price: Long,
        val valueBefore: Long,
        val valueAfter: Long,
        val rise: String,
    ) : HistoryEntryVO

    /**
     * The artist's first label, from the generated roster of a new account.
     *
     * @property labelName the first label
     * @property labelFlag its flag
     * @property value the starting market value
     */
    data class Founding(
        val labelName: String,
        val labelFlag: String,
        val value: Long,
    ) : HistoryEntryVO
}
