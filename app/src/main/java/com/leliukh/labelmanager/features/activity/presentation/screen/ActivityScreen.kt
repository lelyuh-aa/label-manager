package com.leliukh.labelmanager.features.activity.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.leliukh.labelmanager.core.design_system.presentation.components.chip.SelectableChip
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.EmptyState
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.SkeletonArtistRow
import com.leliukh.labelmanager.core.design_system.presentation.components.list.GroupPosition
import com.leliukh.labelmanager.core.design_system.presentation.components.list.GroupedItem
import com.leliukh.labelmanager.core.design_system.presentation.components.list.SectionHeader
import com.leliukh.labelmanager.core.design_system.presentation.components.navigation.LargeTitle
import com.leliukh.labelmanager.core.design_system.presentation.components.navigation.TopBarTextAction
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppTheme
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper
import com.leliukh.labelmanager.core.design_system.presentation.theme.ColorPair
import com.leliukh.labelmanager.core.formatting.presentation.formatDate
import com.leliukh.labelmanager.core.formatting.presentation.formatMoney
import com.leliukh.labelmanager.core.formatting.presentation.formatMoneyCompact
import com.leliukh.labelmanager.core.formatting.presentation.relativeTime
import com.leliukh.labelmanager.core.ui_utils.presentation.CollectEvents
import com.leliukh.labelmanager.features.activity.domain.models.ActivityType
import com.leliukh.labelmanager.features.activity.presentation.models.ActivityFilter
import com.leliukh.labelmanager.features.activity.presentation.models.ActivityItemVO
import com.leliukh.labelmanager.features.activity.presentation.viewmodel.ActivityViewAction
import com.leliukh.labelmanager.features.activity.presentation.viewmodel.ActivityViewEvent
import com.leliukh.labelmanager.features.activity.presentation.viewmodel.ActivityViewModel
import com.leliukh.labelmanager.features.activity.presentation.viewmodel.ActivityViewState
import com.leliukh.labelmanager.features.home.presentation.models.HomeTab
import com.leliukh.labelmanager.features.home.presentation.screen.HomeScreen
import com.leliukh.labelmanager.features.label.domain.models.Genre
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.toJavaInstant

private const val SKELETON_ROWS = 5

/**
 * The Activity tab: what happened to the label, newest first, grouped by day, with filter
 * chips and Mark all read.
 *
 * @param bottomPadding space the list leaves under its last item for the navigation bar
 * @param onOpenArtist called with the id of the artist of a tapped event
 * @param viewModel state holder of the screen
 */
@Composable
fun ActivityScreen(
    bottomPadding: PaddingValues,
    onOpenArtist: (artistId: String) -> Unit,
    viewModel: ActivityViewModel = hiltViewModel(),
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val items = viewModel.items.collectAsLazyPagingItems()

    CollectEvents(viewModel.uiEventsFlow) { event ->
        when (event) {
            is ActivityViewEvent.OpenArtist -> onOpenArtist(event.artistId)

            ActivityViewEvent.Reload -> items.refresh()
        }
    }

    LaunchedEffect(Unit) { viewModel.handleUiAction(ActivityViewAction.Shown) }

    ActivityContent(
        state = state,
        items = items,
        onAction = viewModel::handleUiAction,
        bottomPadding = bottomPadding,
    )
}

@Composable
private fun ActivityContent(
    state: ActivityViewState,
    items: LazyPagingItems<ActivityItemVO>,
    onAction: (ActivityViewAction) -> Unit,
    bottomPadding: PaddingValues,
    now: Instant = Clock.System.now(),
) {
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = statusBar + 12.dp,
            bottom = bottomPadding.calculateBottomPadding(),
        ),
    ) {
        item(key = "header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LargeTitle(
                    text = stringResource(R.string.activity_title),
                    modifier = Modifier.weight(1f),
                )

                if (state.unreadCount > 0) {
                    TopBarTextAction(
                        text = stringResource(R.string.activity_mark_all_read),
                        onClick = { onAction(ActivityViewAction.MarkAllRead) },
                        isLoading = state.isMarking,
                    )
                }
            }
        }

        item(key = "filters") {
            LazyRow(
                modifier = Modifier.padding(top = 14.dp, bottom = 6.dp),
                contentPadding = PaddingValues(horizontal = AppThemeWrapper.dimens.space4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(ActivityFilter.entries) { filter ->
                    SelectableChip(
                        label = stringResource(filter.labelRes),
                        isSelected = filter == state.filter,
                        onClick = { onAction(ActivityViewAction.FilterChosen(filter)) },
                    )
                }
            }
        }

        val refresh = items.loadState.refresh
        when {
            items.itemCount == 0 && refresh is LoadState.Loading -> items(SKELETON_ROWS) { index ->
                SkeletonArtistRow(
                    position = GroupPosition.of(index, SKELETON_ROWS),
                    modifier = Modifier.padding(top = if (index == 0) 16.dp else 0.dp),
                )
            }

            items.itemCount == 0 && refresh is LoadState.Error -> item(key = "error") {
                EmptyState(
                    icon = AppIcons.WifiOff,
                    title = stringResource(R.string.activity_error_title),
                    message = stringResource(R.string.roster_error_message),
                    actionLabel = stringResource(R.string.common_retry),
                    onAction = { items.retry() },
                    modifier = Modifier.padding(top = 72.dp),
                )
            }

            items.itemCount == 0 -> item(key = "empty") {
                EmptyState(
                    icon = AppIcons.Bell,
                    title = stringResource(R.string.activity_empty_title),
                    message = stringResource(R.string.activity_empty_message),
                    modifier = Modifier.padding(top = 72.dp),
                )
            }

            else -> items(count = items.itemCount, key = items.itemKey { it.key }) { index ->
                when (val item = items[index]) {
                    is ActivityItemVO.DayHeader -> SectionHeader(
                        title = dayTitle(item.date, now),
                        modifier = Modifier.padding(top = 20.dp, bottom = 10.dp),
                    )

                    is ActivityItemVO.Event -> EventRow(
                        event = item,
                        position = groupPosition(items, index),
                        now = now,
                        onClick = {
                            onAction(
                                ActivityViewAction.EventClicked(item.id, item.artistId, item.isRead),
                            )
                        },
                    )

                    null -> Unit
                }
            }
        }
    }
}

@Composable
private fun EventRow(
    event: ActivityItemVO.Event,
    position: GroupPosition,
    now: Instant,
    onClick: () -> Unit,
) {
    val colors = AppThemeWrapper.colors
    val style = event.type.style()
    val unreadLabel = stringResource(R.string.activity_unread)

    GroupedItem(position = position, onClick = onClick, separatorInset = 68.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(style.colors.container, AppThemeWrapper.shapes.small),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = style.icon,
                    contentDescription = null,
                    tint = style.colors.content,
                    modifier = Modifier.size(19.dp),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = eventTitle(event),
                    style = AppThemeWrapper.typography.bodyMedium.copy(
                        fontWeight = if (event.isRead) FontWeight.Medium else FontWeight.SemiBold,
                    ),
                    color = colors.textPrimary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = eventSubtitle(event),
                    style = AppThemeWrapper.typography.subhead,
                    color = colors.textSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                eventAmount(event)?.let { amount ->
                    Text(
                        text = amount,
                        style = AppThemeWrapper.typography.money,
                        color = when (event.type) {
                            ActivityType.ArtistSold -> colors.positive

                            ActivityType.ArtistBought -> colors.negative

                            else -> colors.textSecondary
                        },
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (!event.isRead) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(colors.accent, CircleShape)
                                .semantics { contentDescription = unreadLabel },
                        )
                    }

                    Text(
                        text = eventTime(event.occurredAt, now),
                        style = AppThemeWrapper.typography.caption,
                        color = colors.textSecondary,
                    )
                }
            }
        }
    }
}

private class EventStyle(val icon: ImageVector, val colors: ColorPair)

@Composable
private fun ActivityType.style(): EventStyle {
    val colors = AppThemeWrapper.colors
    return when (this) {
        ActivityType.ArtistSold ->
            EventStyle(AppIcons.Coins, ColorPair(colors.positive, colors.positiveSoft))

        ActivityType.ArtistBought ->
            EventStyle(AppIcons.Swap, ColorPair(colors.accentText, colors.accentSoft))

        ActivityType.ArtistListed ->
            EventStyle(AppIcons.Tag, ColorPair(colors.textSecondary, colors.fill))

        ActivityType.ArtistUnlisted ->
            EventStyle(AppIcons.Close, ColorPair(colors.textSecondary, colors.fill))
    }
}

@Composable
private fun eventTitle(event: ActivityItemVO.Event): String = when (event.type) {
    ActivityType.ArtistSold -> stringResource(
        R.string.activity_sold_title,
        event.counterpartyName.orEmpty(),
        event.artistName,
    )

    ActivityType.ArtistBought -> stringResource(R.string.activity_bought_title, event.artistName)

    ActivityType.ArtistListed -> stringResource(R.string.activity_listed_title, event.artistName)

    ActivityType.ArtistUnlisted ->
        stringResource(R.string.activity_unlisted_title, event.artistName)
}

@Composable
private fun eventSubtitle(event: ActivityItemVO.Event): String = when (event.type) {
    ActivityType.ArtistSold -> stringResource(R.string.activity_sold_subtitle)

    ActivityType.ArtistBought -> if (event.valueAfter != null && event.valueRise != null) {
        stringResource(
            R.string.activity_bought_subtitle,
            event.counterpartyName.orEmpty(),
            formatMoney(event.valueAfter),
            event.valueRise,
        )
    } else {
        stringResource(R.string.activity_bought_subtitle_short, event.counterpartyName.orEmpty())
    }

    ActivityType.ArtistListed ->
        stringResource(R.string.activity_listed_subtitle, formatMoney(event.amount ?: 0))

    ActivityType.ArtistUnlisted -> stringResource(R.string.activity_unlisted_subtitle)
}

private fun eventAmount(event: ActivityItemVO.Event): String? {
    val amount = event.amount ?: return null
    return when (event.type) {
        ActivityType.ArtistSold -> "+${formatMoney(amount)}"

        ActivityType.ArtistBought -> "−${formatMoney(amount)}"

        ActivityType.ArtistListed -> formatMoneyCompact(amount)

        ActivityType.ArtistUnlisted -> null
    }
}

@Composable
private fun eventTime(occurredAt: Instant, now: Instant): String {
    val zone = TimeZone.currentSystemDefault()
    val isToday = occurredAt.toLocalDateTime(zone).date == now.toLocalDateTime(zone).date
    return if (isToday) {
        relativeTime(occurredAt, now)
    } else {
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            .format(occurredAt.toJavaInstant().atZone(ZoneId.systemDefault()))
    }
}

@Composable
private fun dayTitle(date: LocalDate, now: Instant): String {
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return when (date) {
        today -> stringResource(R.string.activity_today)

        today.minus(1, DateTimeUnit.DAY) -> stringResource(R.string.activity_yesterday)

        else -> formatDate(date)
    }
}

private fun groupPosition(items: LazyPagingItems<ActivityItemVO>, index: Int): GroupPosition {
    val isFirst = index == 0 || items.peek(index - 1) is ActivityItemVO.DayHeader
    val isLast = index == items.itemCount - 1 || items.peek(index + 1) is ActivityItemVO.DayHeader
    return when {
        isFirst && isLast -> GroupPosition.Single

        isFirst -> GroupPosition.First

        isLast -> GroupPosition.Last

        else -> GroupPosition.Middle
    }
}

private val ActivityFilter.labelRes: Int
    get() = when (this) {
        ActivityFilter.All -> R.string.activity_filter_all

        ActivityFilter.Sales -> R.string.activity_filter_sales

        ActivityFilter.Purchases -> R.string.activity_filter_purchases

        ActivityFilter.Listings -> R.string.activity_filter_listings
    }

@Preview(name = "Activity", widthDp = 412, heightDp = 917)
@Composable
private fun ActivityPreview() {
    ActivityPreviewSurface(
        state = ActivityViewState(unreadCount = 2),
        items = previewItems,
        darkTheme = false,
    )
}

@Preview(name = "Activity, dark", widthDp = 412, heightDp = 917)
@Composable
private fun ActivityDarkPreview() {
    ActivityPreviewSurface(state = ActivityViewState(), items = previewItems, darkTheme = true)
}

@Preview(name = "Activity, empty", widthDp = 412, heightDp = 917)
@Composable
private fun ActivityEmptyPreview() {
    ActivityPreviewSurface(state = ActivityViewState(), items = emptyList(), darkTheme = false)
}

@Composable
private fun ActivityPreviewSurface(
    state: ActivityViewState,
    items: List<ActivityItemVO>,
    darkTheme: Boolean,
) {
    val notLoading = LoadState.NotLoading(endOfPaginationReached = true)
    val pagingItems = flowOf(
        PagingData.from(
            data = items,
            sourceLoadStates = LoadStates(
                refresh = notLoading,
                prepend = notLoading,
                append = notLoading,
            ),
        ),
    ).collectAsLazyPagingItems()

    AppTheme(darkTheme = darkTheme) {
        HomeScreen(
            initialTab = HomeTab.Activity,
            badges = mapOf(HomeTab.Activity to state.unreadCount),
            tabContent = { _, bottomPadding ->
                ActivityContent(
                    state = state,
                    items = pagingItems,
                    onAction = {},
                    bottomPadding = bottomPadding,
                    now = previewNow,
                )
            },
        )
    }
}

private val previewNow = Instant.parse("2026-10-25T12:00:00Z")

private val previewItems = listOf(
    ActivityItemVO.DayHeader(LocalDate(2026, 10, 25)),
    previewEvent("1", ActivityType.ArtistSold, "Jonas Vermeulen", "Lagos Sound", 1_600_000, 2)
        .copy(isRead = false),
    previewEvent("2", ActivityType.ArtistBought, "Chidi Okafor", "Lagos Sound", 1_450_000, 14)
        .copy(isRead = false, valueAfter = 1_350_000, valueRise = "35%"),
    previewEvent("3", ActivityType.ArtistListed, "Niklas Weber", null, 1_600_000, 120),
    ActivityItemVO.DayHeader(LocalDate(2026, 10, 24)),
    previewEvent("4", ActivityType.ArtistBought, "Théo Marchand", "Garonne Records", 1_000_000, 1_000)
        .copy(valueAfter = 1_450_000, valueRise = "45%"),
    previewEvent("5", ActivityType.ArtistUnlisted, "Matteo Ricci", null, null, 1_400),
)

private fun previewEvent(
    id: String,
    type: ActivityType,
    artist: String,
    counterparty: String?,
    amount: Long?,
    minutesAgo: Long,
) = ActivityItemVO.Event(
    id = id,
    type = type,
    artistId = "p$id",
    artistName = artist,
    genre = Genre.HipHop,
    counterpartyName = counterparty,
    amount = amount,
    valueAfter = null,
    valueRise = null,
    occurredAt = Instant.fromEpochMilliseconds(
        previewNow.toEpochMilliseconds() - minutesAgo * 60_000,
    ),
    isRead = true,
)
