package com.leliukh.labelmanager.backend.domain.services

import com.leliukh.labelmanager.backend.data.BackendStore
import com.leliukh.labelmanager.backend.domain.errors.DomainException
import com.leliukh.labelmanager.backend.domain.models.ListingView
import com.leliukh.labelmanager.backend.domain.models.Page
import com.leliukh.labelmanager.backend.domain.models.ArtistView
import com.leliukh.labelmanager.backend.domain.models.Genre
import com.leliukh.labelmanager.backend.domain.models.PurchaseView
import com.leliukh.labelmanager.backend.domain.paging.keysetPage
import com.leliukh.labelmanager.backend.domain.validation.validate
import kotlin.time.Clock

/** Sort order of the market list. */
enum class ListingSort {
    NEWEST,
    PRICE_ASCENDING,
    PRICE_DESCENDING,
}

/**
 * Filters and paging of the market list. Text filters match case-insensitive substrings; null or
 * empty filters match everything.
 *
 * @property artistName part of the artist's full name
 * @property labelName part of the selling label's name
 * @property artistCountry artist's nationality code
 * @property labelCountry selling label's country code
 * @property minPrice lowest asking price, inclusive
 * @property maxPrice highest asking price, inclusive
 * @property genres genres to include
 * @property sort sort order
 * @property cursor cursor of the previous page
 * @property limit page size
 */
data class ListingQuery(
    val artistName: String? = null,
    val labelName: String? = null,
    val artistCountry: String? = null,
    val labelCountry: String? = null,
    val minPrice: Long? = null,
    val maxPrice: Long? = null,
    val genres: Set<Genre> = emptySet(),
    val sort: ListingSort = ListingSort.NEWEST,
    val cursor: String? = null,
    val limit: Int = 20,
)

/**
 * The market: browsing, listing, withdrawing and buying artists.
 *
 * @param store persistent state
 * @param clock source of the current time
 * @param executor applies the market rules inside transactions
 */
class MarketService(
    private val store: BackendStore,
    private val clock: Clock,
    private val executor: DealExecutor,
) {

    /**
     * @param query filters, sort order and paging
     * @return the matching open listings
     */
    suspend fun listings(query: ListingQuery): Page<ListingView> = store.read {
        allListings()
            .map { listingView(it) }
            .filter { it.matches(query) }
            .keysetPage(
                cursor = query.cursor,
                limit = query.limit,
                key = { view ->
                    when (query.sort) {
                        ListingSort.NEWEST -> -view.listing.listedAt.toEpochMilliseconds()

                        ListingSort.PRICE_ASCENDING -> view.listing.askingPrice

                        ListingSort.PRICE_DESCENDING -> -view.listing.askingPrice
                    }
                },
                id = { it.listing.id },
            )
    }

    /**
     * @param callerLabelId id of the caller's label, which must own the artist
     * @param artistId id of the artist to list
     * @param askingPrice price a buyer will pay
     * @return the new listing
     */
    suspend fun createListing(
        callerLabelId: String,
        artistId: String,
        askingPrice: Long,
    ): ListingView {
        validate { price("askingPrice", askingPrice) }
        return store.write {
            val artist = requireArtist(artistId)
            if (artist.labelId != callerLabelId) {
                throw DomainException.Forbidden("Only the owner can list an artist")
            }
            val listing = with(executor) { list(artist, askingPrice, clock.now()) }
            listingView(listing)
        }
    }

    /**
     * @param callerLabelId id of the caller's label, which must be the seller
     * @param listingId id of the listing to withdraw
     */
    suspend fun withdraw(callerLabelId: String, listingId: String) {
        store.write {
            val listing =
                findListing(listingId)
                    ?: throw DomainException.NotFound("No open listing with id $listingId")
            if (listing.sellerLabelId != callerLabelId) {
                throw DomainException.Forbidden("Only the seller can withdraw a listing")
            }
            with(executor) { withdraw(listing, clock.now()) }
        }
    }

    /**
     * Buys a listed artist for the asking price. Atomic: of two simultaneous buyers only one gets
     * the artist, the other gets [DomainException.ListingNotAvailable].
     *
     * @param callerLabelId id of the buying label
     * @param listingId id of the listing to buy
     * @return the deal, the artist and the buyer's new budget
     */
    suspend fun purchase(callerLabelId: String, listingId: String): PurchaseView = store.write {
        val listing = findListing(listingId) ?: throw DomainException.ListingNotAvailable()
        val deal = with(executor) { purchase(listing, callerLabelId, clock.now()) }
        PurchaseView(
            deal = dealView(deal),
            artist = ArtistView(requireArtist(deal.artistId), listing = null),
            budget = requireLabel(callerLabelId).budget,
        )
    }

    private fun ListingView.matches(query: ListingQuery): Boolean =
        query.artistName.matchesPart(artist.artist.fullName) &&
            query.labelName.matchesPart(seller.name) &&
            (query.artistCountry.isNullOrEmpty() ||
                artist.artist.countryCode == query.artistCountry) &&
            (query.labelCountry.isNullOrEmpty() || seller.countryCode == query.labelCountry) &&
            (query.minPrice == null || listing.askingPrice >= query.minPrice) &&
            (query.maxPrice == null || listing.askingPrice <= query.maxPrice) &&
            (query.genres.isEmpty() || artist.artist.genre in query.genres)

    private fun String?.matchesPart(text: String): Boolean =
        isNullOrBlank() || text.contains(trim(), ignoreCase = true)
}
