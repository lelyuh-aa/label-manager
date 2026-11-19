package com.leliukh.labelmanager.core.design_system.presentation.components.button

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.preview.PreviewSurface
import com.leliukh.labelmanager.core.design_system.presentation.preview.ThemePreviews
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper

/**
 * Look of a [CircleIconButton].
 */
enum class CircleIconButtonStyle {
    /** White disc on the screen background, accent icon: edit, back. */
    Surface,

    /** Pastel accent disc: filters, toggles that are on. */
    Tinted,

    /** No disc, secondary icon: inline toggles such as hiding the budget. */
    Plain,
}

/**
 * Round icon-only button with a 48 dp touch target.
 *
 * @param icon the icon
 * @param contentDescription what the button does, read by screen readers
 * @param onClick called on tap
 * @param modifier modifier of the button
 * @param style visual style
 * @param size diameter of the visible disc
 */
@Composable
fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: CircleIconButtonStyle = CircleIconButtonStyle.Surface,
    size: Dp = 40.dp,
) {
    val colors = AppThemeWrapper.colors
    val (container, content) = when (style) {
        CircleIconButtonStyle.Surface -> colors.surface to colors.accentText

        CircleIconButtonStyle.Tinted -> colors.accentSoft to colors.accentText

        CircleIconButtonStyle.Plain -> colors.surface.copy(alpha = 0f) to colors.textSecondary
    }
    val elevation = if (style == CircleIconButtonStyle.Surface && !colors.isDark) 1.dp else 0.dp

    Box(
        modifier = modifier
            .size(AppThemeWrapper.dimens.minTouchTarget)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClickLabel = contentDescription, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .shadow(
                    elevation = elevation,
                    shape = CircleShape,
                    ambientColor = colors.shadow,
                    spotColor = colors.shadow,
                )
                .background(container, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = content,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun CircleIconButtonPreview() {
    PreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CircleIconButton(icon = AppIcons.Pencil, contentDescription = "Edit label", onClick = {})

            CircleIconButton(
                icon = AppIcons.ChevronLeft,
                contentDescription = "Back",
                onClick = {},
            )

            CircleIconButton(
                icon = AppIcons.Sliders,
                contentDescription = "Filters",
                onClick = {},
                style = CircleIconButtonStyle.Tinted,
            )

            CircleIconButton(
                icon = AppIcons.Eye,
                contentDescription = "Hide budget",
                onClick = {},
                style = CircleIconButtonStyle.Plain,
            )
        }
    }
}
