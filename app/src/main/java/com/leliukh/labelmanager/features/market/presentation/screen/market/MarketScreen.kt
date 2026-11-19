package com.leliukh.labelmanager.features.market.presentation.screen.market

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.design_system.presentation.components.chip.RemovableChip
import com.leliukh.labelmanager.core.design_system.presentation.components.chip.SegmentedControl
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.EmptyState
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.SkeletonArtistRow
import com.leliukh.labelmanager.core.design_system.presentation.components.field.SearchField
import com.leliukh.labelmanager.core.design_system.presentation.components.list.GroupPosition
import com.leliukh.labelmanager.core.design_system.presentation.components.list.SectionHeader
import com.leliukh.labelmanager.core.design_system.presentation.components.money.BudgetPill
import com.leliukh.labelmanager.core.design_system.presentation.components.navigation.ExtendedFab
import com.leliukh.labelmanager.core.design_system.presentation.components.navigation.LargeTitle
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppTheme
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper
import com.leliukh.labelmanager.core.ui_utils.presentation.CollectEvents
import com.leliukh.labelmanager.features.home.presentation.models.HomeTab
import com.leliukh.labelmanager.features.home.presentation.screen.HomeScreen
import com.leliukh.labelmanager.features.market.domain.models.ListingFilter
import com.leliukh.labelmanager.features.market.domain.models.ListingSort
import com.leliukh.labelmanager.features.market.domain.models.SearchScope
import com.leliukh.labelmanager.features.market.presentation.models.MarketListingVO
import com.leliukh.labelmanager.features.market.presentation.navigation.DealDoneRoute
import com.leliukh.labelmanager.features.market.presentation.screen.components.MarketListingRow
import com.leliukh.labelmanager.features.market.presentation.viewmodel.market.MarketViewAction
import com.leliukh.labelmanager.features.market.presentation.viewmodel.market.MarketViewEvent
import com.leliukh.labelmanager.features.market.presentation.viewmodel.market.MarketViewModel
import com.leliukh.labelmanager.features.market.presentation.viewmodel.market.MarketViewState
import com.leliukh.labelmanager.features.label.domain.models.Genre
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Instant

private const val SKELETON_ROWS = 6
private val FabSpace = 72.dp

/**
 * The Market tab: search by artist or label, filters with chips, the paged market with
 * Buy, and Sell artist.
 *
 * @param bottomPadding space the list leaves under its last item for the navigation bar
 * @param onOpenArtist called with the id of a tapped artist
 * @param onDealDone called after a purchase with what the deal-done moment shows
 * @param onSellArtist opens the sell flow
 * @param viewModel state holder of the screen
 */
@Composable
fun MarketScreen(
    bottomPadding: PaddingValues,
    onOpenArtist: (artistId: String) -> Unit,
    onDealDone: (DealDoneRoute) -> Unit,
    onSellArtist: () -> Unit,
    viewModel: MarketViewModel = hiltViewModel(),
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val listings = viewModel.listings.collectAsLazyPagingItems()

    CollectEvents(viewModel.uiEventsFlow) { event ->
        when (event) {
            is MarketViewEvent.OpenArtist -> onOpenArtist(event.artistId)

            is MarketViewEvent.OpenDealDone -> onDealDone(event.route)

            MarketViewEvent.OpenSellArtist -> onSellArtist()

            MarketViewEvent.ListingsChanged -> listings.refresh()
        }
    }

    MarketContent(
        state = state,
        listings = listings,
        onAction = viewModel::handleUiAction,
        bottomPadding = bottomPadding,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarketContent(
    state: MarketViewState,
    listings: LazyPagingItems<MarketListingVO>,
    onAction: (MarketViewAction) -> Unit,
    bottomPadding: PaddingValues,
    now: Instant = Clock.System.now(),
) {
    val pullState = rememberPullToRefreshState()
    val listState = rememberLazyListState()
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val isRefreshing = listings.loadState.refresh is LoadState.Loading && listings.itemCount > 0

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { listings.refresh() },
            state = pullState,
            modifier = Modifier.fillMaxSize(),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullState,
                    isRefreshing = isRefreshing,
                    containerColor = AppThemeWrapper.colors.surface,
                    color = AppThemeWrapper.colors.accent,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding(),
                )
            },
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = statusBar + 12.dp,
                    bottom = bottomPadding.calculateBottomPadding() + FabSpace,
                ),
            ) {
                item(key = "header") { Header(state = state, onAction = onAction) }

                item(key = "search") { SearchRow(state = state, onAction = onAction) }

                item(key = "scope") {
                    SegmentedControl(
                        options = listOf(
                            stringResource(R.string.market_scope_artists),
                            stringResource(R.string.market_scope_labels),
                        ),
                        selectedIndex = state.scope.ordinal,
                        onSelect = { index ->
                            onAction(MarketViewAction.ScopeChanged(SearchScope.entries[index]))
                        },
                        modifier = Modifier
                            .padding(horizontal = AppThemeWrapper.dimens.space4)
                            .padding(top = 12.dp),
                    )
                }

                if (state.chips.isNotEmpty()) {
                    item(key = "chips") { Chips(state = state, onAction = onAction) }
                }

                item(key = "title") {
                    SectionHeader(
                        title = stringResource(R.string.market_listings),
                        count = state.totalCount?.toString(),
                        trailing = { SortMenu(sort = state.filter.sort, onAction = onAction) },
                        modifier = Modifier.padding(top = 20.dp, bottom = 10.dp),
                    )
                }

                listingItems(state = state, listings = listings, onAction = onAction, now = now)
            }
        }

        ExtendedFab(
            text = stringResource(R.string.market_sell_artist),
            icon = AppIcons.Plus,
            onClick = { onAction(MarketViewAction.SellClicked) },
            isExpanded = listState.firstVisibleItemIndex == 0,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = bottomPadding.calculateBottomPadding()),
        )
    }

    state.draft?.let { draft ->
        FiltersSheet(
            draft = draft,
            matchCount = state.draftCount,
            onChange = { onAction(MarketViewAction.DraftChanged(it)) },
            onReset = { onAction(MarketViewAction.ResetDraft) },
            onApply = { onAction(MarketViewAction.ApplyDraft) },
            onDismiss = { onAction(MarketViewAction.CloseFilters) },
        )
    }

    state.buyConfirm?.let { confirm ->
        BuyConfirmSheet(
            confirm = confirm,
            onConfirm = { onAction(MarketViewAction.ConfirmPurchase) },
            onSell = { onAction(MarketViewAction.SellClicked) },
            onDismiss = { onAction(MarketViewAction.DismissBuy) },
        )
    }
}

private fun LazyListScope.listingItems(
    state: MarketViewState,
    listings: LazyPagingItems<MarketListingVO>,
    onAction: (MarketViewAction) -> Unit,
    now: Instant,
) {
    val refresh = listings.loadState.refresh
    when {
        listings.itemCount == 0 && refresh is LoadState.Loading -> items(SKELETON_ROWS) { index ->
            SkeletonArtistRow(position = GroupPosition.of(index, SKELETON_ROWS))
        }

        listings.itemCount == 0 && refresh is LoadState.Error -> item(key = "error") {
            EmptyState(
                icon = AppIcons.WifiOff,
                title = stringResource(R.string.market_error_title),
                message = stringResource(R.string.market_error_message),
                actionLabel = stringResource(R.string.common_retry),
                onAction = { listings.retry() },
                modifier = Modifier.padding(top = 48.dp),
            )
        }

        listings.itemCount == 0 -> item(key = "empty") {
            val isFiltered = state.chips.isNotEmpty() || state.query.isNotBlank()
            EmptyState(
                icon = if (isFiltered) AppIcons.Search else AppIcons.Swap,
                title = stringResource(
                    if (isFiltered) {
                        R.string.market_no_results_title
                    } else {
                        R.string.market_empty_title
                    },
                ),
                message = stringResource(
                    if (isFiltered) {
                        R.string.market_no_results_message
                    } else {
                        R.string.market_empty_message
                    },
                ),
                actionLabel = stringResource(R.string.market_clear_filters).takeIf { isFiltered },
                onAction = {
                    onAction(MarketViewAction.QueryChanged(""))
                    onAction(MarketViewAction.ClearFilters)
                },
                modifier = Modifier.padding(top = 48.dp),
            )
        }

        else -> {
            items(count = listings.itemCount, key = listings.itemKey { it.listingId }) { index ->
                val listing = listings[index] ?: return@items
                MarketListingRow(
                    listing = listing,
                    position = GroupPosition.of(index, listings.itemCount),
                    now = now,
                    onClick = { onAction(MarketViewAction.ListingClicked(listing)) },
                    onBuy = { onAction(MarketViewAction.BuyClicked(listing)) },
                    modifier = Modifier.animateItem(),
                )
            }

            appendFooter(listings)
        }
    }
}

private fun LazyListScope.appendFooter(listings: LazyPagingItems<MarketListingVO>) {
    when (listings.loadState.append) {
        is LoadState.Loading -> item(key = "appendLoading") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = AppThemeWrapper.colors.accent,
                    strokeWidth = 2.5.dp,
                )
            }
        }

        is LoadState.Error -> item(key = "appendError") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button) { listings.retry() }
                    .padding(vertical = 16.dp, horizontal = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.market_load_more_failed),
                    style = AppThemeWrapper.typography.callout,
                    color = AppThemeWrapper.colors.textSecondary,
                )

                Text(
                    text = " · " + stringResource(R.string.common_retry),
                    style = AppThemeWrapper.typography.callout,
                    color = AppThemeWrapper.colors.accentText,
                )
            }
        }

        is LoadState.NotLoading -> Unit
    }
}

@Composable
private fun Header(state: MarketViewState, onAction: (MarketViewAction) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LargeTitle(text = stringResource(R.string.market_title), modifier = Modifier.weight(1f))

        if (state.budget != null) {
            BudgetPill(
                amount = state.budget,
                isMasked = state.isBudgetHidden,
                onToggleMask = { onAction(MarketViewAction.ToggleBudget) },
            )
        }
    }
}

@Composable
private fun SearchRow(state: MarketViewState, onAction: (MarketViewAction) -> Unit) {
    val colors = AppThemeWrapper.colors
    val filterCount = state.chips.size
    val filtersLabel = if (filterCount > 0) {
        stringResource(R.string.market_filters_active, filterCount)
    } else {
        stringResource(R.string.market_filters)
    }

    Row(
        modifier = Modifier
            .padding(horizontal = AppThemeWrapper.dimens.space4)
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SearchField(
            query = state.query,
            onQueryChange = { onAction(MarketViewAction.QueryChanged(it)) },
            placeholder = stringResource(
                if (state.scope == SearchScope.Artists) {
                    R.string.market_search_artists
                } else {
                    R.string.market_search_labels
                },
            ),
            modifier = Modifier.weight(1f),
        )

        Box {
            Box(
                modifier = Modifier
                    .size(AppThemeWrapper.dimens.controlHeight)
                    .clip(AppThemeWrapper.shapes.control)
                    .background(colors.accentSoft)
                    .clickable(role = Role.Button) { onAction(MarketViewAction.OpenFilters) }
                    .semantics { contentDescription = filtersLabel },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = AppIcons.Sliders,
                    contentDescription = null,
                    tint = colors.accentText,
                    modifier = Modifier.size(20.dp),
                )
            }

            if (filterCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 5.dp, y = (-5).dp)
                        .size(20.dp)
                        .background(colors.background, CircleShape)
                        .padding(2.dp)
                        .background(colors.accent, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = filterCount.toString(),
                        style = AppThemeWrapper.typography.micro,
                        color = colors.onAccent,
                    )
                }
            }
        }
    }
}

@Composable
private fun Chips(state: MarketViewState, onAction: (MarketViewAction) -> Unit) {
    LazyRow(
        modifier = Modifier.padding(top = 12.dp),
        contentPadding = PaddingValues(horizontal = AppThemeWrapper.dimens.space4),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(state.chips) { chip ->
            RemovableChip(
                label = chip.label(),
                onRemove = { onAction(MarketViewAction.ChipRemoved(chip)) },
            )
        }

        item {
            Text(
                text = stringResource(R.string.market_clear),
                style = AppThemeWrapper.typography.callout,
                color = AppThemeWrapper.colors.accentText,
                modifier = Modifier
                    .clip(AppThemeWrapper.shapes.pill)
                    .clickable(role = Role.Button) { onAction(MarketViewAction.ClearFilters) }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun SortMenu(sort: ListingSort, onAction: (MarketViewAction) -> Unit) {
    var isOpen by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .height(32.dp)
                .clip(AppThemeWrapper.shapes.pill)
                .clickable(role = Role.DropdownList) { isOpen = true }
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = sort.label(),
                style = AppThemeWrapper.typography.subheadMedium,
                color = AppThemeWrapper.colors.textSecondary,
            )

            Icon(
                imageVector = AppIcons.ChevronDown,
                contentDescription = null,
                tint = AppThemeWrapper.colors.textSecondary,
                modifier = Modifier.size(14.dp),
            )
        }

        DropdownMenu(
            expanded = isOpen,
            onDismissRequest = { isOpen = false },
            containerColor = AppThemeWrapper.colors.surface,
        ) {
            ListingSort.entries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.label(),
                            style = if (option == sort) {
                                AppThemeWrapper.typography.bodySemibold
                            } else {
                                AppThemeWrapper.typography.body
                            },
                            color = AppThemeWrapper.colors.textPrimary,
                        )
                    },
                    trailingIcon = if (option == sort) {
                        {
                            Icon(
                                imageVector = AppIcons.Check,
                                contentDescription = null,
                                tint = AppThemeWrapper.colors.accentText,
                            )
                        }
                    } else {
                        null
                    },
                    onClick = {
                        isOpen = false
                        onAction(MarketViewAction.SortChosen(option))
                    },
                )
            }
        }
    }
}

@Preview(name = "Market", widthDp = 412, heightDp = 917)
@Composable
private fun MarketPreview() {
    MarketPreviewSurface(state = previewState, listings = previewListings, darkTheme = false)
}

@Preview(name = "Market, filtered, dark", widthDp = 412, heightDp = 917)
@Composable
private fun MarketFilteredPreview() {
    MarketPreviewSurface(
        state = previewState.copy(
            filter = ListingFilter(maxPrice = 2_000_000, genres = setOf(Genre.HipHop)),
            totalCount = 3,
            isBudgetHidden = true,
        ),
        listings = previewListings.take(3),
        darkTheme = true,
    )
}

@Preview(name = "Market, no results", widthDp = 412, heightDp = 917)
@Composable
private fun MarketNoResultsPreview() {
    MarketPreviewSurface(
        state = previewState.copy(
            filter = ListingFilter(maxPrice = 500_000, artistCountry = "IT"),
            totalCount = 0,
        ),
        listings = emptyList(),
        darkTheme = false,
    )
}

@Preview(name = "Market, loading", widthDp = 412, heightDp = 917)
@Composable
private fun MarketLoadingPreview() {
    MarketPreviewSurface(
        state = previewState.copy(totalCount = null),
        listings = emptyList(),
        darkTheme = false,
        refresh = LoadState.Loading,
    )
}

@Composable
private fun MarketPreviewSurface(
    state: MarketViewState,
    listings: List<MarketListingVO>,
    darkTheme: Boolean,
    refresh: LoadState = LoadState.NotLoading(endOfPaginationReached = false),
) {
    val notLoading = LoadState.NotLoading(endOfPaginationReached = true)
    val items = flowOf(
        PagingData.from(
            data = listings,
            sourceLoadStates = LoadStates(
                refresh = refresh,
                prepend = notLoading,
                append = notLoading,
            ),
        ),
    ).collectAsLazyPagingItems()

    AppTheme(darkTheme = darkTheme) {
        HomeScreen(
            initialTab = HomeTab.Market,
            tabContent = { _, bottomPadding ->
                MarketContent(
                    state = state,
                    listings = items,
                    onAction = {},
                    bottomPadding = bottomPadding,
                    now = previewNow,
                )
            },
        )
    }
}

private val previewNow = Instant.parse("2026-10-25T12:00:00Z")

private val previewState = MarketViewState(budget = 3_000_000, totalCount = 24)

private val previewListings = listOf(
    previewListing("Chidi Okafor", Genre.HipHop, "🇳🇬", "Lagos Sound", 1_450_000, 12),
    previewListing("Luca Ferri", Genre.Electronic, "🇮🇹", "Tiber Dischi", 2_100_000, 45),
    previewListing("Jonas Vermeulen", Genre.Pop, "🇧🇪", "Rhein Valley Records", 1_600_000, 120)
        .copy(isMine = true),
    previewListing("Mateo Silva", Genre.Rock, "🇧🇷", "Rio Azul", 980_000, 180),
    previewListing("Erik Lund", Genre.HipHop, "🇸🇪", "Göta Musik", 1_250_000, 300),
    previewListing("Pierre Dubois", Genre.Electronic, "🇫🇷", "Garonne Records", 3_400_000, 600),
)

private fun previewListing(
    name: String,
    genre: Genre,
    flag: String,
    seller: String,
    price: Long,
    minutesAgo: Long,
) = MarketListingVO(
    listingId = name,
    artistId = name,
    fullName = name,
    initials = name.split(' ').joinToString("") { it.take(1) },
    genre = genre,
    flag = flag,
    countryName = "",
    age = 24,
    sellerName = seller,
    askingPrice = price,
    listedAt = Instant.fromEpochMilliseconds(
        previewNow.toEpochMilliseconds() - minutesAgo * 60_000,
    ),
    isMine = false,
)
