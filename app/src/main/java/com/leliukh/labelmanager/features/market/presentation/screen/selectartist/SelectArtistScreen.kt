package com.leliukh.labelmanager.features.market.presentation.screen.selectartist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.EmptyState
import com.leliukh.labelmanager.core.design_system.presentation.components.field.SearchField
import com.leliukh.labelmanager.core.design_system.presentation.components.list.GroupPosition
import com.leliukh.labelmanager.core.design_system.presentation.components.list.SectionHeader
import com.leliukh.labelmanager.core.design_system.presentation.components.navigation.AppTopBar
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppTheme
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper
import com.leliukh.labelmanager.core.ui_utils.presentation.CollectEvents
import com.leliukh.labelmanager.features.market.presentation.viewmodel.selectartist.SelectArtistViewAction
import com.leliukh.labelmanager.features.market.presentation.viewmodel.selectartist.SelectArtistViewEvent
import com.leliukh.labelmanager.features.market.presentation.viewmodel.selectartist.SelectArtistViewModel
import com.leliukh.labelmanager.features.market.presentation.viewmodel.selectartist.SelectArtistViewState
import com.leliukh.labelmanager.features.label.domain.models.Genre
import com.leliukh.labelmanager.features.label.presentation.models.RosterArtistVO
import com.leliukh.labelmanager.features.label.presentation.models.RosterSectionVO
import com.leliukh.labelmanager.features.label.presentation.models.titleRes
import com.leliukh.labelmanager.features.label.presentation.screen.components.RosterArtistRow

/**
 * Sell an artist, step one: pick who goes on the market.
 *
 * @param onPick called with the picked artist's id
 * @param onBack called by the back button
 * @param viewModel state holder of the screen
 */
@Composable
fun SelectArtistScreen(
    onPick: (artistId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: SelectArtistViewModel = hiltViewModel(),
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()

    CollectEvents(viewModel.uiEventsFlow) { event ->
        when (event) {
            is SelectArtistViewEvent.OpenCreateListing -> onPick(event.artistId)

            SelectArtistViewEvent.Close -> onBack()
        }
    }

    SelectArtistContent(state = state, onAction = viewModel::handleUiAction)
}

@Composable
private fun SelectArtistContent(
    state: SelectArtistViewState,
    onAction: (SelectArtistViewAction) -> Unit,
) {
    val navigationBar = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppThemeWrapper.colors.background)
            .statusBarsPadding(),
    ) {
        AppTopBar(
            onBack = { onAction(SelectArtistViewAction.Back) },
            title = stringResource(R.string.select_artist_title),
        )

        LazyColumn(contentPadding = PaddingValues(bottom = navigationBar + 24.dp)) {
            item(key = "intro") {
                Text(
                    text = stringResource(R.string.select_artist_intro),
                    style = AppThemeWrapper.typography.callout,
                    color = AppThemeWrapper.colors.textSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
            }

            item(key = "search") {
                SearchField(
                    query = state.query,
                    onQueryChange = { onAction(SelectArtistViewAction.QueryChanged(it)) },
                    placeholder = stringResource(R.string.select_artist_search),
                    modifier = Modifier
                        .padding(horizontal = AppThemeWrapper.dimens.space4)
                        .padding(top = 8.dp),
                )
            }

            if (!state.isLoading && state.sections.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        icon = AppIcons.Search,
                        title = stringResource(R.string.roster_no_results_title, state.query.trim()),
                        message = stringResource(R.string.roster_no_results_message),
                        modifier = Modifier.padding(top = 64.dp),
                    )
                }
            }

            state.sections.forEach { section ->
                item(key = "title-${section.genre}") {
                    SectionHeader(
                        title = stringResource(section.genre.titleRes),
                        count = section.artists.size.toString(),
                        modifier = Modifier.padding(top = 22.dp, bottom = 10.dp),
                    )
                }

                itemsIndexed(section.artists, key = { _, artist -> artist.id }) { index, artist ->
                    RosterArtistRow(
                        artist = artist,
                        position = GroupPosition.of(index, section.artists.size),
                        onClick = { onAction(SelectArtistViewAction.ArtistClicked(artist.id)) },
                        enabled = artist.askingPrice == null,
                    )
                }
            }
        }
    }
}

@Preview(name = "Select artist", widthDp = 412, heightDp = 917)
@Composable
private fun SelectArtistPreview() {
    AppTheme(darkTheme = false) {
        SelectArtistContent(
            state = SelectArtistViewState(
                isLoading = false,
                sections = listOf(
                    RosterSectionVO(
                        genre = Genre.Pop,
                        artists = listOf(
                            previewArtist("1", "Lukas Brenner", "LB", "🇩🇪", "Germany", null),
                            previewArtist("2", "Matteo Ricci", "MR", "🇮🇹", "Italy", null),
                            previewArtist("3", "Jonas Vermeulen", "JV", "🇧🇪", "Belgium", 1_600_000),
                        ),
                    ),
                ),
            ),
            onAction = {},
        )
    }
}

private fun previewArtist(
    id: String,
    name: String,
    initials: String,
    flag: String,
    country: String,
    askingPrice: Long?,
) = RosterArtistVO(
    id = id,
    fullName = name,
    initials = initials,
    genre = Genre.Pop,
    flag = flag,
    countryName = country,
    age = 27,
    marketValue = 1_000_000,
    valueRise = null,
    askingPrice = askingPrice,
)
