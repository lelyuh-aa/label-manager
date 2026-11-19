package com.leliukh.labelmanager.core.design_system.presentation.components.chip

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.preview.PreviewSurface
import com.leliukh.labelmanager.core.design_system.presentation.preview.ThemePreviews
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper

/**
 * An applied filter that can be removed with one tap.
 *
 * @param label what the filter does, e.g. "Up to $2M"
 * @param onRemove called when the chip is tapped
 * @param modifier modifier of the chip
 */
@Composable
fun RemovableChip(
    label: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeWrapper.colors
    val removeLabel = stringResource(R.string.common_remove, label)

    Row(
        modifier = modifier
            .height(AppThemeWrapper.dimens.chipHeight)
            .clip(AppThemeWrapper.shapes.pill)
            .background(colors.accentSoft)
            .clickable(role = Role.Button, onClickLabel = removeLabel, onClick = onRemove)
            .padding(start = 12.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = label, style = AppThemeWrapper.typography.callout, color = colors.accentText)

        Icon(
            imageVector = AppIcons.Close,
            contentDescription = null,
            tint = colors.accentText,
            modifier = Modifier.size(14.dp),
        )
    }
}

/**
 * A toggleable option, e.g. a genre in the market filters.
 *
 * @param label the option
 * @param isSelected whether the option is on
 * @param onClick called on tap
 * @param modifier modifier of the chip
 */
@Composable
fun SelectableChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeWrapper.colors

    Row(
        modifier = modifier
            .height(36.dp)
            .clip(AppThemeWrapper.shapes.pill)
            .background(if (isSelected) colors.accentSoft else colors.fill)
            .semantics { selected = isSelected }
            .clickable(role = Role.Checkbox, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (isSelected) {
            Icon(
                imageVector = AppIcons.Check,
                contentDescription = null,
                tint = colors.accentText,
                modifier = Modifier.size(15.dp),
            )
        }

        Text(
            text = label,
            style = AppThemeWrapper.typography.callout,
            color = if (isSelected) colors.accentText else colors.textPrimary,
        )
    }
}

/**
 * A small neutral count next to a section title, e.g. the 3 of "Pop 3".
 *
 * @param text the count
 * @param modifier modifier of the badge
 */
@Composable
fun CountBadge(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(AppThemeWrapper.shapes.pill)
            .background(AppThemeWrapper.colors.fill)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = text,
            style = AppThemeWrapper.typography.caption,
            color = AppThemeWrapper.colors.textSecondary,
        )
    }
}

/**
 * Pastel status label, e.g. "For sale · $1.6M" or "Yours".
 *
 * @param text the label
 * @param modifier modifier of the badge
 * @param isAccent whether to use the accent tint instead of the neutral fill
 */
@Composable
fun StatusBadge(text: String, modifier: Modifier = Modifier, isAccent: Boolean = true) {
    val colors = AppThemeWrapper.colors

    Box(
        modifier = modifier
            .clip(AppThemeWrapper.shapes.pill)
            .background(if (isAccent) colors.accentSoft else colors.fill)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = text,
            style = AppThemeWrapper.typography.caption,
            color = if (isAccent) colors.accentText else colors.textSecondary,
        )
    }
}

@ThemePreviews
@Composable
private fun ChipsPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RemovableChip(label = "HipHop", onRemove = {})

                RemovableChip(label = "Up to $2M", onRemove = {})
            }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectableChip(label = "Pop", isSelected = false, onClick = {})

                SelectableChip(label = "HipHop", isSelected = true, onClick = {})
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CountBadge(text = "6")

                StatusBadge(text = "For sale · $1.6M")

                StatusBadge(text = "Yours", isAccent = false)
            }
        }
    }
}
