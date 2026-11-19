package com.leliukh.labelmanager.backend.domain.services

import com.leliukh.labelmanager.backend.data.StoreWriter
import com.leliukh.labelmanager.backend.domain.errors.DomainException
import com.leliukh.labelmanager.backend.domain.ids.IdGenerator
import com.leliukh.labelmanager.backend.domain.models.ActivityEvent
import com.leliukh.labelmanager.backend.domain.models.ActivityType
import com.leliukh.labelmanager.backend.domain.models.Listing
import com.leliukh.labelmanager.backend.domain.models.Artist
import com.leliukh.labelmanager.backend.domain.models.Label
import com.leliukh.labelmanager.backend.domain.models.Deal
import com.leliukh.labelmanager.backend.domain.rules.GameRules
import kotlin.time.Instant

/**
 * Carries out market operations inside a store transaction. Shared by user requests and the rival
 * labels simulation, so both follow exactly the same rules.
 *
 * @param idGenerator source of deal, listing and event ids
 */
class DealExecutor(
    private val idGenerator: IdGenerator,
) {

    /**
     * Opens a listing for an artist.
     *
     * @param artist the artist to list
     * @param askingPrice price a buyer will pay
     * @param at moment of listing
     * @return the new listing
     * @throws DomainException.ArtistAlreadyListed when the artist has an open listing
     */
    fun StoreWriter.list(artist: Artist, askingPrice: Long, at: Instant): Listing {
        if (findListingOfArtist(artist.id) != null) throw DomainException.ArtistAlreadyListed()
        val listing = Listing(idGenerator.next(), artist.id, artist.labelId, askingPrice, at)
        saveListing(listing)
        recordActivity(
            requireLabel(artist.labelId),
            ActivityType.ARTIST_LISTED,
            artist,
            at,
            amount = askingPrice,
        )
        return listing
    }

    /**
     * Closes a listing without a sale.
     *
     * @param listing the listing to withdraw
     * @param at moment of withdrawal
     */
    fun StoreWriter.withdraw(listing: Listing, at: Instant) {
        deleteListing(listing.id)
        val artist = requireArtist(listing.artistId)
        recordActivity(requireLabel(listing.sellerLabelId), ActivityType.ARTIST_UNLISTED, artist, at)
    }

    /**
     * Sells a listed artist to a buyer: the buyer pays the asking price to the seller, the artist
     * joins the buyer and gains 10–100 % of market value, the listing closes.
     *
     * @param listing the listing to buy
     * @param buyerLabelId id of the buying label
     * @param at moment of the deal
     * @return the recorded deal
     * @throws DomainException.CannotBuyOwnArtist when the buyer is the seller
     * @throws DomainException.InsufficientBudget when the buyer cannot pay the asking price
     */
    fun StoreWriter.purchase(listing: Listing, buyerLabelId: String, at: Instant): Deal {
        if (listing.sellerLabelId == buyerLabelId) throw DomainException.CannotBuyOwnArtist()
        val buyer = requireLabel(buyerLabelId)
        val seller = requireLabel(listing.sellerLabelId)
        if (buyer.budget < listing.askingPrice) {
            throw DomainException.InsufficientBudget(listing.askingPrice, buyer.budget)
        }
        val artist = requireArtist(listing.artistId)
        val newValue = GameRules.valueAfterDeal(artist.marketValue, artist.hits)
        val deal =
            Deal(
                id = idGenerator.next(),
                artistId = artist.id,
                artistName = artist.fullName,
                fromLabelId = seller.id,
                toLabelId = buyer.id,
                price = listing.askingPrice,
                valueBefore = artist.marketValue,
                valueAfter = newValue,
                signedAt = at,
            )
        deleteListing(listing.id)
        saveLabel(buyer.copy(budget = buyer.budget - listing.askingPrice))
        saveLabel(seller.copy(budget = seller.budget + listing.askingPrice))
        saveArtist(artist.copy(labelId = buyer.id, marketValue = newValue))
        saveDeal(deal)
        recordActivity(buyer, ActivityType.ARTIST_BOUGHT, artist, at, seller, deal)
        recordActivity(seller, ActivityType.ARTIST_SOLD, artist, at, buyer, deal)
        return deal
    }

    private fun StoreWriter.recordActivity(
        label: Label,
        type: ActivityType,
        artist: Artist,
        at: Instant,
        counterparty: Label? = null,
        deal: Deal? = null,
        amount: Long? = deal?.price,
    ) {
        if (!label.isUserOwned) return
        saveActivity(
            ActivityEvent(
                id = idGenerator.next(),
                labelId = label.id,
                type = type,
                occurredAt = at,
                artistId = artist.id,
                artistFirstName = artist.firstName,
                artistLastName = artist.lastName,
                artistGenre = artist.genre,
                counterpartyLabelId = counterparty?.id,
                amount = amount,
                valueBefore = deal?.valueBefore,
                valueAfter = deal?.valueAfter,
            )
        )
    }
}
