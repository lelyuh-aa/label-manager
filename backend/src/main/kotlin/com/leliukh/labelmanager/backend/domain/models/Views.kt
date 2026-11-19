package com.leliukh.labelmanager.backend.domain.models

/**
 * An artist together with their open listing.
 *
 * @property artist the artist
 * @property listing the open listing, if the artist is on the market
 */
data class ArtistView(
    val artist: Artist,
    val listing: Listing?,
)

/**
 * A label with its roster.
 *
 * @property label the label
 * @property artists the roster
 */
data class LabelView(
    val label: Label,
    val artists: List<ArtistView>,
) {

    /** Sum of the roster's market values. */
    val value: Long
        get() = artists.sumOf { it.artist.marketValue }
}

/**
 * A deal with both labels resolved.
 *
 * @property deal the deal
 * @property from the selling label
 * @property to the buying label
 */
data class DealView(
    val deal: Deal,
    val from: Label,
    val to: Label,
)

/**
 * Everything the artist screen shows.
 *
 * @property artist the artist and their listing
 * @property label the owning label
 * @property deals the artist's deals, newest first
 */
data class ArtistDetailsView(
    val artist: ArtistView,
    val label: Label,
    val deals: List<DealView>,
)

/**
 * An open listing with the artist and the seller resolved.
 *
 * @property listing the listing
 * @property artist the listed artist
 * @property seller the selling label
 */
data class ListingView(
    val listing: Listing,
    val artist: ArtistView,
    val seller: Label,
)

/**
 * Outcome of a purchase.
 *
 * @property deal the completed deal
 * @property artist the bought artist with the new market value
 * @property budget the buyer's budget after paying
 */
data class PurchaseView(
    val deal: DealView,
    val artist: ArtistView,
    val budget: Long,
)

/**
 * An activity event with the other label resolved.
 *
 * @property event the event
 * @property counterparty the other label of a deal
 */
data class ActivityView(
    val event: ActivityEvent,
    val counterparty: Label?,
)

/**
 * One page of a cursor-paginated list.
 *
 * @property items the items of this page
 * @property nextCursor cursor of the next page; null on the last page
 * @property totalCount number of matching items across all pages
 */
data class Page<T>(
    val items: List<T>,
    val nextCursor: String?,
    val totalCount: Int,
)

/**
 * A page of activity events.
 *
 * @property page the events
 * @property unreadCount number of unread events across all pages
 */
data class ActivityPage(
    val page: Page<ActivityView>,
    val unreadCount: Int,
)
