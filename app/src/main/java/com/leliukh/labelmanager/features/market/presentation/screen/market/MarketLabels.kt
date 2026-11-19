package com.leliukh.labelmanager.features.market.presentation.screen.market

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.formatting.presentation.formatMoneyCompact
import com.leliukh.labelmanager.features.market.domain.models.ListingSort
import com.leliukh.labelmanager.features.market.presentation.models.FilterChipVO
import com.leliukh.labelmanager.features.label.presentation.models.titleRes

/**
 * A price range in words, e.g. "Up to $2M"; null when neither end is set.
 *
 * @param minPrice lowest price
 * @param maxPrice highest price
 */
@Composable
internal fun priceLabel(minPrice: Long?, maxPrice: Long?): String? = when {
    minPrice != null && maxPrice != null -> stringResource(
        R.string.market_chip_price_range,
        formatMoneyCompact(minPrice),
        formatMoneyCompact(maxPrice),
    )

    minPrice != null ->
        stringResource(R.string.market_chip_price_from, formatMoneyCompact(minPrice))

    maxPrice != null ->
        stringResource(R.string.market_chip_price_up_to, formatMoneyCompact(maxPrice))

    else -> null
}

/** Label of an active filter chip. */
@Composable
internal fun FilterChipVO.label(): String = when (this) {
    is FilterChipVO.Price -> priceLabel(minPrice, maxPrice).orEmpty()

    is FilterChipVO.GenreChip -> stringResource(genre.titleRes)

    is FilterChipVO.ArtistCountry ->
        stringResource(R.string.market_chip_artist_country, flag, countryName)

    is FilterChipVO.LabelCountry ->
        stringResource(R.string.market_chip_label_country, flag, countryName)
}

/** Label of a sort order. */
@Composable
internal fun ListingSort.label(): String = stringResource(
    when (this) {
        ListingSort.Newest -> R.string.market_sort_newest

        ListingSort.PriceAscending -> R.string.market_sort_price_ascending

        ListingSort.PriceDescending -> R.string.market_sort_price_descending
    },
)
