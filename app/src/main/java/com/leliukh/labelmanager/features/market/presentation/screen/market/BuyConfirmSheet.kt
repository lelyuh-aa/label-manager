package com.leliukh.labelmanager.features.market.presentation.screen.market

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.design_system.presentation.components.button.AppButton
import com.leliukh.labelmanager.core.design_system.presentation.components.button.AppButtonStyle
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.Banner
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.BannerTone
import com.leliukh.labelmanager.core.design_system.presentation.components.artist.ArtistAvatar
import com.leliukh.labelmanager.core.design_system.presentation.components.sheet.AppBottomSheet
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper
import com.leliukh.labelmanager.core.formatting.presentation.formatMoney
import com.leliukh.labelmanager.features.market.presentation.models.BuyConfirmVO
import com.leliukh.labelmanager.features.label.presentation.models.colors
import com.leliukh.labelmanager.features.label.presentation.models.nameRes
import com.leliukh.labelmanager.features.label.presentation.models.shortLabelRes
import kotlin.math.abs

/**
 * Confirmation before buying: the artist, the budget now, the price and the budget after.
 * When the budget is short the sheet says by how much and offers to sell an artist instead.
 *
 * @param confirm what to show
 * @param onConfirm buys the artist
 * @param onSell opens the sell flow, when the budget is short
 * @param onDismiss closes the sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyConfirmSheet(
    confirm: BuyConfirmVO,
    onConfirm: () -> Unit,
    onSell: () -> Unit,
    onDismiss: () -> Unit,
) {
    val listing = confirm.listing
    val colors = AppThemeWrapper.colors

    AppBottomSheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ArtistAvatar(
                initials = listing.initials,
                badge = stringResource(listing.genre.shortLabelRes),
                colors = listing.genre.colors,
                size = 64.dp,
            )

            Text(
                text = listing.fullName,
                style = AppThemeWrapper.typography.title,
                color = colors.textPrimary,
                modifier = Modifier.padding(top = 6.dp),
            )

            Text(
                text = "${listing.flag} ${stringResource(listing.genre.nameRes)} · " +
                    pluralStringResource(R.plurals.artist_age, listing.age, listing.age) +
                    " · ${listing.sellerName}",
                style = AppThemeWrapper.typography.subhead,
                color = colors.textSecondary,
            )
        }

        Column(
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()
                .background(colors.fill, AppThemeWrapper.shapes.card)
                .padding(horizontal = 16.dp, vertical = 6.dp),
        ) {
            AmountRow(
                label = stringResource(R.string.market_buy_budget),
                amount = formatMoney(confirm.budget),
            )

            AmountRow(
                label = stringResource(R.string.market_buy_price),
                amount = "−${formatMoney(listing.askingPrice)}",
            )

            HorizontalDivider(thickness = 1.dp, color = colors.separator)

            AmountRow(
                label = stringResource(
                    if (confirm.canAfford) {
                        R.string.market_buy_after
                    } else {
                        R.string.market_buy_short_label
                    },
                ),
                amount = formatMoney(abs(confirm.budgetAfter)),
                color = if (confirm.canAfford) colors.textPrimary else colors.negative,
                isBold = true,
            )
        }

        if (confirm.errorRes != null) {
            Banner(
                message = stringResource(confirm.errorRes),
                tone = BannerTone.Error,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        Row(
            modifier = Modifier.padding(top = 14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = if (confirm.canAfford) AppIcons.TrendingUp else AppIcons.Info,
                contentDescription = null,
                tint = if (confirm.canAfford) colors.positive else colors.negative,
                modifier = Modifier.size(18.dp),
            )

            Text(
                text = if (confirm.canAfford) {
                    stringResource(R.string.market_buy_note)
                } else {
                    stringResource(R.string.market_buy_short, formatMoney(abs(confirm.budgetAfter)))
                },
                style = AppThemeWrapper.typography.subhead,
                color = colors.textSecondary,
            )
        }

        Column(
            modifier = Modifier.padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (confirm.canAfford) {
                AppButton(
                    text = stringResource(R.string.market_buy_confirm),
                    onClick = onConfirm,
                    isLoading = confirm.isBuying,
                )

                AppButton(
                    text = stringResource(R.string.market_buy_cancel),
                    onClick = onDismiss,
                    style = AppButtonStyle.Secondary,
                    enabled = !confirm.isBuying,
                )
            } else {
                AppButton(text = stringResource(R.string.market_sell_artist), onClick = onSell)

                AppButton(
                    text = stringResource(R.string.market_buy_not_now),
                    onClick = onDismiss,
                    style = AppButtonStyle.Secondary,
                )
            }
        }
    }
}

@Composable
private fun AmountRow(
    label: String,
    amount: String,
    color: Color = AppThemeWrapper.colors.textPrimary,
    isBold: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = AppThemeWrapper.typography.callout,
            color = AppThemeWrapper.colors.textSecondary,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = amount,
            style = if (isBold) {
                AppThemeWrapper.typography.moneyLarge
            } else {
                AppThemeWrapper.typography.money
            },
            color = color,
        )
    }
}
