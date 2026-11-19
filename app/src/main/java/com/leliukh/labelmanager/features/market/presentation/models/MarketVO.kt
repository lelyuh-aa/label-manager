package com.leliukh.labelmanager.features.market.presentation.models

import com.leliukh.labelmanager.features.label.domain.models.Genre
import kotlin.time.Instant

/**
 * A row of the market list.
 *
 * @property listingId id of the listing
 * @property artistId id of the listed artist
 * @property fullName first and last name
 * @property initials letters for the avatar
 * @property genre playing genre
 * @property flag flag emoji of the artist's nationality
 * @property countryName localised nationality
 * @property age age in full years
 * @property sellerName name of the selling label
 * @property askingPrice price a buyer pays
 * @property listedAt moment of listing
 * @property isMine whether the user is the seller
 */
data class MarketListingVO(
    val listingId: String,
    val artistId: String,
    val fullName: String,
    val initials: String,
    val genre: Genre,
    val flag: String,
    val countryName: String,
    val age: Int,
    val sellerName: String,
    val askingPrice: Long,
    val listedAt: Instant,
    val isMine: Boolean,
)

/** An active filter shown as a removable chip under the search field. */
sealed interface FilterChipVO {

    /**
     * @property minPrice lowest price, if set
     * @property maxPrice highest price, if set
     */
    data class Price(val minPrice: Long?, val maxPrice: Long?) : FilterChipVO

    /** @property genre the genre */
    data class GenreChip(val genre: Genre) : FilterChipVO

    /**
     * @property countryName localised country
     * @property flag its flag emoji
     */
    data class ArtistCountry(val countryName: String, val flag: String) : FilterChipVO

    /**
     * @property countryName localised country
     * @property flag its flag emoji
     */
    data class LabelCountry(val countryName: String, val flag: String) : FilterChipVO
}

/**
 * The buy confirmation sheet.
 *
 * @property listing the listing to buy
 * @property budget the user's budget now
 * @property isBuying whether the purchase request is running
 * @property errorRes why the last attempt failed
 */
data class BuyConfirmVO(
    val listing: MarketListingVO,
    val budget: Long,
    val isBuying: Boolean = false,
    val errorRes: Int? = null,
) {

    /** Budget left after paying; negative when the budget is short. */
    val budgetAfter: Long
        get() = budget - listing.askingPrice

    /** Whether the budget covers the asking price. */
    val canAfford: Boolean
        get() = budgetAfter >= 0
}
