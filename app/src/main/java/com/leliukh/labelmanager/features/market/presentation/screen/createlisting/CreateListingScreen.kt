package com.leliukh.labelmanager.features.market.presentation.screen.createlisting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.design_system.presentation.components.button.AppButton
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.Banner
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.BannerTone
import com.leliukh.labelmanager.core.design_system.presentation.components.field.AppTextField
import com.leliukh.labelmanager.core.design_system.presentation.components.money.MoneyText
import com.leliukh.labelmanager.core.design_system.presentation.components.navigation.AppTopBar
import com.leliukh.labelmanager.core.design_system.presentation.components.artist.ArtistAvatar
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppTheme
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper
import com.leliukh.labelmanager.core.formatting.presentation.formatNumericDate
import com.leliukh.labelmanager.core.formatting.presentation.formatMoney
import com.leliukh.labelmanager.core.formatting.presentation.formatMoneyCompact
import com.leliukh.labelmanager.core.formatting.presentation.formatPercentChange
import com.leliukh.labelmanager.core.ui_utils.presentation.CollectEvents
import com.leliukh.labelmanager.features.market.presentation.viewmodel.createlisting.CreateListingViewAction
import com.leliukh.labelmanager.features.market.presentation.viewmodel.createlisting.CreateListingViewEvent
import com.leliukh.labelmanager.features.market.presentation.viewmodel.createlisting.CreateListingViewModel
import com.leliukh.labelmanager.features.market.presentation.viewmodel.createlisting.CreateListingViewState
import com.leliukh.labelmanager.features.market.presentation.viewmodel.createlisting.QuickPriceVO
import com.leliukh.labelmanager.features.label.domain.models.Genre
import com.leliukh.labelmanager.features.label.presentation.models.colors
import com.leliukh.labelmanager.features.label.presentation.models.shortLabelRes
import kotlinx.datetime.LocalDate
import kotlin.math.abs

/**
 * Sell an artist, step two: the artist, the market value, the asking price with quick
 * markups, and how the price compares with the value.
 *
 * @param artistId id of the artist
 * @param onClose called with whether the artist was listed
 */
@Composable
fun CreateListingScreen(
    artistId: String,
    onClose: (isListed: Boolean) -> Unit,
) {
    val viewModel = hiltViewModel<CreateListingViewModel, CreateListingViewModel.Factory>(
        creationCallback = { factory -> factory.create(artistId) },
    )
    val state by viewModel.viewState.collectAsStateWithLifecycle()

    CollectEvents(viewModel.uiEventsFlow) { event ->
        when (event) {
            is CreateListingViewEvent.Close -> onClose(event.isListed)
        }
    }

    CreateListingContent(state = state, onAction = viewModel::handleUiAction)
}

@Composable
private fun CreateListingContent(
    state: CreateListingViewState,
    onAction: (CreateListingViewAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppThemeWrapper.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        AppTopBar(
            onBack = { onAction(CreateListingViewAction.Cancel) },
            title = stringResource(R.string.create_listing_title),
            backIcon = AppIcons.Close,
        )

        if (!state.isLoading) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                ArtistSummary(state = state)

                ValueCard(marketValue = state.marketValue)

                AppTextField(
                    value = state.price?.let(::formatMoney).orEmpty(),
                    onValueChange = { onAction(CreateListingViewAction.PriceTyped(it)) },
                    label = stringResource(R.string.create_listing_price_label),
                    placeholder = formatMoney(state.marketValue),
                    error = if (state.price != null && !state.isPriceValid) {
                        stringResource(R.string.create_listing_price_invalid)
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                )

                QuickPrices(state = state, onAction = onAction)

                PriceHint(state = state)

                if (state.errorRes != null) {
                    Banner(message = stringResource(state.errorRes), tone = BannerTone.Error)
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AppButton(
                    text = if (state.price != null && state.isPriceValid) {
                        stringResource(R.string.create_listing_submit, formatMoney(state.price))
                    } else {
                        stringResource(R.string.create_listing_submit_empty)
                    },
                    onClick = { onAction(CreateListingViewAction.Submit) },
                    enabled = state.isPriceValid,
                    isLoading = state.isSubmitting,
                    icon = AppIcons.Tag,
                )

                Text(
                    text = stringResource(R.string.create_listing_footer),
                    style = AppThemeWrapper.typography.caption,
                    color = AppThemeWrapper.colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun ArtistSummary(state: CreateListingViewState) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ArtistAvatar(
            initials = state.initials,
            badge = stringResource(state.genre.shortLabelRes),
            colors = state.genre.colors,
            size = 56.dp,
            badgeBorder = AppThemeWrapper.colors.background,
        )

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = state.fullName,
                style = AppThemeWrapper.typography.title,
                color = AppThemeWrapper.colors.textPrimary,
            )

            Text(
                text = "${state.flag} ${state.countryName} · " +
                    pluralStringResource(R.plurals.artist_age, state.age, state.age) + " · " +
                    formatNumericDate(state.birthDate),
                style = AppThemeWrapper.typography.subhead,
                color = AppThemeWrapper.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun ValueCard(marketValue: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppThemeWrapper.colors.surface, AppThemeWrapper.shapes.card)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = AppIcons.TrendingUp,
            contentDescription = null,
            tint = AppThemeWrapper.colors.textSecondary,
            modifier = Modifier.size(18.dp),
        )

        Text(
            text = stringResource(R.string.artist_market_value),
            style = AppThemeWrapper.typography.callout,
            color = AppThemeWrapper.colors.textSecondary,
            modifier = Modifier.weight(1f),
        )

        MoneyText(amount = marketValue, style = AppThemeWrapper.typography.moneyLarge)
    }
}

@Composable
private fun QuickPrices(
    state: CreateListingViewState,
    onAction: (CreateListingViewAction) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        state.quickPrices.forEach { quick ->
            QuickPriceChip(
                quick = quick,
                isSelected = quick.price == state.price,
                onClick = { onAction(CreateListingViewAction.QuickPriceChosen(quick.price)) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun QuickPriceChip(
    quick: QuickPriceVO,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeWrapper.colors

    Column(
        modifier = modifier
            .clip(AppThemeWrapper.shapes.control)
            .background(if (isSelected) colors.accentSoft else colors.surface)
            .border(
                width = 1.5.dp,
                color = if (isSelected) colors.accent else colors.surface,
                shape = AppThemeWrapper.shapes.control,
            )
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics { selected = isSelected }
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = if (quick.markupPercent == 0) {
                stringResource(R.string.create_listing_quick_value)
            } else {
                "+${quick.markupPercent}%"
            },
            style = AppThemeWrapper.typography.caption,
            color = if (isSelected) colors.accentText else colors.textSecondary,
        )

        Text(
            text = formatMoneyCompact(quick.price),
            style = AppThemeWrapper.typography.bodySemibold,
            color = if (isSelected) colors.accentText else colors.textPrimary,
        )
    }
}

@Composable
private fun PriceHint(state: CreateListingViewState) {
    val price = state.price ?: return
    val difference = state.difference
    val text = when {
        difference > 0 -> stringResource(
            R.string.create_listing_above,
            formatMoney(difference),
            formatPercentChange(state.marketValue, price),
        )

        difference < 0 -> stringResource(
            R.string.create_listing_below,
            formatMoney(abs(difference)),
            formatPercentChange(state.marketValue, price),
        )

        else -> stringResource(R.string.create_listing_at_value)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppThemeWrapper.colors.fill, AppThemeWrapper.shapes.control)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = AppIcons.Info,
            contentDescription = null,
            tint = AppThemeWrapper.colors.textSecondary,
            modifier = Modifier.size(18.dp),
        )

        Text(
            text = text + " " + stringResource(R.string.create_listing_buyer_pays),
            style = AppThemeWrapper.typography.subhead,
            color = AppThemeWrapper.colors.textSecondary,
        )
    }
}

@Preview(name = "Create listing", widthDp = 412, heightDp = 917)
@Composable
private fun CreateListingPreview() {
    AppTheme(darkTheme = false) {
        CreateListingContent(state = previewState.withPrice(1_725_000), onAction = {})
    }
}

@Preview(name = "Create listing, below value, dark", widthDp = 412, heightDp = 917)
@Composable
private fun CreateListingBelowPreview() {
    AppTheme(darkTheme = true) {
        CreateListingContent(state = previewState.withPrice(1_100_000), onAction = {})
    }
}

@Preview(name = "Create listing, submitting", widthDp = 412, heightDp = 917)
@Composable
private fun CreateListingSubmittingPreview() {
    AppTheme(darkTheme = false) {
        CreateListingContent(
            state = previewState.withPrice(1_300_000).copy(isSubmitting = true),
            onAction = {},
        )
    }
}

private val previewState = CreateListingViewState(
    isLoading = false,
    fullName = "Matteo Ricci",
    initials = "MR",
    genre = Genre.Pop,
    flag = "🇮🇹",
    countryName = "Italy",
    age = 31,
    birthDate = LocalDate(1995, 2, 11),
    marketValue = 1_300_000,
    quickPrices = listOf(0, 10, 25, 50).map { QuickPriceVO(it, 1_300_000L * (100 + it) / 100) },
)
