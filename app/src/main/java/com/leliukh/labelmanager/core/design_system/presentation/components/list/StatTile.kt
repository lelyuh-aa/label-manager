package com.leliukh.labelmanager.core.design_system.presentation.components.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.leliukh.labelmanager.core.design_system.presentation.components.button.CircleIconButton
import com.leliukh.labelmanager.core.design_system.presentation.components.button.CircleIconButtonStyle
import com.leliukh.labelmanager.core.design_system.presentation.components.money.MoneyText
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.preview.PreviewSurface
import com.leliukh.labelmanager.core.design_system.presentation.preview.ThemePreviews
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper
import com.leliukh.labelmanager.core.design_system.presentation.theme.ColorPair

/**
 * Card with an icon tile, a label and a value, e.g. the budget or the roster value.
 *
 * @param icon icon in the tile
 * @param iconColors colors of the icon tile
 * @param label label above the value
 * @param modifier modifier of the card
 * @param topEnd optional content in the top-end corner, e.g. a mask toggle or a caption
 * @param value the value, normally a [MoneyText]
 */
@Composable
fun StatTile(
    icon: ImageVector,
    iconColors: ColorPair,
    label: String,
    modifier: Modifier = Modifier,
    topEnd: (@Composable () -> Unit)? = null,
    value: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(AppThemeWrapper.shapes.card)
            .background(AppThemeWrapper.colors.surface)
            .semantics(mergeDescendants = false) { isTraversalGroup = true }
            .padding(start = 16.dp, end = 10.dp, top = 12.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(36.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(iconColors.container, AppThemeWrapper.shapes.small),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColors.content,
                    modifier = Modifier.size(18.dp),
                )
            }

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                topEnd?.invoke()
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = AppThemeWrapper.typography.subheadMedium,
                color = AppThemeWrapper.colors.textSecondary,
            )

            value()
        }
    }
}

@ThemePreviews
@Composable
private fun StatTilePreview() {
    val colors = AppThemeWrapper.colors

    PreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                icon = AppIcons.Wallet,
                iconColors = ColorPair(colors.accentText, colors.accentSoft),
                label = "Budget",
                modifier = Modifier.weight(1f),
                topEnd = {
                    CircleIconButton(
                        icon = AppIcons.Eye,
                        contentDescription = "Hide budget",
                        onClick = {},
                        style = CircleIconButtonStyle.Plain,
                    )
                },
            ) {
                MoneyText(amount = 3_000_000, style = AppThemeWrapper.typography.moneyLarge)
            }

            StatTile(
                icon = AppIcons.Music,
                iconColors = ColorPair(colors.info, colors.infoSoft),
                label = "Roster value",
                modifier = Modifier.weight(1f),
                topEnd = {
                    Text(
                        text = "20 artists",
                        style = AppThemeWrapper.typography.subheadMedium,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(end = 6.dp),
                    )
                },
            ) {
                MoneyText(amount = 11_240_000, style = AppThemeWrapper.typography.moneyLarge)
            }
        }
    }
}
