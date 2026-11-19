package com.leliukh.labelmanager.features.home.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.EmptyState
import com.leliukh.labelmanager.core.design_system.presentation.components.navigation.BottomFade
import com.leliukh.labelmanager.core.design_system.presentation.components.navigation.FloatingNavigationBar
import com.leliukh.labelmanager.core.design_system.presentation.components.navigation.LargeTitle
import com.leliukh.labelmanager.core.design_system.presentation.components.navigation.NavigationBarTab
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppTheme
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper
import com.leliukh.labelmanager.features.home.presentation.models.HomeTab

private const val TAB_FADE_MS = 180

/**
 * The signed-in home: the selected tab's content under a floating navigation bar.
 *
 * Each tab keeps its saveable state (e.g. scroll genre) while another tab is shown. Back
 * on any tab but Roster returns to Roster before leaving the app.
 *
 * @param tabContent draws a tab; receives the bottom padding its scrolling content needs to
 * clear the navigation bar
 * @param initialTab tab shown first
 * @param badges unread counts per tab
 */
@Composable
fun HomeScreen(
    tabContent: @Composable (tab: HomeTab, bottomPadding: PaddingValues) -> Unit,
    initialTab: HomeTab = HomeTab.Roster,
    badges: Map<HomeTab, Int> = emptyMap(),
) {
    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }
    val stateHolder = rememberSaveableStateHolder()
    val dimens = AppThemeWrapper.dimens
    val navigationBar = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val contentBottomPadding = PaddingValues(
        bottom = navigationBar +
            dimens.navigationBarBottomInset +
            dimens.navigationBarHeight +
            dimens.space4,
    )
    val tabs = HomeTab.entries.map { tab ->
        NavigationBarTab(
            label = stringResource(tab.labelRes),
            icon = tab.icon,
            badgeCount = badges[tab],
        )
    }

    BackHandler(enabled = selectedTab != HomeTab.Roster) { selectedTab = HomeTab.Roster }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppThemeWrapper.colors.background),
    ) {
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                fadeIn(tween(TAB_FADE_MS)) togetherWith fadeOut(tween(TAB_FADE_MS))
            },
            label = "homeTab",
        ) { tab ->
            stateHolder.SaveableStateProvider(tab.name) {
                tabContent(tab, contentBottomPadding)
            }
        }

        BottomFade(modifier = Modifier.align(Alignment.BottomCenter))

        FloatingNavigationBar(
            items = tabs,
            selectedIndex = selectedTab.ordinal,
            onSelect = { index -> selectedTab = HomeTab.entries[index] },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = dimens.space4)
                .padding(bottom = dimens.navigationBarBottomInset),
        )
    }
}

/**
 * Stand-in for a tab whose screen is not built yet.
 *
 * @param tab the tab
 */
@Composable
private fun TabPlaceholder(tab: HomeTab) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 12.dp),
    ) {
        LargeTitle(
            text = stringResource(tab.labelRes),
            modifier = Modifier.padding(horizontal = AppThemeWrapper.dimens.screenTitleInset),
        )

        EmptyState(
            icon = tab.icon,
            title = stringResource(R.string.common_coming_soon_title),
            message = stringResource(R.string.common_coming_soon_message),
            modifier = Modifier.padding(top = 120.dp),
        )
    }
}

@Preview(name = "Home", widthDp = 412, heightDp = 917)
@Composable
private fun HomeScreenPreview() {
    AppTheme(darkTheme = false) {
        HomeScreen(
            tabContent = { tab, _ -> TabPlaceholder(tab) },
            badges = mapOf(HomeTab.Activity to 2),
        )
    }
}

@Preview(name = "Home, dark, Market", widthDp = 412, heightDp = 917)
@Composable
private fun HomeScreenDarkPreview() {
    AppTheme(darkTheme = true) {
        HomeScreen(
            tabContent = { tab, _ -> TabPlaceholder(tab) },
            initialTab = HomeTab.Market,
            badges = mapOf(HomeTab.Activity to 2),
        )
    }
}
