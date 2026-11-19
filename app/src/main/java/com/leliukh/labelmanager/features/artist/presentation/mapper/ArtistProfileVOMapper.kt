package com.leliukh.labelmanager.features.artist.presentation.mapper

import com.leliukh.labelmanager.core.formatting.presentation.countryDisplayName
import com.leliukh.labelmanager.core.formatting.presentation.countryFlagEmoji
import com.leliukh.labelmanager.core.formatting.presentation.formatPercentChange
import com.leliukh.labelmanager.features.artist.domain.models.ArtistDetails
import com.leliukh.labelmanager.features.artist.presentation.models.HistoryEntryVO
import com.leliukh.labelmanager.features.artist.presentation.models.ListingVO
import com.leliukh.labelmanager.features.artist.presentation.models.ArtistProfileVO
import com.leliukh.labelmanager.features.artist.presentation.models.PriceComparisonVO
import com.leliukh.labelmanager.features.label.domain.models.STARTING_MARKET_VALUE
import com.leliukh.labelmanager.features.label.presentation.mapper.initials

fun ArtistDetails.toProfileVO(): ArtistProfileVO {
    val lastDeal = deals.firstOrNull()
    val firstLabel = deals.lastOrNull()?.from ?: label
    return ArtistProfileVO(
        artistId = artist.id,
        firstName = artist.firstName,
        lastName = artist.lastName,
        initials = artist.initials,
        genre = artist.genre,
        flag = countryFlagEmoji(artist.countryCode),
        countryName = countryDisplayName(artist.countryCode),
        age = artist.age,
        birthDate = artist.birthDate,
        hits = artist.hits,
        marketValue = artist.marketValue,
        valueRise = lastDeal
            ?.takeIf { artist.marketValue > it.valueBefore }
            ?.let { formatPercentChange(it.valueBefore, artist.marketValue) },
        labelName = label.name,
        labelFlag = countryFlagEmoji(label.countryCode),
        isMine = isMine,
        listing = artist.listing?.let { listing ->
            ListingVO(
                listingId = listing.id,
                askingPrice = listing.askingPrice,
                listedAt = listing.listedAt,
                priceVsValue = PriceComparisonVO(
                    percent = formatPercentChange(artist.marketValue, listing.askingPrice),
                    isAbove = listing.askingPrice > artist.marketValue,
                    isEqual = listing.askingPrice == artist.marketValue,
                ),
            )
        },
        history = deals.map { deal ->
            HistoryEntryVO.Move(
                signedAt = deal.signedAt,
                fromName = deal.from.name,
                fromFlag = countryFlagEmoji(deal.from.countryCode),
                toName = deal.to.name,
                toFlag = countryFlagEmoji(deal.to.countryCode),
                price = deal.price,
                valueBefore = deal.valueBefore,
                valueAfter = deal.valueAfter,
                rise = formatPercentChange(deal.valueBefore, deal.valueAfter),
            )
        } + HistoryEntryVO.Founding(
            labelName = firstLabel.name,
            labelFlag = countryFlagEmoji(firstLabel.countryCode),
            value = STARTING_MARKET_VALUE,
        ),
    )
}
