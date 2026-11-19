package com.leliukh.labelmanager.backend.domain.services

import com.leliukh.labelmanager.backend.data.StoreReader
import com.leliukh.labelmanager.backend.domain.errors.DomainException
import com.leliukh.labelmanager.backend.domain.models.Listing
import com.leliukh.labelmanager.backend.domain.models.ListingView
import com.leliukh.labelmanager.backend.domain.models.Artist
import com.leliukh.labelmanager.backend.domain.models.ArtistView
import com.leliukh.labelmanager.backend.domain.models.Label
import com.leliukh.labelmanager.backend.domain.models.LabelView
import com.leliukh.labelmanager.backend.domain.models.Deal
import com.leliukh.labelmanager.backend.domain.models.DealView

/**
 * @param labelId label id
 * @return the label, or a not-found error
 */
internal fun StoreReader.requireLabel(labelId: String): Label =
    findLabel(labelId) ?: throw DomainException.NotFound("No label with id $labelId")

/**
 * @param artistId artist id
 * @return the artist, or a not-found error
 */
internal fun StoreReader.requireArtist(artistId: String): Artist =
    findArtist(artistId) ?: throw DomainException.NotFound("No artist with id $artistId")

internal fun StoreReader.artistView(artist: Artist): ArtistView =
    ArtistView(artist, findListingOfArtist(artist.id))

internal fun StoreReader.labelView(label: Label): LabelView {
    val listingsByArtist =
        allListings().filter { it.sellerLabelId == label.id }.associateBy(Listing::artistId)
    val artists = artistsOfLabel(label.id).map { ArtistView(it, listingsByArtist[it.id]) }
    return LabelView(label, artists)
}

internal fun StoreReader.dealView(deal: Deal): DealView =
    DealView(deal, requireLabel(deal.fromLabelId), requireLabel(deal.toLabelId))

internal fun StoreReader.listingView(listing: Listing): ListingView =
    ListingView(
        listing = listing,
        artist = ArtistView(requireArtist(listing.artistId), listing),
        seller = requireLabel(listing.sellerLabelId),
    )
