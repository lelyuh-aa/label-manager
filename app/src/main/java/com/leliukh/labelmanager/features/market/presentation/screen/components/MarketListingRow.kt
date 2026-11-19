package com.leliukh.labelmanager.features.market.presentation.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.design_system.presentation.components.button.CapsuleButton
import com.leliukh.labelmanager.core.design_system.presentation.components.chip.StatusBadge
import com.leliukh.labelmanager.core.design_system.presentation.components.list.GroupPosition
import com.leliukh.labelmanager.core.design_system.presentation.components.list.GroupedItem
import com.leliukh.labelmanager.core.design_system.presentation.components.list.ListRow
import com.leliukh.labelmanager.core.design_system.presentation.components.money.MoneyText
import com.leliukh.labelmanager.core.design_system.presentation.components.artist.ArtistAvatar
import com.leliukh.labelmanager.core.design_system.presentation.preview.PreviewSurface
import com.leliukh.labelmanager.core.design_system.presentation.preview.ThemePreviews
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper
import com.leliukh.labelmanager.core.formatting.presentation.relativeTime
import com.leliukh.labelmanager.features.market.presentation.models.MarketListingVO
import com.leliukh.labelmanager.features.label.domain.models.Genre
import com.leliukh.labelmanager.features.label.presentation.models.colors
import com.leliukh.labelmanager.features.label.presentation.models.shortLabelRes
import kotlin.time.Instant

/**
 * A listing in the market list: the artist with the selling label, age and listing time, the
 * asking price and Buy, or a "Yours" mark on the user's own listings.
 *
 * @param listing the listing
 * @param position where the row sits in its group
 * @param now the current moment, for the listing time
 * @param onClick opens the artist
 * @param onBuy opens the buy confirmation
 * @param modifier modifier of the row
 */
@Composable
fun MarketListingRow(
    listing: MarketListingVO,
    position: GroupPosition,
    now: Instant,
    onClick: () -> Unit,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GroupedItem(position = position, onClick = onClick, modifier = modifier) {
        ListRow(
            title = "${listing.fullName} ${listing.flag}",
            leading = {
                ArtistAvatar(
                    initials = listing.initials,
                    badge = stringResource(listing.genre.shortLabelRes),
                    colors = listing.genre.colors,
                )
            },
            subtitle = {
                Text(
                    text = stringResource(
                        R.string.market_listing_meta,
                        listing.sellerName,
                        pluralStringResource(R.plurals.artist_age, listing.age, listing.age),
                        relativeTime(listing.listedAt, now),
                    ),
                    style = AppThemeWrapper.typography.subhead,
                    color = AppThemeWrapper.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            trailing = {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    MoneyText(amount = listing.askingPrice)

                    if (listing.isMine) {
                        StatusBadge(text = stringResource(R.string.market_yours), isAccent = false)
                    } else {
                        CapsuleButton(text = stringResource(R.string.market_buy), onClick = onBuy)
                    }
                }
            },
        )
    }
}

@ThemePreviews
@Composable
private fun MarketListingRowPreview() {
    PreviewSurface {
        Column {
            MarketListingRow(
                listing = previewListing,
                position = GroupPosition.First,
                now = Instant.parse("2026-10-25T12:00:00Z"),
                onClick = {},
                onBuy = {},
            )

            MarketListingRow(
                listing = previewListing.copy(
                    listingId = "2",
                    fullName = "Jonas Vermeulen",
                    isMine = true,
                ),
                position = GroupPosition.Last,
                now = Instant.parse("2026-10-25T12:00:00Z"),
                onClick = {},
                onBuy = {},
            )
        }
    }
}

private val previewListing = MarketListingVO(
    listingId = "1",
    artistId = "p1",
    fullName = "Chidi Okafor",
    initials = "CO",
    genre = Genre.HipHop,
    flag = "🇳🇬",
    countryName = "Nigeria",
    age = 23,
    sellerName = "Lagos Sound",
    askingPrice = 1_450_000,
    listedAt = Instant.parse("2026-10-25T11:48:00Z"),
    isMine = false,
)
