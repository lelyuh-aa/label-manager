package com.leliukh.labelmanager.core.design_system.presentation.components.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.preview.PreviewSurface
import com.leliukh.labelmanager.core.design_system.presentation.preview.ThemePreviews
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper

/**
 * Tone of a [Banner].
 */
enum class BannerTone {
    Info,
    Success,
    Warning,
    Error,
}

/**
 * Inline message with an optional action, e.g. "You're offline — showing saved data".
 *
 * Announced by screen readers when it appears.
 *
 * @param message the message
 * @param modifier modifier of the banner
 * @param title optional bold first line above [message]
 * @param tone color tone
 * @param icon optional leading icon; defaults to one that matches [tone]
 * @param actionLabel optional action label
 * @param isActionLoading shows a spinner in place of the action while it runs
 * @param onAction called by the action
 */
@Composable
fun Banner(
    message: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    tone: BannerTone = BannerTone.Info,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    isActionLoading: Boolean = false,
    onAction: () -> Unit = {},
) {
    val colors = AppThemeWrapper.colors
    val (container, content, defaultIcon) = when (tone) {
        BannerTone.Info -> Triple(colors.accentSoft, colors.accentText, AppIcons.Info)

        BannerTone.Success -> Triple(colors.positiveSoft, colors.positive, AppIcons.Check)

        BannerTone.Warning -> Triple(colors.warningSoft, colors.warning, AppIcons.Warning)

        BannerTone.Error -> Triple(colors.negativeSoft, colors.negative, AppIcons.Warning)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppThemeWrapper.shapes.control)
            .background(container)
            .semantics { liveRegion = LiveRegionMode.Polite }
            .padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = icon ?: defaultIcon,
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(18.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = AppThemeWrapper.typography.bodySemibold,
                    color = content,
                )
            }

            Text(
                text = message,
                style = if (title != null) {
                    AppThemeWrapper.typography.subhead
                } else {
                    AppThemeWrapper.typography.subheadMedium
                },
                color = if (title != null) colors.textSecondary else colors.textPrimary,
            )
        }

        if (actionLabel != null) {
            // The spinner takes the label's place, so the banner keeps its size.
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = actionLabel,
                    style = AppThemeWrapper.typography.callout,
                    color = content,
                    modifier = Modifier
                        .alpha(if (isActionLoading) 0f else 1f)
                        .clip(AppThemeWrapper.shapes.small)
                        .clickable(
                            enabled = !isActionLoading,
                            role = Role.Button,
                            onClick = onAction,
                        )
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                )

                if (isActionLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = content,
                        strokeWidth = 2.dp,
                    )
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun BannerPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Banner(
                message = "You're offline — showing saved data",
                icon = AppIcons.WifiOff,
                actionLabel = "Retry",
            )

            Banner(message = "Lagos Sound bought Jonas Vermeulen", tone = BannerTone.Success)

            Banner(message = "Budget is $412,000 short", tone = BannerTone.Warning)

            Banner(
                title = "Wrong email or password",
                message = "Check both fields and try again.",
                tone = BannerTone.Error,
            )
        }
    }
}
