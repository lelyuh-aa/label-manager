package com.leliukh.labelmanager.features.market.domain.models

import com.leliukh.labelmanager.features.artist.domain.models.LabelSummary
import com.leliukh.labelmanager.features.artist.domain.models.Deal
import com.leliukh.labelmanager.features.label.domain.models.Artist
import com.leliukh.labelmanager.features.label.domain.models.Genre
import kotlin.time.Instant

/**
 * An artist on the market.
 *
 * @property id unique listing id
 * @property artist the listed artist
 * @property seller the label selling the artist
 * @property askingPrice price a buyer pays, in whole US dollars
 * @property listedAt moment of listing
 * @property isMine whether the user's own label is the seller
 */
data class Listing(
    val id: String,
    val artist: Artist,
    val seller: LabelSummary,
    val askingPrice: Long,
    val listedAt: Instant,
    val isMine: Boolean,
)

/** What the market search text is matched against. */
enum class SearchScope {
    Artists,
    Labels,
}

/** Order of the market list. */
enum class ListingSort {
    Newest,
    PriceAscending,
    PriceDescending,
}

/**
 * Which listings to show. Empty and null fields do not filter.
 *
 * @property text search text, matched as a part of the artist's or the label's name
 * @property scope what [text] is matched against
 * @property minPrice lowest asking price, in whole US dollars
 * @property maxPrice highest asking price, in whole US dollars
 * @property genres genres to show; empty shows all
 * @property artistCountry nationality of the artists
 * @property labelCountry country of the selling labels
 * @property sort order of the list
 */
data class ListingFilter(
    val text: String = "",
    val scope: SearchScope = SearchScope.Artists,
    val minPrice: Long? = null,
    val maxPrice: Long? = null,
    val genres: Set<Genre> = emptySet(),
    val artistCountry: String? = null,
    val labelCountry: String? = null,
    val sort: ListingSort = ListingSort.Newest,
)

/**
 * A completed purchase.
 *
 * @property deal the deal
 * @property artist the bought artist, now in the user's label, with the new market value
 * @property budget the user's budget after paying
 */
data class Purchase(
    val deal: Deal,
    val artist: Artist,
    val budget: Long,
)
