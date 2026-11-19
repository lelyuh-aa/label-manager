package com.leliukh.labelmanager.features.label.presentation.screen.roster

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.design_system.presentation.components.button.CircleIconButton
import com.leliukh.labelmanager.core.design_system.presentation.components.button.CircleIconButtonStyle
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.Banner
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.BannerTone
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.EmptyState
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.SkeletonBlock
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.SkeletonArtistRow
import com.leliukh.labelmanager.core.design_system.presentation.components.field.SearchField
import com.leliukh.labelmanager.core.design_system.presentation.components.list.GroupPosition
import com.leliukh.labelmanager.core.design_system.presentation.components.list.SectionHeader
import com.leliukh.labelmanager.core.design_system.presentation.components.list.StatTile
import com.leliukh.labelmanager.core.design_system.presentation.components.money.MoneyText
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppTheme
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper
import com.leliukh.labelmanager.core.design_system.presentation.theme.ColorPair
import com.leliukh.labelmanager.core.formatting.presentation.formatRecentMoment
import com.leliukh.labelmanager.core.ui_utils.presentation.CollectEvents
import com.leliukh.labelmanager.features.home.presentation.screen.HomeScreen
import com.leliukh.labelmanager.features.label.domain.models.Genre
import com.leliukh.labelmanager.features.label.presentation.models.RosterArtistVO
import com.leliukh.labelmanager.features.label.presentation.models.RosterSectionVO
import com.leliukh.labelmanager.features.label.presentation.models.StaleDataVO
import com.leliukh.labelmanager.features.label.presentation.models.titleRes
import com.leliukh.labelmanager.features.label.presentation.screen.components.RosterArtistRow
import com.leliukh.labelmanager.features.label.presentation.viewmodel.roster.RosterViewAction
import com.leliukh.labelmanager.features.label.presentation.viewmodel.roster.RosterViewEvent
import com.leliukh.labelmanager.features.label.presentation.viewmodel.roster.RosterViewModel
import com.leliukh.labelmanager.features.label.presentation.viewmodel.roster.RosterViewState
import kotlin.time.Instant

private val SkeletonSections = listOf(3, 4)

/**
 * The Roster tab: the label's name, budget and value, a search field and the artists grouped by
 * genre. Pull down to refresh.
 *
 * @param bottomPadding space the list leaves under its last item for the navigation bar
 * @param onOpenArtist called with the id of a tapped artist
 * @param onEditLabel called by the edit button next to the label name
 * @param viewModel state holder of the screen
 */
@Composable
fun RosterScreen(
    bottomPadding: PaddingValues,
    onOpenArtist: (artistId: String) -> Unit,
    onEditLabel: () -> Unit,
    viewModel: RosterViewModel = hiltViewModel(),
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()

    CollectEvents(viewModel.uiEventsFlow) { event ->
        when (event) {
            is RosterViewEvent.OpenArtist -> onOpenArtist(event.artistId)

            RosterViewEvent.OpenEditLabel -> onEditLabel()
        }
    }

    RosterContent(state = state, onAction = viewModel::handleUiAction, bottomPadding = bottomPadding)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RosterContent(
    state: RosterViewState,
    onAction: (RosterViewAction) -> Unit,
    bottomPadding: PaddingValues,
) {
    val pullState = rememberPullToRefreshState()
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { onAction(RosterViewAction.PullToRefresh) },
        state = pullState,
        modifier = Modifier.fillMaxSize(),
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullState,
                isRefreshing = state.isRefreshing,
                containerColor = AppThemeWrapper.colors.surface,
                color = AppThemeWrapper.colors.accent,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding(),
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = statusBar + 12.dp,
                bottom = bottomPadding.calculateBottomPadding(),
            ),
        ) {
            item(key = "header") {
                Header(state = state, onAction = onAction)
            }

            state.staleData?.let { staleData ->
                item(key = "stale") {
                    StaleDataBanner(
                        staleData = staleData,
                        isRetrying = state.isRetrying,
                        onAction = onAction,
                    )
                }
            }

            item(key = "stats") {
                Stats(state = state, onAction = onAction)
            }

            item(key = "search") {
                SearchField(
                    query = state.query,
                    onQueryChange = { onAction(RosterViewAction.QueryChanged(it)) },
                    placeholder = stringResource(R.string.roster_search_placeholder),
                    modifier = Modifier
                        .padding(horizontal = AppThemeWrapper.dimens.space4)
                        .padding(top = 14.dp),
                )
            }

            when {
                state.isLoading -> skeleton()

                state.isError -> item(key = "error") { LoadError(onAction = onAction) }

                state.isNoResults -> item(key = "noResults") {
                    NoResults(query = state.query, onAction = onAction)
                }

                else -> roster(sections = state.sections, onAction = onAction)
            }
        }
    }
}

@Composable
private fun Header(state: RosterViewState, onAction: (RosterViewAction) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (state.isLoading || state.isError) {
            SkeletonBlock(width = 210.dp, height = 28.dp, modifier = Modifier.weight(1f, false))

            Spacer(modifier = Modifier.weight(1f))
        } else {
            Text(text = state.flag, fontSize = 26.sp)

            Text(
                text = state.labelName,
                style = AppThemeWrapper.typography.largeTitle,
                color = AppThemeWrapper.colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }

        CircleIconButton(
            icon = AppIcons.Pencil,
            contentDescription = stringResource(R.string.roster_edit_label),
            onClick = { onAction(RosterViewAction.EditLabelClicked) },
        )
    }
}

@Composable
private fun StaleDataBanner(
    staleData: StaleDataVO,
    isRetrying: Boolean,
    onAction: (RosterViewAction) -> Unit,
) {
    Banner(
        title = stringResource(
            if (staleData.isOffline) R.string.roster_offline_title else R.string.roster_stale_title,
        ),
        message = stringResource(
            R.string.roster_stale_message,
            formatRecentMoment(staleData.syncedAt),
        ),
        tone = BannerTone.Warning,
        icon = if (staleData.isOffline) AppIcons.WifiOff else AppIcons.Warning,
        actionLabel = stringResource(R.string.common_retry),
        isActionLoading = isRetrying,
        onAction = { onAction(RosterViewAction.Retry) },
        modifier = Modifier
            .padding(horizontal = AppThemeWrapper.dimens.space4)
            .padding(top = 16.dp),
    )
}

@Composable
private fun Stats(state: RosterViewState, onAction: (RosterViewAction) -> Unit) {
    val colors = AppThemeWrapper.colors
    val isPlaceholder = state.isLoading || state.isError

    Row(
        modifier = Modifier
            .padding(horizontal = AppThemeWrapper.dimens.space4)
            .padding(top = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatTile(
            icon = AppIcons.Wallet,
            iconColors = ColorPair(content = colors.accentText, container = colors.accentSoft),
            label = stringResource(R.string.roster_budget),
            modifier = Modifier.weight(1f),
            topEnd = {
                CircleIconButton(
                    icon = if (state.isBudgetHidden) AppIcons.EyeOff else AppIcons.Eye,
                    contentDescription = stringResource(
                        if (state.isBudgetHidden) {
                            R.string.common_show_budget
                        } else {
                            R.string.common_hide_budget
                        },
                    ),
                    onClick = { onAction(RosterViewAction.ToggleBudget) },
                    style = CircleIconButtonStyle.Plain,
                    size = 36.dp,
                )
            },
        ) {
            if (isPlaceholder) {
                SkeletonBlock(
                    width = 120.dp,
                    height = 22.dp,
                    modifier = Modifier.padding(top = 3.dp),
                )
            } else {
                MoneyText(
                    amount = state.budget,
                    style = AppThemeWrapper.typography.moneyLarge,
                    isMasked = state.isBudgetHidden,
                )
            }
        }

        StatTile(
            icon = AppIcons.Music,
            iconColors = ColorPair(content = colors.info, container = colors.infoSoft),
            label = stringResource(R.string.roster_value),
            modifier = Modifier.weight(1f),
            topEnd = if (isPlaceholder) {
                null
            } else {
                {
                    Text(
                        text = pluralStringResource(
                            R.plurals.roster_artist_count,
                            state.artistCount,
                            state.artistCount,
                        ),
                        style = AppThemeWrapper.typography.subheadMedium,
                        color = colors.textSecondary,
                    )
                }
            },
        ) {
            if (isPlaceholder) {
                SkeletonBlock(
                    width = 132.dp,
                    height = 22.dp,
                    modifier = Modifier.padding(top = 3.dp),
                )
            } else {
                MoneyText(amount = state.rosterValue, style = AppThemeWrapper.typography.moneyLarge)
            }
        }
    }
}

@Composable
private fun LoadError(onAction: (RosterViewAction) -> Unit) {
    EmptyState(
        icon = AppIcons.WifiOff,
        title = stringResource(R.string.roster_error_title),
        message = stringResource(R.string.roster_error_message),
        actionLabel = stringResource(R.string.common_retry),
        onAction = { onAction(RosterViewAction.Retry) },
        modifier = Modifier.padding(top = 72.dp),
    )
}

@Composable
private fun NoResults(query: String, onAction: (RosterViewAction) -> Unit) {
    EmptyState(
        icon = AppIcons.Search,
        title = stringResource(R.string.roster_no_results_title, query.trim()),
        message = stringResource(R.string.roster_no_results_message),
        actionLabel = stringResource(R.string.common_clear_search),
        onAction = { onAction(RosterViewAction.ClearQuery) },
        modifier = Modifier.padding(top = 72.dp),
    )
}

private fun LazyListScope.roster(
    sections: List<RosterSectionVO>,
    onAction: (RosterViewAction) -> Unit,
) {
    sections.forEach { section ->
        item(key = "title-${section.genre}") {
            SectionHeader(
                title = stringResource(section.genre.titleRes),
                count = section.artists.size.toString(),
                modifier = Modifier
                    .animateItem()
                    .padding(top = 22.dp, bottom = 10.dp),
            )
        }

        itemsIndexed(section.artists, key = { _, artist -> artist.id }) { index, artist ->
            RosterArtistRow(
                artist = artist,
                position = GroupPosition.of(index, section.artists.size),
                onClick = { onAction(RosterViewAction.ArtistClicked(artist.id)) },
                modifier = Modifier.animateItem(),
            )
        }
    }
}

private fun LazyListScope.skeleton() {
    SkeletonSections.forEachIndexed { sectionIndex, rows ->
        item(key = "skeletonTitle-$sectionIndex") {
            SkeletonBlock(
                width = 110.dp,
                height = 16.dp,
                modifier = Modifier
                    .padding(horizontal = AppThemeWrapper.dimens.screenTitleInset)
                    .padding(top = 24.dp, bottom = 12.dp),
            )
        }

        items(rows) { index ->
            SkeletonArtistRow(position = GroupPosition.of(index, rows))
        }
    }
}

@Preview(name = "Roster", widthDp = 412, heightDp = 917)
@Composable
private fun RosterPreview() {
    RosterPreviewSurface(state = previewState, darkTheme = false)
}

@Preview(name = "Roster, dark", widthDp = 412, heightDp = 917)
@Composable
private fun RosterDarkPreview() {
    RosterPreviewSurface(state = previewState.copy(isBudgetHidden = true), darkTheme = true)
}

@Preview(name = "Roster, offline", widthDp = 412, heightDp = 917)
@Composable
private fun RosterOfflinePreview() {
    RosterPreviewSurface(
        state = previewState.copy(
            staleData = StaleDataVO(
                isOffline = true,
                syncedAt = Instant.parse("2026-10-25T14:32:00Z"),
            ),
        ),
        darkTheme = false,
    )
}

@Preview(name = "Roster, loading", widthDp = 412, heightDp = 917)
@Composable
private fun RosterLoadingPreview() {
    RosterPreviewSurface(state = RosterViewState(), darkTheme = false)
}

@Preview(name = "Roster, error", widthDp = 412, heightDp = 917)
@Composable
private fun RosterErrorPreview() {
    RosterPreviewSurface(state = RosterViewState(isLoading = false, isError = true), darkTheme = true)
}

@Preview(name = "Roster, no results", widthDp = 412, heightDp = 917)
@Composable
private fun RosterNoResultsPreview() {
    RosterPreviewSurface(
        state = previewState.copy(query = "Zidane", sections = emptyList(), isNoResults = true),
        darkTheme = false,
    )
}

@Composable
private fun RosterPreviewSurface(state: RosterViewState, darkTheme: Boolean) {
    AppTheme(darkTheme = darkTheme) {
        HomeScreen(
            tabContent = { _, bottomPadding ->
                RosterContent(state = state, onAction = {}, bottomPadding = bottomPadding)
            },
        )
    }
}

private val previewState = RosterViewState(
    isLoading = false,
    labelName = "Rhein Valley Records",
    flag = "🇩🇪",
    budget = 3_000_000,
    rosterValue = 11_240_000,
    artistCount = 16,
    sections = listOf(
        RosterSectionVO(
            genre = Genre.Pop,
            artists = listOf(
                previewArtist("1", "Lukas Brenner", "LB", Genre.Pop, "🇩🇪", "Germany"),
                previewArtist("2", "Matteo Ricci", "MR", Genre.Pop, "🇮🇹", "Italy")
                    .copy(age = 31, marketValue = 1_300_000, valueRise = "30%"),
                previewArtist("3", "Jonas Vermeulen", "JV", Genre.Pop, "🇧🇪", "Belgium")
                    .copy(age = 19, askingPrice = 1_600_000),
            ),
        ),
        RosterSectionVO(
            genre = Genre.Rock,
            artists = listOf(
                previewArtist("4", "Aidan Byrne", "AB", Genre.Rock, "🇮🇪", "Ireland"),
                previewArtist("5", "Théo Marchand", "TM", Genre.Rock, "🇫🇷", "France")
                    .copy(age = 22, marketValue = 1_450_000, valueRise = "45%"),
                previewArtist("6", "Daan de Vries", "DV", Genre.Rock, "🇳🇱", "Netherlands"),
                previewArtist("7", "Felix Gruber", "FG", Genre.Rock, "🇦🇹", "Austria"),
            ),
        ),
    ),
)

private fun previewArtist(
    id: String,
    name: String,
    initials: String,
    genre: Genre,
    flag: String,
    country: String,
) = RosterArtistVO(
    id = id,
    fullName = name,
    initials = initials,
    genre = genre,
    flag = flag,
    countryName = country,
    age = 27,
    marketValue = 1_000_000,
    valueRise = null,
    askingPrice = null,
)
