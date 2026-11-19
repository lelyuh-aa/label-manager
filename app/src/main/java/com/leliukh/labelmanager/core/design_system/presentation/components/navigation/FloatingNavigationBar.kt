package com.leliukh.labelmanager.core.design_system.presentation.components.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.preview.PreviewSurface
import com.leliukh.labelmanager.core.design_system.presentation.preview.ThemePreviews
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper

private const val MAX_BADGE = 99

/**
 * One tab of the [FloatingNavigationBar].
 *
 * @property label the tab name
 * @property icon the tab icon
 * @property badgeCount number shown in a badge; null or zero hides it
 */
data class NavigationBarTab(
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int? = null,
)

/**
 * Pill-shaped navigation bar that floats above the content, with a pastel indicator that
 * springs to the selected tab. Put a [BottomFade] behind it so content fades out under it.
 *
 * This is the navigation bar of the app, not to be confused with the system navigation bar
 * below it.
 *
 * The bar is opaque: an elevation shadow shows through a translucent fill as a grey band.
 *
 * @param items the tabs
 * @param selectedIndex index of the selected tab
 * @param onSelect called with the index of the tapped tab
 * @param modifier modifier of the bar
 */
@Composable
fun FloatingNavigationBar(
    items: List<NavigationBarTab>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeWrapper.colors
    val shape = AppThemeWrapper.shapes.pill

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(AppThemeWrapper.dimens.navigationBarHeight)
            .shadow(
                elevation = 18.dp,
                shape = shape,
                ambientColor = colors.shadow,
                spotColor = colors.shadow,
            )
            .clip(shape)
            .background(colors.navigationBarSolid)
            .border(1.dp, colors.navigationBarBorder, shape)
            .padding(5.dp),
    ) {
        val tabWidth = maxWidth / items.size.coerceAtLeast(1)
        val indicatorOffset by animateDpAsState(
            targetValue = tabWidth * selectedIndex,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
            label = "tabIndicator",
        )

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(tabWidth)
                .fillMaxHeight()
                .background(colors.accentSoft, shape),
        )

        Row(modifier = Modifier.fillMaxSize().selectableGroup()) {
            items.forEachIndexed { index, item ->
                Tab(
                    item = item,
                    isSelected = index == selectedIndex,
                    onClick = { onSelect(index) },
                    width = tabWidth,
                )
            }
        }
    }
}

/**
 * Gradient from transparent to the screen background, laid under floating controls so the
 * content scrolling below them fades out instead of being cut off.
 *
 * @param modifier modifier of the fade, normally aligned to the bottom of the screen
 * @param height height of the fade
 */
@Composable
fun BottomFade(modifier: Modifier = Modifier, height: Dp = 150.dp) {
    val background = AppThemeWrapper.colors.background

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                Brush.verticalGradient(
                    0f to background.copy(alpha = 0f),
                    0.45f to background.copy(alpha = 0.75f),
                    0.8f to background,
                ),
            ),
    )
}

@Composable
private fun Tab(item: NavigationBarTab, isSelected: Boolean, onClick: () -> Unit, width: Dp) {
    val colors = AppThemeWrapper.colors
    val contentColor = if (isSelected) colors.accentText else colors.navigationInactive

    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .clip(AppThemeWrapper.shapes.pill)
            .selectable(selected = isSelected, role = Role.Tab, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(23.dp),
            )

            Text(text = item.label, style = AppThemeWrapper.typography.micro, color = contentColor)
        }

        val badgeCount = item.badgeCount ?: 0
        if (badgeCount > 0) {
            Text(
                text = if (badgeCount > MAX_BADGE) "$MAX_BADGE+" else badgeCount.toString(),
                style = AppThemeWrapper.typography.micro,
                color = colors.onAccent,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = 13.dp, y = 2.dp)
                    .background(colors.accent, CircleShape)
                    .border(2.dp, colors.navigationBarSolid, CircleShape)
                    .padding(horizontal = 6.dp, vertical = 1.dp),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun FloatingNavigationBarPreview() {
    val items = listOf(
        NavigationBarTab(label = "Roster", icon = AppIcons.Users),
        NavigationBarTab(label = "Market", icon = AppIcons.Swap),
        NavigationBarTab(label = "Activity", icon = AppIcons.Bell, badgeCount = 2),
        NavigationBarTab(label = "Profile", icon = AppIcons.Profile),
    )

    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FloatingNavigationBar(items = items, selectedIndex = 0, onSelect = {})

            FloatingNavigationBar(items = items, selectedIndex = 1, onSelect = {})
        }
    }
}
