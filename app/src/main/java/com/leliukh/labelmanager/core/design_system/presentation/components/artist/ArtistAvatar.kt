package com.leliukh.labelmanager.core.design_system.presentation.components.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leliukh.labelmanager.core.design_system.presentation.preview.PreviewSurface
import com.leliukh.labelmanager.core.design_system.presentation.preview.ThemePreviews
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper
import com.leliukh.labelmanager.core.design_system.presentation.theme.ColorPair

private val BadgeShape = RoundedCornerShape(6.dp)

/**
 * Round avatar with the artist's initials on their genre color and the genre code in
 * a badge at the bottom-right corner. Artists have no photos in this game.
 *
 * Decorative for accessibility: the row it sits in announces the name and genre.
 *
 * @param initials one or two letters
 * @param badge short genre code, e.g. "POP"
 * @param colors genre colors
 * @param modifier modifier of the avatar
 * @param size diameter of the avatar
 * @param badgeBorder color around the badge, the color of the surface the avatar sits on
 */
@Composable
fun ArtistAvatar(
    initials: String,
    badge: String,
    colors: ColorPair,
    modifier: Modifier = Modifier,
    size: Dp = AppThemeWrapper.dimens.avatar,
    badgeBorder: Color = AppThemeWrapper.colors.surface,
) {
    val scale = size / AppThemeWrapper.dimens.avatar

    Box(modifier = modifier.size(size).clearAndSetSemantics {}) {
        Box(
            modifier = Modifier
                .size(size)
                .background(colors.container, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initials,
                style = AppThemeWrapper.typography.callout.copy(
                    fontSize = (14 * scale).sp,
                    lineHeight = (18 * scale).sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = colors.content,
            )
        }

        Text(
            text = badge,
            style = AppThemeWrapper.typography.badge.copy(
                fontSize = (9 * scale.coerceAtMost(1.4f)).sp,
            ),
            color = badgeBorder,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 5.dp, y = 4.dp)
                .background(colors.content, BadgeShape)
                .border(2.dp, badgeBorder, BadgeShape)
                .padding(horizontal = 5.dp, vertical = 1.dp),
        )
    }
}

/**
 * The genre written out on its own color, e.g. "Rock".
 *
 * @param text the genre name
 * @param colors genre colors
 * @param modifier modifier of the chip
 */
@Composable
fun GenreChip(text: String, colors: ColorPair, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = AppThemeWrapper.typography.caption,
        color = colors.content,
        modifier = modifier
            .background(colors.container, RoundedCornerShape(percent = 50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@ThemePreviews
@Composable
private fun ArtistAvatarPreview() {
    val genres = AppThemeWrapper.colors.genres

    PreviewSurface {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArtistAvatar(initials = "LB", badge = "POP", colors = genres.pop)

            ArtistAvatar(initials = "TM", badge = "RCK", colors = genres.rock)

            ArtistAvatar(initials = "CO", badge = "HIP", colors = genres.hipHop)

            ArtistAvatar(
                initials = "AP",
                badge = "EDM",
                colors = genres.electronic,
                size = 72.dp,
                badgeBorder = AppThemeWrapper.colors.background,
            )
        }
    }
}

@ThemePreviews
@Composable
private fun GenreChipPreview() {
    val genres = AppThemeWrapper.colors.genres

    PreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GenreChip(text = "Pop", colors = genres.pop)

            GenreChip(text = "Rock", colors = genres.rock)

            GenreChip(text = "Hip-hop", colors = genres.hipHop)

            GenreChip(text = "Electronic", colors = genres.electronic)
        }
    }
}
