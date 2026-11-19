package com.leliukh.labelmanager.core.design_system.presentation.components.feedback

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.design_system.presentation.components.button.AppButton
import com.leliukh.labelmanager.core.design_system.presentation.components.button.AppButtonStyle
import com.leliukh.labelmanager.core.design_system.presentation.components.list.GroupPosition
import com.leliukh.labelmanager.core.design_system.presentation.components.list.GroupedItem
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.preview.PreviewSurface
import com.leliukh.labelmanager.core.design_system.presentation.preview.ThemePreviews
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper

private const val PULSE_DURATION_MS = 900
private const val PULSE_MIN_ALPHA = 0.45f

/**
 * A pulsing grey block standing in for content that is still loading.
 *
 * @param width width of the block
 * @param height height of the block
 * @param modifier modifier of the block
 * @param shape shape of the block
 */
@Composable
fun SkeletonBlock(
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(6.dp),
) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = PULSE_MIN_ALPHA,
        animationSpec = infiniteRepeatable(
            animation = tween(PULSE_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "skeletonAlpha",
    )

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .alpha(alpha)
            .background(AppThemeWrapper.colors.fill, shape),
    )
}

/**
 * Loading placeholder shaped like an artist row inside an inset group.
 *
 * @param position where the row sits in its group
 * @param modifier modifier of the row
 */
@Composable
fun SkeletonArtistRow(position: GroupPosition, modifier: Modifier = Modifier) {
    val loading = stringResource(R.string.common_loading)

    GroupedItem(position = position, modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .semantics { contentDescription = loading },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SkeletonBlock(width = 42.dp, height = 42.dp, shape = CircleShape)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SkeletonBlock(width = 140.dp, height = 14.dp)

                SkeletonBlock(width = 96.dp, height = 11.dp)
            }

            SkeletonBlock(width = 70.dp, height = 14.dp)
        }
    }
}

/**
 * Centered illustration-free empty or error state with an optional action.
 *
 * @param icon icon above the title
 * @param title short title
 * @param message explanation below the title
 * @param modifier modifier of the block
 * @param actionLabel optional action label
 * @param onAction called by the action
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    val colors = AppThemeWrapper.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(colors.accentSoft, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.accentText,
                modifier = Modifier.size(28.dp),
            )
        }

        Text(
            text = title,
            style = AppThemeWrapper.typography.headline,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )

        Text(
            text = message,
            style = AppThemeWrapper.typography.body,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )

        if (actionLabel != null) {
            AppButton(
                text = actionLabel,
                onClick = onAction,
                style = AppButtonStyle.Secondary,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .width(220.dp),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun SkeletonPreview() {
    PreviewSurface {
        Column {
            SkeletonArtistRow(position = GroupPosition.First)

            SkeletonArtistRow(position = GroupPosition.Middle)

            SkeletonArtistRow(position = GroupPosition.Last)
        }
    }
}

@ThemePreviews
@Composable
private fun EmptyStatePreview() {
    PreviewSurface {
        EmptyState(
            icon = AppIcons.Search,
            title = "No artists match",
            message = "Try another name or remove a filter.",
            actionLabel = "Clear filters",
        )
    }
}
