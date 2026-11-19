package com.leliukh.labelmanager.contract.dto.market

import com.leliukh.labelmanager.contract.dto.artist.ArtistDto
import com.leliukh.labelmanager.contract.dto.label.LabelSummaryDto
import com.leliukh.labelmanager.contract.dto.deal.DealDto
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * An artist on the market, as returned by
 * [com.leliukh.labelmanager.contract.routes.ApiRoutes.LISTINGS].
 *
 * @property id unique listing id
 * @property artist the listed artist
 * @property seller the label selling the artist
 * @property askingPrice price a buyer pays, in whole US dollars
 * @property listedAt moment the artist was put on the market
 */
@Serializable
public data class ListingDto(
    val id: String,
    val artist: ArtistDto,
    val seller: LabelSummaryDto,
    val askingPrice: Long,
    val listedAt: Instant,
)

/**
 * Body of a `POST` to [com.leliukh.labelmanager.contract.routes.ApiRoutes.LISTINGS].
 *
 * @property artistId id of one of the caller's artists that is not listed yet
 * @property askingPrice price a buyer will pay, in whole US dollars
 */
@Serializable
public data class CreateListingRequestDto(
    val artistId: String,
    val askingPrice: Long,
)

/**
 * Response of [com.leliukh.labelmanager.contract.routes.ApiRoutes.LISTING_PURCHASE].
 *
 * @property deal the completed deal
 * @property artist the bought artist, now in the caller's label, with the new market value
 * @property budget the caller's budget after paying
 */
@Serializable
public data class PurchaseResultDto(
    val deal: DealDto,
    val artist: ArtistDto,
    val budget: Long,
)

/**
 * Sort order of [com.leliukh.labelmanager.contract.routes.ApiRoutes.LISTINGS].
 */
@Serializable
public enum class ListingSortDto {
    NEWEST,
    PRICE_ASCENDING,
    PRICE_DESCENDING,
}
