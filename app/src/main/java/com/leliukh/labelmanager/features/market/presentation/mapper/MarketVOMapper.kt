package com.leliukh.labelmanager.features.market.presentation.mapper

import com.leliukh.labelmanager.core.formatting.presentation.countryDisplayName
import com.leliukh.labelmanager.core.formatting.presentation.countryFlagEmoji
import com.leliukh.labelmanager.features.market.domain.models.Listing
import com.leliukh.labelmanager.features.market.domain.models.ListingFilter
import com.leliukh.labelmanager.features.market.domain.models.Purchase
import com.leliukh.labelmanager.features.market.presentation.models.FilterChipVO
import com.leliukh.labelmanager.features.market.presentation.models.MarketListingVO
import com.leliukh.labelmanager.features.market.presentation.navigation.DealDoneRoute
import com.leliukh.labelmanager.features.label.domain.models.Genre
import com.leliukh.labelmanager.features.label.presentation.mapper.initials

fun Listing.toVO(): MarketListingVO = MarketListingVO(
    listingId = id,
    artistId = artist.id,
    fullName = "${artist.firstName} ${artist.lastName}",
    initials = artist.initials,
    genre = artist.genre,
    flag = countryFlagEmoji(artist.countryCode),
    countryName = countryDisplayName(artist.countryCode),
    age = artist.age,
    sellerName = seller.name,
    askingPrice = askingPrice,
    listedAt = listedAt,
    isMine = isMine,
)

/** What the deal-done moment shows after this purchase. */
fun Purchase.toRoute(): DealDoneRoute = DealDoneRoute(
    artistId = artist.id,
    fullName = "${artist.firstName} ${artist.lastName}",
    initials = artist.initials,
    genre = artist.genre,
    countryCode = artist.countryCode,
    age = artist.age,
    labelName = deal.to.name,
    price = deal.price,
    valueBefore = deal.valueBefore,
    valueAfter = deal.valueAfter,
    budget = budget,
)

/** The active filters as chips, in the order of the filter sheet. The search text is not one. */
fun ListingFilter.toChips(): List<FilterChipVO> = buildList {
    if (minPrice != null || maxPrice != null) add(FilterChipVO.Price(minPrice, maxPrice))
    Genre.entries.filter { it in genres }.forEach { add(FilterChipVO.GenreChip(it)) }
    artistCountry?.let {
        add(FilterChipVO.ArtistCountry(countryDisplayName(it), countryFlagEmoji(it)))
    }
    labelCountry?.let {
        add(FilterChipVO.LabelCountry(countryDisplayName(it), countryFlagEmoji(it)))
    }
}

/**
 * @param chip a chip of this filter
 * @return the filter without what the chip stands for
 */
fun ListingFilter.without(chip: FilterChipVO): ListingFilter = when (chip) {
    is FilterChipVO.Price -> copy(minPrice = null, maxPrice = null)

    is FilterChipVO.GenreChip -> copy(genres = genres - chip.genre)

    is FilterChipVO.ArtistCountry -> copy(artistCountry = null)

    is FilterChipVO.LabelCountry -> copy(labelCountry = null)
}

/** The filters that narrow the list, without the search text and the sort order. */
fun ListingFilter.withoutSearch(): ListingFilter = copy(text = "", sort = ListingFilter().sort)
