package com.leliukh.labelmanager.core.design_system.presentation.components.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.leliukh.labelmanager.core.design_system.presentation.components.chip.CountBadge
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.preview.PreviewSurface
import com.leliukh.labelmanager.core.design_system.presentation.preview.ThemePreviews
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper

private val GroupCorner = 20.dp

/**
 * Where an item sits inside an inset group; decides which corners are rounded and whether a
 * separator is drawn above it.
 */
enum class GroupPosition {
    Single,
    First,
    Middle,
    Last,
    ;

    companion object {

        /**
         * @param index index of the item inside its group
         * @param count number of items in the group
         */
        fun of(index: Int, count: Int): GroupPosition = when {
            count <= 1 -> Single

            index == 0 -> First

            index == count - 1 -> Last

            else -> Middle
        }
    }
}

/**
 * One item of an inset grouped list. Each item draws its own part of the rounded card, so
 * groups work inside lazy lists without nesting a column per group.
 *
 * @param genre where the item sits in its group
 * @param modifier modifier of the item
 * @param separatorInset start inset of the separator above the item, aligned with the text
 * @param onClick called on tap; null for a static item
 * @param content the item content
 */
@Composable
fun GroupedItem(
    position: GroupPosition,
    modifier: Modifier = Modifier,
    separatorInset: Dp = 70.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = AppThemeWrapper.colors

    Box(
        modifier = modifier
            .padding(horizontal = AppThemeWrapper.dimens.space4)
            .clip(position.shape())
            .background(colors.surface)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        content()

        if (position == GroupPosition.Middle || position == GroupPosition.Last) {
            HorizontalDivider(
                modifier = Modifier.padding(start = separatorInset),
                thickness = 1.dp,
                color = colors.separator,
            )
        }
    }
}

/**
 * Standard row layout: leading content, a title with an optional subtitle, trailing content
 * and an optional chevron.
 *
 * @param title main line
 * @param modifier modifier of the row
 * @param subtitle optional second line
 * @param leading optional content before the text, e.g. an avatar
 * @param trailing optional content after the text, e.g. an amount
 * @param showChevron whether to end the row with a chevron
 */
@Composable
fun ListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: (@Composable () -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    showChevron: Boolean = false,
) {
    val colors = AppThemeWrapper.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(start = 16.dp, end = 12.dp, top = 11.dp, bottom = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        leading?.invoke()

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = AppThemeWrapper.typography.bodyMedium,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            subtitle?.invoke()
        }

        trailing?.invoke(this)

        if (showChevron) {
            Icon(
                imageVector = AppIcons.ChevronRight,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/**
 * Bold, sentence-case title above a group, with an optional count and trailing action.
 *
 * @param title the section name
 * @param modifier modifier of the header
 * @param count optional count shown in a badge
 * @param trailing optional content at the end, e.g. a sort button
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    count: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppThemeWrapper.dimens.screenTitleInset),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = AppThemeWrapper.typography.headline,
            color = AppThemeWrapper.colors.textPrimary,
        )

        if (count != null) {
            CountBadge(text = count)
        }

        if (trailing != null) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                trailing()
            }
        }
    }
}

private fun GroupPosition.shape(): Shape = when (this) {
    GroupPosition.Single -> RoundedCornerShape(GroupCorner)

    GroupPosition.First -> RoundedCornerShape(topStart = GroupCorner, topEnd = GroupCorner)

    GroupPosition.Middle -> RectangleShape

    GroupPosition.Last -> RoundedCornerShape(bottomStart = GroupCorner, bottomEnd = GroupCorner)
}

@ThemePreviews
@Composable
private fun GroupedListPreview() {
    val rows = listOf("Lukas Brenner" to "Germany · 27 y.o.", "Matteo Ricci" to "Italy · 31 y.o.")

    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeader(title = "Pop", count = "3")

            Column {
                rows.forEachIndexed { index, (name, details) ->
                    GroupedItem(
                        position = GroupPosition.of(index, rows.size),
                        separatorInset = 16.dp,
                        onClick = {},
                    ) {
                        ListRow(
                            title = name,
                            subtitle = {
                                Text(
                                    text = details,
                                    style = AppThemeWrapper.typography.subhead,
                                    color = AppThemeWrapper.colors.textSecondary,
                                )
                            },
                            showChevron = true,
                        )
                    }
                }
            }

            GroupedItem(position = GroupPosition.Single) {
                Box(modifier = Modifier.fillMaxWidth().height(48.dp))
            }
        }
    }
}
