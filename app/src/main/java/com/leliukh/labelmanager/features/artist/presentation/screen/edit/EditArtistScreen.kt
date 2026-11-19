package com.leliukh.labelmanager.features.artist.presentation.screen.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.contract.rules.ContractLimits
import com.leliukh.labelmanager.core.countries.presentation.CountryPickerSheet
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.Banner
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.BannerTone
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.EmptyState
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.InfoNote
import com.leliukh.labelmanager.core.design_system.presentation.components.field.AppTextField
import com.leliukh.labelmanager.core.design_system.presentation.components.field.SelectField
import com.leliukh.labelmanager.core.design_system.presentation.components.navigation.AppTopBar
import com.leliukh.labelmanager.core.design_system.presentation.components.navigation.TopBarTextAction
import com.leliukh.labelmanager.core.design_system.presentation.components.artist.ArtistAvatar
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppTheme
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper
import com.leliukh.labelmanager.core.formatting.presentation.formatMoney
import com.leliukh.labelmanager.core.ui_utils.presentation.CollectEvents
import com.leliukh.labelmanager.features.artist.presentation.viewmodel.edit.EditArtistViewAction
import com.leliukh.labelmanager.features.artist.presentation.viewmodel.edit.EditArtistViewEvent
import com.leliukh.labelmanager.features.artist.presentation.viewmodel.edit.EditArtistViewModel
import com.leliukh.labelmanager.features.artist.presentation.viewmodel.edit.EditArtistViewState
import com.leliukh.labelmanager.features.label.domain.models.Genre
import com.leliukh.labelmanager.features.label.presentation.models.colors
import com.leliukh.labelmanager.features.label.presentation.models.nameRes
import com.leliukh.labelmanager.features.label.presentation.models.shortLabelRes

/**
 * Edit artist: first and last name and nationality. Age, genre and value are set by the
 * game and the screen says so.
 *
 * @param artistId id of the artist
 * @param onClose called after saving and by the back button
 */
@Composable
fun EditArtistScreen(
    artistId: String,
    onClose: () -> Unit,
) {
    val viewModel = hiltViewModel<EditArtistViewModel, EditArtistViewModel.Factory>(
        creationCallback = { factory -> factory.create(artistId) },
    )
    val state by viewModel.viewState.collectAsStateWithLifecycle()

    CollectEvents(viewModel.uiEventsFlow) { event ->
        when (event) {
            EditArtistViewEvent.Close -> onClose()
        }
    }

    EditArtistContent(state = state, onAction = viewModel::handleUiAction)
}

@Composable
private fun EditArtistContent(
    state: EditArtistViewState,
    onAction: (EditArtistViewAction) -> Unit,
) {
    var isPickerOpen by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppThemeWrapper.colors.background)
            .statusBarsPadding()
            .imePadding(),
    ) {
        AppTopBar(
            onBack = { onAction(EditArtistViewAction.Back) },
            title = stringResource(R.string.edit_artist_title),
            actions = {
                TopBarTextAction(
                    text = stringResource(R.string.common_save),
                    onClick = { onAction(EditArtistViewAction.Save) },
                    enabled = state.isSaveEnabled,
                    isLoading = state.isSaving,
                )
            },
        )

        when {
            state.isNotFound -> EmptyState(
                icon = AppIcons.Info,
                title = stringResource(R.string.edit_artist_title),
                message = stringResource(R.string.edit_artist_not_found),
                modifier = Modifier.padding(top = 120.dp),
            )

            !state.isLoading -> EditArtistForm(
                state = state,
                onAction = onAction,
                onOpenPicker = { isPickerOpen = true },
            )
        }
    }

    if (isPickerOpen) {
        CountryPickerSheet(
            selectedCode = state.countryCode,
            onSelect = { code ->
                onAction(EditArtistViewAction.CountryChosen(code))
                isPickerOpen = false
            },
            onDismiss = { isPickerOpen = false },
        )
    }
}

@Composable
private fun EditArtistForm(
    state: EditArtistViewState,
    onAction: (EditArtistViewAction) -> Unit,
    onOpenPicker: () -> Unit,
) {
    val nameError = stringResource(
        R.string.edit_artist_name_invalid,
        ContractLimits.ARTIST_NAME_MAX_LENGTH,
    )
    val nameKeyboard = KeyboardOptions(
        capitalization = KeyboardCapitalization.Words,
        imeAction = ImeAction.Next,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ArtistAvatar(
                initials = state.initials,
                badge = stringResource(state.genre.shortLabelRes),
                colors = state.genre.colors,
                size = 84.dp,
                badgeBorder = AppThemeWrapper.colors.background,
            )

            Text(
                text = stringResource(
                    R.string.edit_artist_summary,
                    stringResource(state.genre.nameRes),
                    pluralStringResource(R.plurals.artist_age, state.age, state.age),
                    formatMoney(state.marketValue),
                ),
                style = AppThemeWrapper.typography.subhead,
                color = AppThemeWrapper.colors.textSecondary,
            )
        }

        if (state.errorRes != null) {
            Banner(message = stringResource(state.errorRes), tone = BannerTone.Error)
        }

        AppTextField(
            value = state.firstName,
            onValueChange = { onAction(EditArtistViewAction.FirstNameChanged(it)) },
            label = stringResource(R.string.edit_artist_first_name),
            error = if (state.isFirstNameInvalid) nameError else null,
            keyboardOptions = nameKeyboard,
        )

        AppTextField(
            value = state.lastName,
            onValueChange = { onAction(EditArtistViewAction.LastNameChanged(it)) },
            label = stringResource(R.string.edit_artist_last_name),
            error = if (state.isLastNameInvalid) nameError else null,
            keyboardOptions = nameKeyboard.copy(imeAction = ImeAction.Done),
        )

        SelectField(
            label = stringResource(R.string.common_country),
            value = state.countryName,
            emoji = state.flag,
            onClick = onOpenPicker,
        )

        InfoNote(text = stringResource(R.string.edit_artist_note))
    }
}

@Preview(name = "Edit artist", widthDp = 412, heightDp = 917)
@Composable
private fun EditArtistPreview() {
    AppTheme(darkTheme = false) {
        EditArtistContent(state = previewState, onAction = {})
    }
}

@Preview(name = "Edit artist, invalid, dark", widthDp = 412, heightDp = 917)
@Composable
private fun EditArtistInvalidPreview() {
    AppTheme(darkTheme = true) {
        EditArtistContent(
            state = previewState.copy(
                lastName = "",
                isLastNameInvalid = true,
                isSaveEnabled = false,
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Edit artist, saving", widthDp = 412, heightDp = 917)
@Composable
private fun EditArtistSavingPreview() {
    AppTheme(darkTheme = false) {
        EditArtistContent(
            state = previewState.copy(isSaving = true, isSaveEnabled = false),
            onAction = {},
        )
    }
}

private val previewState = EditArtistViewState(
    isLoading = false,
    initials = "TM",
    genre = Genre.Rock,
    age = 22,
    marketValue = 1_450_000,
    firstName = "Théo",
    lastName = "Marchand-Roux",
    countryCode = "FR",
    countryName = "France",
    flag = "🇫🇷",
    isSaveEnabled = true,
)
