package com.leliukh.labelmanager.features.label.presentation.viewmodel.editlabel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leliukh.labelmanager.core.formatting.presentation.countryDisplayName
import com.leliukh.labelmanager.core.formatting.presentation.countryFlagEmoji
import com.leliukh.labelmanager.core.network.domain.onFailure
import com.leliukh.labelmanager.core.network.domain.onSuccess
import com.leliukh.labelmanager.core.ui_utils.presentation.messageRes
import com.leliukh.labelmanager.features.label.domain.interactor.LabelInteractor
import com.leliukh.labelmanager.features.label.domain.validation.isValidLabelName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val KEY_NAME = "name"
private const val KEY_COUNTRY = "country"

/**
 * Edit label: the label's name and country, saved together.
 *
 * The form starts from the cached label; the edited values survive process death. Save is
 * enabled once something changed and the name is valid; a successful save updates the cache
 * and closes the screen.
 *
 * @param labelInteractor the signed-in user's label
 * @param savedStateHandle keeps the edited values
 */
@HiltViewModel
class EditLabelViewModel @Inject constructor(
    private val labelInteractor: LabelInteractor,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private var originalName = ""
    private var originalCountry = ""
    private val mutableState = MutableStateFlow(EditLabelViewState())
    private val events = Channel<EditLabelViewEvent>(Channel.BUFFERED)

    val viewState: StateFlow<EditLabelViewState> = mutableState.asStateFlow()

    val uiEventsFlow: Flow<EditLabelViewEvent> = events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val label = labelInteractor.cachedLabel()?.label ?: run {
                events.trySend(EditLabelViewEvent.Close)
                return@launch
            }
            originalName = label.name
            originalCountry = label.countryCode
            mutableState.value = recompute(
                EditLabelViewState(
                    isLoading = false,
                    name = savedStateHandle[KEY_NAME] ?: label.name,
                    countryCode = savedStateHandle[KEY_COUNTRY] ?: label.countryCode,
                ),
            )
        }
    }

    /**
     * @param action what the user did
     */
    fun handleUiAction(action: EditLabelViewAction) {
        when (action) {
            is EditLabelViewAction.NameChanged -> {
                savedStateHandle[KEY_NAME] = action.name
                updateState { copy(name = action.name, errorRes = null) }
            }

            is EditLabelViewAction.CountryChosen -> {
                savedStateHandle[KEY_COUNTRY] = action.countryCode
                updateState { copy(countryCode = action.countryCode, errorRes = null) }
            }

            EditLabelViewAction.Save -> save()

            EditLabelViewAction.Back -> events.trySend(EditLabelViewEvent.Close)
        }
    }

    private fun save() {
        val state = mutableState.value
        if (!state.isSaveEnabled) return
        updateState { copy(isSaving = true, errorRes = null) }
        viewModelScope.launch {
            labelInteractor.updateLabel(state.name, state.countryCode)
                .onSuccess { events.trySend(EditLabelViewEvent.Close) }
                .onFailure { error ->
                    updateState { copy(isSaving = false, errorRes = error.messageRes) }
                }
        }
    }

    private fun updateState(transform: EditLabelViewState.() -> EditLabelViewState) {
        mutableState.update { recompute(it.transform()) }
    }

    private fun recompute(state: EditLabelViewState): EditLabelViewState {
        val isNameValid = isValidLabelName(state.name)
        val isChanged = state.name.trim() != originalName || state.countryCode != originalCountry
        return state.copy(
            countryName = countryDisplayName(state.countryCode),
            flag = countryFlagEmoji(state.countryCode),
            isNameInvalid = !state.isLoading && !isNameValid,
            isSaveEnabled = !state.isLoading && !state.isSaving && isNameValid && isChanged,
        )
    }
}

/**
 * @property isLoading whether the label is still being read from the cache
 * @property name the label name as typed
 * @property countryCode the chosen country
 * @property countryName localised name of [countryCode]
 * @property flag flag emoji of [countryCode]
 * @property isNameInvalid whether [name] is too short or too long
 * @property isSaveEnabled whether Save can be tapped
 * @property isSaving whether the save request is running
 * @property errorRes why the last save failed
 */
data class EditLabelViewState(
    val isLoading: Boolean = true,
    val name: String = "",
    val countryCode: String = "",
    val countryName: String = "",
    val flag: String = "",
    val isNameInvalid: Boolean = false,
    val isSaveEnabled: Boolean = false,
    val isSaving: Boolean = false,
    @param:StringRes val errorRes: Int? = null,
)

/** What the user does on Edit label. */
sealed interface EditLabelViewAction {

    /** @property name the new text of the name field */
    data class NameChanged(val name: String) : EditLabelViewAction

    /** @property countryCode the country picked in the sheet */
    data class CountryChosen(val countryCode: String) : EditLabelViewAction

    data object Save : EditLabelViewAction

    data object Back : EditLabelViewAction
}

/** One-off results of Edit label. */
sealed interface EditLabelViewEvent {

    data object Close : EditLabelViewEvent
}
