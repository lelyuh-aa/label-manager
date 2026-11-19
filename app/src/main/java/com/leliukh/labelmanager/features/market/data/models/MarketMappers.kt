package com.leliukh.labelmanager.features.market.data.models

import com.leliukh.labelmanager.contract.dto.market.ListingDto
import com.leliukh.labelmanager.contract.dto.market.ListingSortDto
import com.leliukh.labelmanager.contract.dto.market.PurchaseResultDto
import com.leliukh.labelmanager.contract.dto.artist.GenreDto
import com.leliukh.labelmanager.contract.routes.ApiQuery
import com.leliukh.labelmanager.features.market.domain.models.Listing
import com.leliukh.labelmanager.features.market.domain.models.ListingFilter
import com.leliukh.labelmanager.features.market.domain.models.ListingSort
import com.leliukh.labelmanager.features.market.domain.models.Purchase
import com.leliukh.labelmanager.features.market.domain.models.SearchScope
import com.leliukh.labelmanager.features.artist.data.models.toDomain
import com.leliukh.labelmanager.features.label.data.models.toDomain
import com.leliukh.labelmanager.features.label.domain.models.Genre

/**
 * @param myLabelId id of the signed-in user's label, to mark own listings
 */
fun ListingDto.toDomain(myLabelId: String?): Listing = Listing(
    id = id,
    artist = artist.toDomain(),
    seller = seller.toDomain(),
    askingPrice = askingPrice,
    listedAt = listedAt,
    isMine = seller.id == myLabelId,
)

fun PurchaseResultDto.toDomain(): Purchase = Purchase(
    deal = deal.toDomain(),
    artist = artist.toDomain(),
    budget = budget,
)

/** The filter as query parameters of the listings endpoint; filters that are off are left out. */
fun ListingFilter.toQuery(): Map<String, String> = buildMap {
    val trimmed = text.trim()
    if (trimmed.isNotEmpty()) {
        put(if (scope == SearchScope.Artists) ApiQuery.ARTIST_NAME else ApiQuery.LABEL_NAME, trimmed)
    }
    minPrice?.let { put(ApiQuery.MIN_PRICE, it.toString()) }
    maxPrice?.let { put(ApiQuery.MAX_PRICE, it.toString()) }
    if (genres.isNotEmpty()) {
        put(ApiQuery.GENRES, genres.sorted().joinToString(",") { it.toDto().name })
    }
    artistCountry?.let { put(ApiQuery.ARTIST_COUNTRY, it) }
    labelCountry?.let { put(ApiQuery.LABEL_COUNTRY, it) }
    put(ApiQuery.SORT, sort.toDto().name)
}

private fun Genre.toDto(): GenreDto = when (this) {
    Genre.Pop -> GenreDto.POP

    Genre.Rock -> GenreDto.ROCK

    Genre.HipHop -> GenreDto.HIP_HOP

    Genre.Electronic -> GenreDto.ELECTRONIC
}

private fun ListingSort.toDto(): ListingSortDto = when (this) {
    ListingSort.Newest -> ListingSortDto.NEWEST

    ListingSort.PriceAscending -> ListingSortDto.PRICE_ASCENDING

    ListingSort.PriceDescending -> ListingSortDto.PRICE_DESCENDING
}
