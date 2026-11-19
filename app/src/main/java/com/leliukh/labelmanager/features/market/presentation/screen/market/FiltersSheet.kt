package com.leliukh.labelmanager.features.market.presentation.screen.market

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.countries.presentation.CountryPickerSheet
import com.leliukh.labelmanager.core.design_system.presentation.components.button.AppButton
import com.leliukh.labelmanager.core.design_system.presentation.components.button.AppButtonStyle
import com.leliukh.labelmanager.core.design_system.presentation.components.chip.SelectableChip
import com.leliukh.labelmanager.core.design_system.presentation.components.field.SelectField
import com.leliukh.labelmanager.core.design_system.presentation.components.sheet.AppBottomSheet
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper
import com.leliukh.labelmanager.core.formatting.presentation.countryDisplayName
import com.leliukh.labelmanager.core.formatting.presentation.countryFlagEmoji
import com.leliukh.labelmanager.features.market.domain.models.ListingFilter
import com.leliukh.labelmanager.features.label.domain.models.Genre
import com.leliukh.labelmanager.features.label.presentation.models.titleRes

/** Upper end of the price slider; the slider at its end means "no maximum". */
private const val PRICE_SLIDER_MAX = 10_000_000L
private const val PRICE_SLIDER_STEP = 250_000L

private enum class CountryTarget { Artist, Label }

/**
 * Filter sheet of the market: price range, genres and the countries of the artist and the
 * label. Edits a draft; the main button shows how many artists the draft matches and applies it.
 *
 * @param draft the filters being edited
 * @param matchCount how many listings [draft] matches; null while counting
 * @param onChange called with every edit
 * @param onReset clears the draft
 * @param onApply applies the draft and closes the sheet
 * @param onDismiss closes the sheet without applying
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FiltersSheet(
    draft: ListingFilter,
    matchCount: Int?,
    onChange: (ListingFilter) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
) {
    var countryTarget by rememberSaveable { mutableStateOf<CountryTarget?>(null) }

    AppBottomSheet(onDismiss = onDismiss, title = stringResource(R.string.filters_title)) {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            PriceSection(draft = draft, onChange = onChange)

            FilterSection(title = stringResource(R.string.filters_genre)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Genre.entries.forEach { genre ->
                        val isSelected = genre in draft.genres
                        SelectableChip(
                            label = stringResource(genre.titleRes),
                            isSelected = isSelected,
                            onClick = {
                                val genres = if (isSelected) {
                                    draft.genres - genre
                                } else {
                                    draft.genres + genre
                                }
                                onChange(draft.copy(genres = genres))
                            },
                        )
                    }
                }
            }

            FilterSection(title = stringResource(R.string.filters_country)) {
                CountryField(
                    label = stringResource(R.string.filters_artist_country),
                    code = draft.artistCountry,
                    onClick = { countryTarget = CountryTarget.Artist },
                )

                CountryField(
                    label = stringResource(R.string.filters_label_country),
                    code = draft.labelCountry,
                    onClick = { countryTarget = CountryTarget.Label },
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppButton(
                    text = stringResource(R.string.filters_reset),
                    onClick = onReset,
                    style = AppButtonStyle.Secondary,
                    // As wide as its label, so a long translation is not wrapped.
                    modifier = Modifier.width(IntrinsicSize.Max),
                )

                AppButton(
                    text = if (matchCount == null) {
                        stringResource(R.string.filters_show)
                    } else {
                        stringResource(
                            R.string.filters_show_count,
                            pluralStringResource(
                                R.plurals.filters_artist_count,
                                matchCount,
                                matchCount,
                            ),
                        )
                    },
                    onClick = onApply,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    countryTarget?.let { target ->
        CountryPickerSheet(
            selectedCode = when (target) {
                CountryTarget.Artist -> draft.artistCountry

                CountryTarget.Label -> draft.labelCountry
            },
            onSelect = { code ->
                onChange(draft.withCountry(target, code))
                countryTarget = null
            },
            onSelectAny = {
                onChange(draft.withCountry(target, null))
                countryTarget = null
            },
            onDismiss = { countryTarget = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriceSection(draft: ListingFilter, onChange: (ListingFilter) -> Unit) {
    val colors = AppThemeWrapper.colors
    val start = (draft.minPrice ?: 0L).coerceIn(0L, PRICE_SLIDER_MAX).toFloat()
    val end = (draft.maxPrice ?: PRICE_SLIDER_MAX).coerceIn(0L, PRICE_SLIDER_MAX).toFloat()

    FilterSection(
        title = stringResource(R.string.filters_price),
        trailing = priceLabel(minPrice = draft.minPrice, maxPrice = draft.maxPrice)
            ?: stringResource(R.string.filters_price_any),
    ) {
        RangeSlider(
            value = start..end,
            onValueChange = { range ->
                val min = range.start.toLong().roundedToStep()
                val max = range.endInclusive.toLong().roundedToStep()
                onChange(
                    draft.copy(
                        minPrice = min.takeIf { it > 0 },
                        maxPrice = max.takeIf { it < PRICE_SLIDER_MAX },
                    ),
                )
            },
            valueRange = 0f..PRICE_SLIDER_MAX.toFloat(),
            steps = (PRICE_SLIDER_MAX / PRICE_SLIDER_STEP).toInt() - 1,
            colors = SliderDefaults.colors(
                thumbColor = colors.accent,
                activeTrackColor = colors.accent,
                inactiveTrackColor = colors.fill,
                activeTickColor = colors.accent.copy(alpha = 0f),
                inactiveTickColor = colors.fill.copy(alpha = 0f),
            ),
        )
    }
}

@Composable
private fun FilterSection(
    title: String,
    trailing: String? = null,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = AppThemeWrapper.typography.headline,
                color = AppThemeWrapper.colors.textPrimary,
                modifier = Modifier.weight(1f),
            )

            if (trailing != null) {
                Text(
                    text = trailing,
                    style = AppThemeWrapper.typography.callout,
                    color = AppThemeWrapper.colors.accentText,
                )
            }
        }

        content()
    }
}

@Composable
private fun CountryField(label: String, code: String?, onClick: () -> Unit) {
    SelectField(
        label = label,
        value = code?.let(::countryDisplayName) ?: stringResource(R.string.filters_any),
        emoji = code?.let(::countryFlagEmoji),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
    )
}

private fun ListingFilter.withCountry(target: CountryTarget, code: String?): ListingFilter =
    when (target) {
        CountryTarget.Artist -> copy(artistCountry = code)

        CountryTarget.Label -> copy(labelCountry = code)
    }

private fun Long.roundedToStep(): Long =
    (this + PRICE_SLIDER_STEP / 2) / PRICE_SLIDER_STEP * PRICE_SLIDER_STEP
