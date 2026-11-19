package com.leliukh.labelmanager.core.design_system.presentation.components.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.design_system.presentation.components.button.CircleIconButton
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.preview.PreviewSurface
import com.leliukh.labelmanager.core.design_system.presentation.preview.ThemePreviews
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper

/**
 * Top bar of pushed screens: a back button, an optional centered title and actions.
 *
 * @param onBack called by the back button
 * @param modifier modifier of the bar
 * @param title optional centered title
 * @param backIcon icon of the back button; a cross for modal flows
 * @param actions optional actions at the end
 */
@Composable
fun AppTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    backIcon: ImageVector = AppIcons.ChevronLeft,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 8.dp),
    ) {
        CircleIconButton(
            icon = backIcon,
            contentDescription = stringResource(
                if (backIcon == AppIcons.Close) R.string.common_close else R.string.common_back,
            ),
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart),
        )

        if (title != null) {
            Text(
                text = title,
                style = AppThemeWrapper.typography.bodySemibold,
                color = AppThemeWrapper.colors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 64.dp)
                    .semantics { heading() },
            )
        }

        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically,
            content = actions,
        )
    }
}

/**
 * Text action for the end of an [AppTopBar], e.g. "Save". While [isLoading] it shows a small
 * spinner instead of the label and ignores taps.
 *
 * @param text the label
 * @param onClick called on tap
 * @param modifier modifier of the action
 * @param enabled whether the action reacts to taps
 * @param isLoading whether to show progress instead of the label
 */
@Composable
fun TopBarTextAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    val colors = AppThemeWrapper.colors

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(AppThemeWrapper.shapes.pill)
            .clickable(enabled = enabled && !isLoading, role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = colors.accent,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = text,
                style = AppThemeWrapper.typography.bodySemibold,
                color = if (enabled) colors.accentText else colors.textTertiary,
            )
        }
    }
}

/**
 * Large bold screen title of the tab screens.
 *
 * @param text the title
 * @param modifier modifier of the title
 */
@Composable
fun LargeTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = AppThemeWrapper.typography.largeTitle,
        color = AppThemeWrapper.colors.textPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.semantics { heading() },
    )
}

/**
 * Floating accent action button that collapses to an icon while the list scrolls down.
 *
 * @param text the label
 * @param icon the icon
 * @param onClick called on tap
 * @param modifier modifier of the button
 * @param isExpanded whether the label is shown
 */
@Composable
fun ExtendedFab(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = true,
) {
    val colors = AppThemeWrapper.colors
    val shape = AppThemeWrapper.shapes.pill

    Row(
        modifier = modifier
            .height(54.dp)
            .shadow(14.dp, shape, ambientColor = colors.accent, spotColor = colors.accent)
            .clip(shape)
            .background(colors.accent)
            .clickable(role = Role.Button, onClickLabel = text, onClick = onClick)
            .padding(horizontal = 17.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = if (isExpanded) null else text,
            tint = colors.onAccent,
            modifier = Modifier.size(20.dp),
        )

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally(),
        ) {
            Row {
                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = text,
                    style = AppThemeWrapper.typography.bodySemibold,
                    color = colors.onAccent,
                    modifier = Modifier.padding(end = 3.dp),
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun TopBarsPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppTopBar(
                onBack = {},
                title = "Edit label",
                actions = { TopBarTextAction(text = "Save", onClick = {}) },
            )

            AppTopBar(
                onBack = {},
                title = "Edit artist",
                actions = { TopBarTextAction(text = "Save", onClick = {}, enabled = false) },
            )

            AppTopBar(onBack = {}, backIcon = AppIcons.Close, title = "Sell an artist")

            LargeTitle(text = "Market")

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ExtendedFab(text = "Sell artist", icon = AppIcons.Plus, onClick = {})

                ExtendedFab(
                    text = "Sell artist",
                    icon = AppIcons.Plus,
                    onClick = {},
                    isExpanded = false,
                )
            }
        }
    }
}
