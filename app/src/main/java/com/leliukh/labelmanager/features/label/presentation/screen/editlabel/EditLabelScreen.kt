package com.leliukh.labelmanager.features.label.presentation.screen.editlabel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.contract.rules.ContractLimits
import com.leliukh.labelmanager.core.countries.presentation.CountryPickerSheet
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.Banner
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.BannerTone
import com.leliukh.labelmanager.core.design_system.presentation.components.feedback.InfoNote
import com.leliukh.labelmanager.core.design_system.presentation.components.field.AppTextField
import com.leliukh.labelmanager.core.design_system.presentation.components.field.SelectField
import com.leliukh.labelmanager.core.design_system.presentation.components.navigation.AppTopBar
import com.leliukh.labelmanager.core.design_system.presentation.components.navigation.TopBarTextAction
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppTheme
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper
import com.leliukh.labelmanager.core.ui_utils.presentation.CollectEvents
import com.leliukh.labelmanager.features.label.presentation.viewmodel.editlabel.EditLabelViewAction
import com.leliukh.labelmanager.features.label.presentation.viewmodel.editlabel.EditLabelViewEvent
import com.leliukh.labelmanager.features.label.presentation.viewmodel.editlabel.EditLabelViewModel
import com.leliukh.labelmanager.features.label.presentation.viewmodel.editlabel.EditLabelViewState

/**
 * Edit label: name and country of the label. Budget and value are not editable and say so.
 *
 * @param onClose called after saving and by the back button
 * @param viewModel state holder of the screen
 */
@Composable
fun EditLabelScreen(
    onClose: () -> Unit,
    viewModel: EditLabelViewModel = hiltViewModel(),
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()

    CollectEvents(viewModel.uiEventsFlow) { event ->
        when (event) {
            EditLabelViewEvent.Close -> onClose()
        }
    }

    EditLabelContent(state = state, onAction = viewModel::handleUiAction)
}

@Composable
private fun EditLabelContent(
    state: EditLabelViewState,
    onAction: (EditLabelViewAction) -> Unit,
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
            onBack = { onAction(EditLabelViewAction.Back) },
            title = stringResource(R.string.edit_label_title),
            actions = {
                TopBarTextAction(
                    text = stringResource(R.string.common_save),
                    onClick = { onAction(EditLabelViewAction.Save) },
                    enabled = state.isSaveEnabled,
                    isLoading = state.isSaving,
                )
            },
        )

        if (!state.isLoading) {
            EditLabelForm(
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
                onAction(EditLabelViewAction.CountryChosen(code))
                isPickerOpen = false
            },
            onDismiss = { isPickerOpen = false },
        )
    }
}

@Composable
private fun EditLabelForm(
    state: EditLabelViewState,
    onAction: (EditLabelViewAction) -> Unit,
    onOpenPicker: () -> Unit,
) {
    val colors = AppThemeWrapper.colors

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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(colors.surface, AppThemeWrapper.shapes.card),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = state.flag, fontSize = 44.sp)
            }

            Text(
                text = stringResource(R.string.edit_label_caption),
                style = AppThemeWrapper.typography.subhead,
                color = colors.textSecondary,
            )
        }

        if (state.errorRes != null) {
            Banner(message = stringResource(state.errorRes), tone = BannerTone.Error)
        }

        AppTextField(
            value = state.name,
            onValueChange = { onAction(EditLabelViewAction.NameChanged(it)) },
            label = stringResource(R.string.edit_label_name_label),
            hint = "${state.name.trim().length} / ${ContractLimits.LABEL_NAME_MAX_LENGTH}",
            error = if (state.isNameInvalid) {
                stringResource(
                    R.string.edit_label_name_invalid,
                    ContractLimits.LABEL_NAME_MIN_LENGTH,
                    ContractLimits.LABEL_NAME_MAX_LENGTH,
                )
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done,
            ),
        )

        SelectField(
            label = stringResource(R.string.common_country),
            value = state.countryName,
            emoji = state.flag,
            onClick = onOpenPicker,
        )

        InfoNote(text = stringResource(R.string.edit_label_note))
    }
}

@Preview(name = "Edit label", widthDp = 412, heightDp = 917)
@Composable
private fun EditLabelPreview() {
    AppTheme(darkTheme = false) {
        EditLabelContent(state = previewState, onAction = {})
    }
}

@Preview(name = "Edit label, invalid name, dark", widthDp = 412, heightDp = 917)
@Composable
private fun EditLabelInvalidPreview() {
    AppTheme(darkTheme = true) {
        EditLabelContent(
            state = previewState.copy(name = "R", isNameInvalid = true, isSaveEnabled = false),
            onAction = {},
        )
    }
}

@Preview(name = "Edit label, saving", widthDp = 412, heightDp = 917)
@Composable
private fun EditLabelSavingPreview() {
    AppTheme(darkTheme = false) {
        EditLabelContent(
            state = previewState.copy(isSaving = true, isSaveEnabled = false),
            onAction = {},
        )
    }
}

@Preview(name = "Edit label, error", widthDp = 412, heightDp = 917)
@Composable
private fun EditLabelErrorPreview() {
    AppTheme(darkTheme = false) {
        EditLabelContent(
            state = previewState.copy(errorRes = R.string.common_error_network),
            onAction = {},
        )
    }
}

private val previewState = EditLabelViewState(
    isLoading = false,
    name = "Rhein Valley Records",
    countryCode = "DE",
    countryName = "Germany",
    flag = "🇩🇪",
    isSaveEnabled = true,
)
