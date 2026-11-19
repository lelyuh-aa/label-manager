package com.leliukh.labelmanager.features.artist.presentation.screen.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.design_system.presentation.components.button.AppButton
import com.leliukh.labelmanager.core.design_system.presentation.components.button.AppButtonStyle
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.Banner
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.BannerTone
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.EmptyState
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.SkeletonBlock
import com.leliukh.labelmanager.core.design_system.presentation.components.list.SectionHeader
import com.leliukh.labelmanager.core.design_system.presentation.components.money.DeltaLabel
import com.leliukh.labelmanager.core.design_system.presentation.components.money.MoneyText
import com.leliukh.labelmanager.core.design_system.presentation.components.navigation.AppTopBar
import com.leliukh.labelmanager.core.design_system.presentation.components.artist.ArtistAvatar
import com.leliukh.labelmanager.core.design_system.presentation.components.artist.GenreChip
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppTheme
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper
import com.leliukh.labelmanager.core.formatting.presentation.formatDate
import com.leliukh.labelmanager.core.formatting.presentation.formatNumericDate
import com.leliukh.labelmanager.core.formatting.presentation.formatMoney
import com.leliukh.labelmanager.core.formatting.presentation.relativeTime
import com.leliukh.labelmanager.core.ui_utils.presentation.CollectEvents
import com.leliukh.labelmanager.features.market.presentation.navigation.DealDoneRoute
import com.leliukh.labelmanager.features.market.presentation.screen.market.BuyConfirmSheet
import com.leliukh.labelmanager.features.artist.presentation.models.HistoryEntryVO
import com.leliukh.labelmanager.features.artist.presentation.models.ListingVO
import com.leliukh.labelmanager.features.artist.presentation.models.ArtistProfileVO
import com.leliukh.labelmanager.features.artist.presentation.models.PriceComparisonVO
import com.leliukh.labelmanager.features.artist.presentation.viewmodel.details.ArtistViewAction
import com.leliukh.labelmanager.features.artist.presentation.viewmodel.details.ArtistViewEvent
import com.leliukh.labelmanager.features.artist.presentation.viewmodel.details.ArtistViewModel
import com.leliukh.labelmanager.features.artist.presentation.viewmodel.details.ArtistViewState
import com.leliukh.labelmanager.features.label.domain.models.Genre
import com.leliukh.labelmanager.features.label.presentation.models.colors
import com.leliukh.labelmanager.features.label.presentation.models.nameRes
import com.leliukh.labelmanager.features.label.presentation.models.shortLabelRes
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.time.Instant

private val AvatarSize = 84.dp

/**
 * Artist profile: name, nationality and genre, the market value with age, hits and label,
 * the open listing and the deal history. Own artists can be edited, listed and withdrawn;
 * another label's listed artist can be bought.
 *
 * @param artistId id of the artist
 * @param onBack called by the back button
 * @param onEdit called with the artist's id by Edit
 * @param onCreateListing called with the artist's id by Put on market
 * @param onDealDone called after a purchase with what the deal-done moment shows
 * @param onSellArtist opens the sell flow, offered when the budget is short
 */
@Composable
fun ArtistScreen(
    artistId: String,
    onBack: () -> Unit,
    onEdit: (artistId: String) -> Unit,
    onCreateListing: (artistId: String) -> Unit,
    onDealDone: (DealDoneRoute) -> Unit,
    onSellArtist: () -> Unit,
) {
    val viewModel = hiltViewModel<ArtistViewModel, ArtistViewModel.Factory>(
        creationCallback = { factory -> factory.create(artistId) },
    )
    val state by viewModel.viewState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(viewModel) {
        viewModel.handleUiAction(ArtistViewAction.ScreenShown)
        onPauseOrDispose {}
    }

    CollectEvents(viewModel.uiEventsFlow) { event ->
        when (event) {
            is ArtistViewEvent.OpenEdit -> onEdit(event.artistId)

            is ArtistViewEvent.OpenCreateListing -> onCreateListing(event.artistId)

            is ArtistViewEvent.OpenDealDone -> onDealDone(event.route)

            ArtistViewEvent.OpenSellArtist -> onSellArtist()

            ArtistViewEvent.Close -> onBack()
        }
    }

    ArtistContent(state = state, onAction = viewModel::handleUiAction)
}

@Composable
private fun ArtistContent(
    state: ArtistViewState,
    onAction: (ArtistViewAction) -> Unit,
    now: Instant = Clock.System.now(),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppThemeWrapper.colors.background)
            .statusBarsPadding(),
    ) {
        AppTopBar(
            onBack = { onAction(ArtistViewAction.Back) },
            actions = {
                if (state.profile?.isMine == true) {
                    EditButton(onClick = { onAction(ArtistViewAction.EditClicked) })
                }
            },
        )

        Box(modifier = Modifier.weight(1f)) {
            when {
                state.profile != null -> Profile(profile = state.profile, now = now)

                state.isError -> EmptyState(
                    icon = AppIcons.WifiOff,
                    title = stringResource(R.string.artist_error_title),
                    message = stringResource(R.string.artist_error_message),
                    actionLabel = stringResource(R.string.common_retry),
                    onAction = { onAction(ArtistViewAction.Retry) },
                    modifier = Modifier.padding(top = 120.dp),
                )

                else -> ProfileSkeleton()
            }
        }

        state.profile?.let { profile ->
            Actions(state = state, profile = profile, onAction = onAction)
        }
    }

    state.buyConfirm?.let { confirm ->
        BuyConfirmSheet(
            confirm = confirm,
            onConfirm = { onAction(ArtistViewAction.ConfirmPurchase) },
            onSell = { onAction(ArtistViewAction.SellClicked) },
            onDismiss = { onAction(ArtistViewAction.DismissBuy) },
        )
    }
}

@Composable
private fun Actions(
    state: ArtistViewState,
    profile: ArtistProfileVO,
    onAction: (ArtistViewAction) -> Unit,
) {
    val listing = profile.listing
    if (!profile.isMine && listing == null) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = AppThemeWrapper.dimens.space4)
            .padding(top = 8.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (state.errorRes != null) {
            Banner(message = stringResource(state.errorRes), tone = BannerTone.Error)
        }

        when {
            profile.isMine && listing == null -> AppButton(
                text = stringResource(R.string.artist_put_on_list),
                onClick = { onAction(ArtistViewAction.PutOnListClicked) },
                icon = AppIcons.Tag,
            )

            profile.isMine -> AppButton(
                text = stringResource(R.string.artist_remove_from_list),
                onClick = { onAction(ArtistViewAction.RemoveFromListClicked) },
                style = AppButtonStyle.Destructive,
                isLoading = state.isWorking,
            )

            listing != null -> {
                AppButton(
                    text = stringResource(
                        R.string.artist_buy_for,
                        formatMoney(listing.askingPrice),
                    ),
                    onClick = { onAction(ArtistViewAction.BuyClicked) },
                    enabled = state.budget != null,
                )

                state.budget?.let { budget ->
                    Text(
                        text = stringResource(
                            R.string.artist_budget_after,
                            formatMoney(budget - listing.askingPrice),
                        ),
                        style = AppThemeWrapper.typography.subhead,
                        color = AppThemeWrapper.colors.textSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun Profile(profile: ArtistProfileVO, now: Instant) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
    ) {
        Header(profile = profile)

        ValueCard(profile = profile)

        if (profile.listing != null) {
            ListingCard(listing = profile.listing, isMine = profile.isMine, now = now)
        }

        SectionHeader(
            title = stringResource(R.string.artist_history_title),
            count = profile.history.size.toString(),
            modifier = Modifier.padding(top = 22.dp, bottom = 10.dp),
        )

        History(entries = profile.history)
    }
}

@Composable
private fun Header(profile: ArtistProfileVO) {
    val colors = AppThemeWrapper.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        ArtistAvatar(
            initials = profile.initials,
            badge = stringResource(profile.genre.shortLabelRes),
            colors = profile.genre.colors,
            size = AvatarSize,
            badgeBorder = colors.background,
        )

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = profile.firstName,
                style = AppThemeWrapper.typography.headline,
                color = colors.textSecondary,
            )

            Text(
                text = profile.lastName,
                style = AppThemeWrapper.typography.largeTitle,
                color = colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                modifier = Modifier.padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "${profile.flag} ${profile.countryName}",
                    style = AppThemeWrapper.typography.callout,
                    color = colors.textSecondary,
                )

                GenreChip(
                    text = stringResource(profile.genre.nameRes),
                    colors = profile.genre.colors,
                )
            }
        }
    }
}

@Composable
private fun ValueCard(profile: ArtistProfileVO) {
    val colors = AppThemeWrapper.colors

    Column(
        modifier = Modifier
            .padding(horizontal = AppThemeWrapper.dimens.space4)
            .padding(top = 20.dp)
            .fillMaxWidth()
            .background(colors.surface, AppThemeWrapper.shapes.card)
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.artist_market_value),
                style = AppThemeWrapper.typography.subheadMedium,
                color = colors.textSecondary,
                modifier = Modifier.weight(1f),
            )

            if (profile.valueRise != null) {
                RiseBadge(text = stringResource(R.string.artist_value_rise, profile.valueRise))
            }
        }

        MoneyText(amount = profile.marketValue, style = AppThemeWrapper.typography.moneyHero)

        HorizontalDivider(
            modifier = Modifier.padding(top = 10.dp),
            thickness = 1.dp,
            color = colors.separator,
        )

        Row(
            modifier = Modifier
                .padding(top = 6.dp)
                .height(IntrinsicSize.Min),
        ) {
            Fact(
                label = stringResource(R.string.artist_age_label),
                value = profile.age.toString(),
            ) {
                Text(
                    text = formatNumericDate(profile.birthDate),
                    style = AppThemeWrapper.typography.caption,
                    color = colors.textSecondary,
                )
            }

            FactDivider()

            Fact(label = stringResource(R.string.artist_hits), value = profile.hits.toString())

            FactDivider()

            Fact(
                label = stringResource(R.string.artist_label),
                value = "${profile.labelFlag} ${profile.labelName}",
                weight = 1.5f,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun RowScope.Fact(
    label: String,
    value: String,
    weight: Float = 1f,
    maxLines: Int = 1,
    extra: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = AppThemeWrapper.typography.caption,
            color = AppThemeWrapper.colors.textSecondary,
        )

        Text(
            text = value,
            style = AppThemeWrapper.typography.bodySemibold,
            color = AppThemeWrapper.colors.textPrimary,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )

        extra?.invoke()
    }
}

@Composable
private fun FactDivider() {
    VerticalDivider(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 8.dp),
        thickness = 1.dp,
        color = AppThemeWrapper.colors.separator,
    )
}

@Composable
private fun ListingCard(listing: ListingVO, isMine: Boolean, now: Instant) {
    val colors = AppThemeWrapper.colors

    Row(
        modifier = Modifier
            .padding(horizontal = AppThemeWrapper.dimens.space4)
            .padding(top = 12.dp)
            .fillMaxWidth()
            .background(colors.accentSoft, AppThemeWrapper.shapes.card)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(colors.surface, AppThemeWrapper.shapes.small),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.Tag,
                contentDescription = null,
                tint = colors.accentText,
                modifier = Modifier.size(20.dp),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(
                    if (isMine) {
                        R.string.artist_listed_title
                    } else {
                        R.string.artist_market_listing_title
                    },
                    relativeTime(listing.listedAt, now),
                ),
                style = AppThemeWrapper.typography.subheadMedium,
                color = colors.accentText,
            )

            Text(
                text = stringResource(
                    R.string.artist_listed_price,
                    formatMoney(listing.askingPrice),
                ),
                style = AppThemeWrapper.typography.moneyLarge,
                color = colors.textPrimary,
            )

            Text(
                text = priceComparison(listing.priceVsValue),
                style = AppThemeWrapper.typography.subhead,
                color = colors.textSecondary,
            )
        }
    }
}

@Composable
private fun History(entries: List<HistoryEntryVO>) {
    Column(
        modifier = Modifier
            .padding(horizontal = AppThemeWrapper.dimens.space4)
            .fillMaxWidth()
            .background(AppThemeWrapper.colors.surface, AppThemeWrapper.shapes.card)
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        entries.forEachIndexed { index, entry ->
            if (index > 0) {
                HorizontalDivider(thickness = 1.dp, color = AppThemeWrapper.colors.separator)
            }

            HistoryRow(entry = entry, isLatest = index == 0)
        }
    }
}

@Composable
private fun HistoryRow(entry: HistoryEntryVO, isLatest: Boolean) {
    val colors = AppThemeWrapper.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(18.dp)
                .background(if (isLatest) colors.accentSoft else colors.fill, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(if (isLatest) colors.accent else colors.textSecondary, CircleShape),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            when (entry) {
                is HistoryEntryVO.Move -> MoveDetails(entry = entry)

                is HistoryEntryVO.Founding -> FoundingDetails(entry = entry)
            }
        }
    }
}

@Composable
private fun MoveDetails(entry: HistoryEntryVO.Move) {
    val colors = AppThemeWrapper.colors

    Text(
        text = formatDate(entry.signedAt),
        style = AppThemeWrapper.typography.caption,
        color = colors.textSecondary,
    )

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        LabelName(flag = entry.fromFlag, name = entry.fromName)

        Icon(
            imageVector = AppIcons.ArrowDown,
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier
                .padding(start = 4.dp)
                .size(14.dp),
        )

        LabelName(flag = entry.toFlag, name = entry.toName)
    }

    Text(
        text = stringResource(R.string.artist_history_fee, formatMoney(entry.price)),
        style = AppThemeWrapper.typography.subhead,
        color = colors.textSecondary,
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(
                R.string.artist_history_value,
                formatMoney(entry.valueBefore),
                formatMoney(entry.valueAfter),
            ),
            style = AppThemeWrapper.typography.subhead,
            color = colors.textSecondary,
            modifier = Modifier.weight(1f, false),
        )

        DeltaLabel(text = entry.rise, isIncrease = true)
    }
}

@Composable
private fun FoundingDetails(entry: HistoryEntryVO.Founding) {
    LabelName(flag = entry.labelFlag, name = entry.labelName)

    Text(
        text = stringResource(R.string.artist_history_founding, formatMoney(entry.value)),
        style = AppThemeWrapper.typography.subhead,
        color = AppThemeWrapper.colors.textSecondary,
    )
}

@Composable
private fun LabelName(flag: String, name: String, modifier: Modifier = Modifier) {
    Text(
        text = "$flag $name",
        style = AppThemeWrapper.typography.bodyMedium,
        color = AppThemeWrapper.colors.textPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

@Composable
private fun RiseBadge(text: String) {
    val colors = AppThemeWrapper.colors

    Row(
        modifier = Modifier
            .height(24.dp)
            .background(colors.positiveSoft, AppThemeWrapper.shapes.pill)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(
            imageVector = AppIcons.TrendingUp,
            contentDescription = null,
            tint = colors.positive,
            modifier = Modifier.size(13.dp),
        )

        Text(text = text, style = AppThemeWrapper.typography.caption, color = colors.positive)
    }
}

@Composable
private fun EditButton(onClick: () -> Unit) {
    val colors = AppThemeWrapper.colors

    Row(
        modifier = Modifier
            .height(44.dp)
            .clip(AppThemeWrapper.shapes.pill)
            .background(colors.surface)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = AppIcons.Pencil,
            contentDescription = null,
            tint = colors.accentText,
            modifier = Modifier.size(16.dp),
        )

        Text(
            text = stringResource(R.string.artist_edit),
            style = AppThemeWrapper.typography.bodySemibold,
            color = colors.accentText,
        )
    }
}

@Composable
private fun ProfileSkeleton() {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            SkeletonBlock(width = AvatarSize, height = AvatarSize, shape = CircleShape)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SkeletonBlock(width = 90.dp, height = 16.dp)

                SkeletonBlock(width = 180.dp, height = 28.dp)

                SkeletonBlock(width = 140.dp, height = 14.dp)
            }
        }

        SkeletonBlock(
            width = 372.dp,
            height = 170.dp,
            shape = AppThemeWrapper.shapes.card,
            modifier = Modifier.fillMaxWidth(),
        )

        SkeletonBlock(width = 150.dp, height = 18.dp)

        SkeletonBlock(
            width = 372.dp,
            height = 140.dp,
            shape = AppThemeWrapper.shapes.card,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}


@Composable
private fun priceComparison(comparison: PriceComparisonVO): String = when {
    comparison.isEqual -> stringResource(R.string.artist_price_at_value)

    comparison.isAbove -> stringResource(R.string.artist_price_above_value, comparison.percent)

    else -> stringResource(R.string.artist_price_below_value, comparison.percent)
}

@Preview(name = "Artist", widthDp = 412, heightDp = 917)
@Composable
private fun ArtistPreview() {
    AppTheme(darkTheme = false) {
        ArtistContent(
            state = ArtistViewState(isLoading = false, profile = previewProfile),
            onAction = {},
        )
    }
}

@Preview(name = "Artist, listed, dark", widthDp = 412, heightDp = 917)
@Composable
private fun ArtistListedPreview() {
    AppTheme(darkTheme = true) {
        ArtistContent(
            state = ArtistViewState(
                isLoading = false,
                profile = previewProfile.copy(
                    listing = ListingVO(
                        listingId = "l1",
                        askingPrice = 2_750_000,
                        listedAt = Instant.parse("2026-10-25T10:00:00Z"),
                        priceVsValue = PriceComparisonVO("60%", isAbove = true, isEqual = false),
                    ),
                ),
            ),
            onAction = {},
            now = previewNow,
        )
    }
}

@Preview(name = "Artist, other label", widthDp = 412, heightDp = 917)
@Composable
private fun ArtistOtherLabelPreview() {
    AppTheme(darkTheme = false) {
        ArtistContent(
            state = ArtistViewState(
                isLoading = false,
                profile = previewProfile.copy(
                    isMine = false,
                    labelName = "Lagos Sound",
                    labelFlag = "🇳🇬",
                ),
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Artist, loading", widthDp = 412, heightDp = 917)
@Composable
private fun ArtistLoadingPreview() {
    AppTheme(darkTheme = false) {
        ArtistContent(state = ArtistViewState(), onAction = {})
    }
}

@Preview(name = "Artist, error", widthDp = 412, heightDp = 917)
@Composable
private fun ArtistErrorPreview() {
    AppTheme(darkTheme = false) {
        ArtistContent(state = ArtistViewState(isLoading = false, isError = true), onAction = {})
    }
}

private val previewNow = Instant.parse("2026-10-25T12:00:00Z")

private val previewProfile = ArtistProfileVO(
    artistId = "p1",
    firstName = "Théo",
    lastName = "Marchand",
    initials = "TM",
    genre = Genre.Rock,
    flag = "🇫🇷",
    countryName = "France",
    age = 22,
    birthDate = LocalDate(2004, 3, 14),
    hits = 7,
    marketValue = 1_450_000,
    valueRise = "45%",
    labelName = "Rhein Valley Records",
    labelFlag = "🇩🇪",
    isMine = true,
    listing = null,
    history = listOf(
        HistoryEntryVO.Move(
            signedAt = Instant.parse("2026-10-18T10:00:00Z"),
            fromName = "Garonne Records",
            fromFlag = "🇫🇷",
            toName = "Rhein Valley Records",
            toFlag = "🇩🇪",
            price = 1_000_000,
            valueBefore = 1_000_000,
            valueAfter = 1_450_000,
            rise = "45%",
        ),
        HistoryEntryVO.Founding(labelName = "Garonne Records", labelFlag = "🇫🇷", value = 1_000_000),
    ),
)
