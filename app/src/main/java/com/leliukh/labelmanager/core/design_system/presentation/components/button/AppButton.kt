package com.leliukh.labelmanager.core.design_system.presentation.components.button

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.preview.PreviewSurface
import com.leliukh.labelmanager.core.design_system.presentation.preview.ThemePreviews
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper

private const val PRESSED_SCALE = 0.97f
private const val DISABLED_ALPHA = 0.4f

/**
 * Visual weight of an [AppButton].
 */
enum class AppButtonStyle {
    /** The one main action of a screen, solid accent. */
    Primary,

    /** A secondary action, pastel accent. */
    Secondary,

    /** An irreversible action such as signing out. */
    Destructive,
}

/**
 * Full-width call-to-action button.
 *
 * While [isLoading] it shows a spinner instead of the label and ignores taps, so a request
 * cannot be sent twice. It shrinks slightly while pressed.
 *
 * @param text the label
 * @param onClick called on tap
 * @param modifier modifier of the button
 * @param style visual weight
 * @param icon optional leading icon
 * @param trailingIcon optional icon after the label, e.g. a forward arrow
 * @param enabled whether the button reacts to taps
 * @param isLoading whether to show progress instead of the label
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: AppButtonStyle = AppButtonStyle.Primary,
    icon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    val colors = AppThemeWrapper.colors
    val (container, content) = when (style) {
        AppButtonStyle.Primary -> colors.accent to colors.onAccent

        AppButtonStyle.Secondary -> colors.accentSoft to colors.accentText

        AppButtonStyle.Destructive -> colors.negativeSoft to colors.negative
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) PRESSED_SCALE else 1f, label = "buttonScale")
    val alpha = if (enabled) 1f else DISABLED_ALPHA

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AppThemeWrapper.dimens.controlHeight)
            .scale(scale)
            .clip(AppThemeWrapper.shapes.control)
            .background(container.copy(alpha = container.alpha * alpha))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = content),
                enabled = enabled && !isLoading,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = AppThemeWrapper.dimens.space5),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "buttonContent",
        ) { loading ->
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = content,
                    strokeWidth = 2.5.dp,
                )
            } else {
                ButtonLabel(
                    text = text,
                    icon = icon,
                    trailingIcon = trailingIcon,
                    color = content.copy(alpha = alpha),
                )
            }
        }
    }
}

/**
 * Small pill button used inside list rows, e.g. "Buy".
 *
 * @param text the label
 * @param onClick called on tap
 * @param modifier modifier of the button
 * @param enabled whether the button reacts to taps
 */
@Composable
fun CapsuleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = AppThemeWrapper.colors

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 64.dp)
            .height(30.dp)
            .clip(AppThemeWrapper.shapes.pill)
            .background(if (enabled) colors.accentSoft else colors.fill)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = AppThemeWrapper.typography.callout,
            color = if (enabled) colors.accentText else colors.textSecondary,
        )
    }
}

@Composable
private fun ButtonLabel(
    text: String,
    icon: ImageVector?,
    trailingIcon: ImageVector?,
    color: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, tint = color)

            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(text = text, style = AppThemeWrapper.typography.bodySemibold, color = color)

        if (trailingIcon != null) {
            Spacer(modifier = Modifier.width(8.dp))

            Icon(imageVector = trailingIcon, contentDescription = null, tint = color)
        }
    }
}

@ThemePreviews
@Composable
private fun AppButtonStylesPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppButton(text = "Sign in", onClick = {})

            AppButton(text = "Put on market", onClick = {}, style = AppButtonStyle.Secondary)

            AppButton(text = "Sign out", onClick = {}, style = AppButtonStyle.Destructive)

            AppButton(text = "Sell artist", onClick = {}, icon = AppIcons.Plus)
        }
    }
}

@ThemePreviews
@Composable
private fun AppButtonStatesPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppButton(text = "Save", onClick = {}, enabled = false)

            AppButton(text = "Save", onClick = {}, isLoading = true)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CapsuleButton(text = "Buy", onClick = {})

                CapsuleButton(text = "Buy", onClick = {}, enabled = false)
            }
        }
    }
}
