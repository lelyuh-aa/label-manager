package com.leliukh.labelmanager.features.entry.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.leliukh.labelmanager.core.navigation.presentation.popTransition
import com.leliukh.labelmanager.core.navigation.presentation.predictivePopTransition
import com.leliukh.labelmanager.core.navigation.presentation.pushTransition
import com.leliukh.labelmanager.features.activity.presentation.notification.RequestNotificationPermission
import com.leliukh.labelmanager.features.activity.presentation.screen.ActivityScreen
import com.leliukh.labelmanager.features.auth.presentation.navigation.SignInRoute
import com.leliukh.labelmanager.features.auth.presentation.navigation.SignUpRoute
import com.leliukh.labelmanager.features.auth.presentation.navigation.WelcomeRoute
import com.leliukh.labelmanager.features.auth.presentation.screen.signin.SignInScreen
import com.leliukh.labelmanager.features.auth.presentation.screen.signup.SignUpScreen
import com.leliukh.labelmanager.features.auth.presentation.screen.welcome.WelcomeScreen
import com.leliukh.labelmanager.features.home.presentation.models.HomeTab
import com.leliukh.labelmanager.features.home.presentation.navigation.HomeRoute
import com.leliukh.labelmanager.features.home.presentation.screen.HomeScreen
import com.leliukh.labelmanager.features.home.presentation.viewmodel.HomeViewModel
import com.leliukh.labelmanager.features.market.presentation.navigation.CreateListingRoute
import com.leliukh.labelmanager.features.market.presentation.navigation.DealDoneRoute
import com.leliukh.labelmanager.features.market.presentation.navigation.SelectArtistRoute
import com.leliukh.labelmanager.features.market.presentation.screen.createlisting.CreateListingScreen
import com.leliukh.labelmanager.features.market.presentation.screen.dealdone.DealDoneScreen
import com.leliukh.labelmanager.features.market.presentation.screen.market.MarketScreen
import com.leliukh.labelmanager.features.market.presentation.screen.selectartist.SelectArtistScreen
import com.leliukh.labelmanager.features.artist.presentation.navigation.EditArtistRoute
import com.leliukh.labelmanager.features.artist.presentation.navigation.ArtistRoute
import com.leliukh.labelmanager.features.artist.presentation.screen.details.ArtistScreen
import com.leliukh.labelmanager.features.artist.presentation.screen.edit.EditArtistScreen
import com.leliukh.labelmanager.features.profile.presentation.screen.ProfileScreen
import com.leliukh.labelmanager.features.label.presentation.navigation.LabelReadyRoute
import com.leliukh.labelmanager.features.label.presentation.navigation.EditLabelRoute
import com.leliukh.labelmanager.features.label.presentation.screen.labelready.LabelReadyScreen
import com.leliukh.labelmanager.features.label.presentation.screen.editlabel.EditLabelScreen
import com.leliukh.labelmanager.features.label.presentation.screen.roster.RosterScreen

/**
 * The app's single navigation graph, built on Navigation 3.
 *
 * The back stack starts at [HomeRoute] for a stored session and at [WelcomeRoute] otherwise.
 * When the session ends while signed in (sign-out, or a refresh token the server rejected),
 * the stack is replaced by [WelcomeRoute] so no signed-in screen stays reachable with back.
 * Sign-in and sign-up replace each other instead of stacking, and a finished auth flow
 * replaces the whole stack, so back from Home leaves the app.
 *
 * @param isSignedIn whether a session exists; read once for the start and observed afterwards
 */
@Composable
fun AppNavigation(isSignedIn: Boolean) {
    val backStack = rememberNavBackStack(if (isSignedIn) HomeRoute else WelcomeRoute)

    LaunchedEffect(isSignedIn) {
        if (!isSignedIn && backStack.any { it is HomeRoute }) backStack.replaceAll(WelcomeRoute)
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        transitionSpec = pushTransition(),
        popTransitionSpec = popTransition(),
        predictivePopTransitionSpec = predictivePopTransition(),
        entryProvider = entryProvider {
            entry<WelcomeRoute> {
                WelcomeScreen(
                    onCreateAccount = { backStack.add(SignUpRoute) },
                    onSignIn = { backStack.add(SignInRoute) },
                )
            }

            entry<SignInRoute> {
                SignInScreen(
                    onSignedIn = { backStack.replaceAll(HomeRoute) },
                    onOpenSignUp = { backStack.replaceTop(SignUpRoute) },
                    onClose = { backStack.removeLastOrNull() },
                )
            }

            entry<SignUpRoute> {
                SignUpScreen(
                    onSignedUp = { backStack.replaceAll(LabelReadyRoute) },
                    onOpenSignIn = { backStack.replaceTop(SignInRoute) },
                    onClose = { backStack.removeLastOrNull() },
                )
            }

            entry<LabelReadyRoute> {
                LabelReadyScreen(onContinue = { backStack.replaceAll(HomeRoute) })
            }

            entry<HomeRoute> {
                val homeViewModel: HomeViewModel = hiltViewModel()
                val homeState by homeViewModel.viewState.collectAsStateWithLifecycle()

                RequestNotificationPermission()

                HomeScreen(
                    badges = mapOf(HomeTab.Activity to homeState.activityBadge),
                    tabContent = { tab, bottomPadding ->
                        when (tab) {
                            HomeTab.Roster -> RosterScreen(
                                bottomPadding = bottomPadding,
                                onOpenArtist = { backStack.add(ArtistRoute(it)) },
                                onEditLabel = { backStack.add(EditLabelRoute) },
                            )

                            HomeTab.Market -> MarketScreen(
                                bottomPadding = bottomPadding,
                                onOpenArtist = { backStack.add(ArtistRoute(it)) },
                                onDealDone = { route -> backStack.add(route) },
                                onSellArtist = { backStack.add(SelectArtistRoute) },
                            )

                            HomeTab.Activity -> ActivityScreen(
                                bottomPadding = bottomPadding,
                                onOpenArtist = { backStack.add(ArtistRoute(it)) },
                            )

                            HomeTab.Profile -> ProfileScreen(
                                bottomPadding = bottomPadding,
                                onEditLabel = { backStack.add(EditLabelRoute) },
                            )
                        }
                    },
                )
            }

            entry<ArtistRoute> { route ->
                ArtistScreen(
                    artistId = route.artistId,
                    onBack = { backStack.removeLastOrNull() },
                    onEdit = { artistId -> backStack.add(EditArtistRoute(artistId)) },
                    onCreateListing = { artistId -> backStack.add(CreateListingRoute(artistId)) },
                    onDealDone = { deal -> backStack.replaceTop(deal) },
                    onSellArtist = { backStack.add(SelectArtistRoute) },
                )
            }

            entry<EditArtistRoute> { route ->
                EditArtistScreen(
                    artistId = route.artistId,
                    onClose = { backStack.removeLastOrNull() },
                )
            }

            entry<SelectArtistRoute> {
                SelectArtistScreen(
                    onPick = { artistId -> backStack.add(CreateListingRoute(artistId)) },
                    onBack = { backStack.removeLastOrNull() },
                )
            }

            entry<CreateListingRoute> { route ->
                CreateListingScreen(
                    artistId = route.artistId,
                    onClose = { isListed ->
                        backStack.removeLastOrNull()
                        if (isListed && backStack.lastOrNull() == SelectArtistRoute) {
                            backStack.removeLastOrNull()
                        }
                    },
                )
            }

            entry<DealDoneRoute> { route ->
                DealDoneScreen(
                    deal = route,
                    onOpenArtist = { backStack.replaceTop(ArtistRoute(route.artistId)) },
                    onBack = { backStack.removeLastOrNull() },
                )
            }

            entry<EditLabelRoute> {
                EditLabelScreen(onClose = { backStack.removeLastOrNull() })
            }
        },
    )
}

private fun NavBackStack<NavKey>.replaceAll(route: NavKey) {
    clear()
    add(route)
}

private fun NavBackStack<NavKey>.replaceTop(route: NavKey) {
    removeLastOrNull()
    add(route)
}
