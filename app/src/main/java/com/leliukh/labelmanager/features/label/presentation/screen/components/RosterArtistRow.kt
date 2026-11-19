package com.leliukh.labelmanager.features.label.presentation.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.design_system.presentation.components.chip.StatusBadge
import com.leliukh.labelmanager.core.design_system.presentation.components.list.GroupPosition
import com.leliukh.labelmanager.core.design_system.presentation.components.list.GroupedItem
import com.leliukh.labelmanager.core.design_system.presentation.components.list.ListRow
import com.leliukh.labelmanager.core.design_system.presentation.components.money.DeltaLabel
import com.leliukh.labelmanager.core.design_system.presentation.components.money.MoneyText
import com.leliukh.labelmanager.core.design_system.presentation.components.artist.ArtistAvatar
import com.leliukh.labelmanager.core.design_system.presentation.preview.PreviewSurface
import com.leliukh.labelmanager.core.design_system.presentation.preview.ThemePreviews
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper
import com.leliukh.labelmanager.core.formatting.presentation.formatMoneyCompact
import com.leliukh.labelmanager.features.label.domain.models.Genre
import com.leliukh.labelmanager.features.label.presentation.models.RosterArtistVO
import com.leliukh.labelmanager.features.label.presentation.models.colors
import com.leliukh.labelmanager.features.label.presentation.models.shortLabelRes

private const val DISABLED_ALPHA = 0.5f

/**
 * An artist of the user's roster in a grouped list: avatar with the genre, name, nationality
 * and age, then the market value with its growth and the asking price while for sale.
 *
 * @param artist the artist
 * @param position where the row sits in its group
 * @param onClick called on tap
 * @param modifier modifier of the row
 * @param enabled whether the row reacts to taps; a disabled row is dimmed
 */
@Composable
fun RosterArtistRow(
    artist: RosterArtistVO,
    position: GroupPosition,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = AppThemeWrapper.colors

    GroupedItem(
        position = position,
        onClick = if (enabled) onClick else null,
        modifier = modifier,
    ) {
        ListRow(
            modifier = Modifier.alpha(if (enabled) 1f else DISABLED_ALPHA),
            title = artist.fullName,
            leading = {
                ArtistAvatar(
                    initials = artist.initials,
                    badge = stringResource(artist.genre.shortLabelRes),
                    colors = artist.genre.colors,
                )
            },
            subtitle = {
                Text(
                    text = stringResource(
                        R.string.roster_artist_meta,
                        artist.flag,
                        artist.countryName,
                        pluralStringResource(R.plurals.artist_age, artist.age, artist.age),
                    ),
                    style = AppThemeWrapper.typography.subhead,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            trailing = {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    MoneyText(amount = artist.marketValue)

                    if (artist.valueRise != null) {
                        DeltaLabel(text = artist.valueRise, isIncrease = true)
                    }

                    if (artist.askingPrice != null) {
                        StatusBadge(
                            text = stringResource(
                                R.string.roster_for_sale,
                                formatMoneyCompact(artist.askingPrice),
                            ),
                        )
                    }
                }
            },
            showChevron = enabled,
        )
    }
}

@ThemePreviews
@Composable
private fun RosterArtistRowPreview() {
    PreviewSurface {
        Column {
            previewArtists.forEachIndexed { index, artist ->
                RosterArtistRow(
                    artist = artist,
                    position = GroupPosition.of(index, previewArtists.size),
                    onClick = {},
                )
            }
        }
    }
}

private val previewArtists = listOf(
    RosterArtistVO(
        id = "1",
        fullName = "Lukas Brenner",
        initials = "LB",
        genre = Genre.Pop,
        flag = "🇩🇪",
        countryName = "Germany",
        age = 27,
        marketValue = 1_000_000,
        valueRise = null,
        askingPrice = null,
    ),
    RosterArtistVO(
        id = "2",
        fullName = "Matteo Ricci",
        initials = "MR",
        genre = Genre.Pop,
        flag = "🇮🇹",
        countryName = "Italy",
        age = 31,
        marketValue = 1_300_000,
        valueRise = "30%",
        askingPrice = null,
    ),
    RosterArtistVO(
        id = "3",
        fullName = "Jonas Vermeulen",
        initials = "JV",
        genre = Genre.Pop,
        flag = "🇧🇪",
        countryName = "Belgium",
        age = 19,
        marketValue = 1_000_000,
        valueRise = null,
        askingPrice = 1_600_000,
    ),
)
