package com.leliukh.labelmanager.features.artist.presentation.viewmodel.edit

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leliukh.labelmanager.core.formatting.presentation.countryDisplayName
import com.leliukh.labelmanager.core.formatting.presentation.countryFlagEmoji
import com.leliukh.labelmanager.core.network.domain.onFailure
import com.leliukh.labelmanager.core.network.domain.onSuccess
import com.leliukh.labelmanager.core.ui_utils.presentation.messageRes
import com.leliukh.labelmanager.features.artist.domain.interactor.ArtistInteractor
import com.leliukh.labelmanager.features.label.domain.interactor.LabelInteractor
import com.leliukh.labelmanager.features.label.domain.models.Artist
import com.leliukh.labelmanager.features.label.domain.models.Genre
import com.leliukh.labelmanager.features.label.domain.validation.isValidArtistName
import com.leliukh.labelmanager.features.label.presentation.mapper.initials
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val KEY_FIRST_NAME = "firstName"
private const val KEY_LAST_NAME = "lastName"
private const val KEY_COUNTRY = "country"

/**
 * Edit artist: first name, last name and nationality of one of the user's artists.
 *
 * The form starts from the cached roster; the edited values survive process death. Save is
 * enabled once something changed and both names are valid; a successful save refreshes the
 * roster and closes the screen.
 *
 * @param artistId id of the artist
 * @param artistInteractor saves the edit
 * @param labelInteractor the cached roster the form starts from
 * @param savedStateHandle keeps the edited values
 */
@HiltViewModel(assistedFactory = EditArtistViewModel.Factory::class)
class EditArtistViewModel @AssistedInject constructor(
    @Assisted private val artistId: String,
    private val artistInteractor: ArtistInteractor,
    private val labelInteractor: LabelInteractor,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private var original: Artist? = null
    private val mutableState = MutableStateFlow(EditArtistViewState())
    private val events = Channel<EditArtistViewEvent>(Channel.BUFFERED)

    val viewState: StateFlow<EditArtistViewState> = mutableState.asStateFlow()

    val uiEventsFlow: Flow<EditArtistViewEvent> = events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val artist = labelInteractor.cachedLabel()?.label?.artists?.find { it.id == artistId }
            if (artist == null) {
                mutableState.value = EditArtistViewState(isLoading = false, isNotFound = true)
                return@launch
            }
            original = artist
            mutableState.value = recompute(
                EditArtistViewState(
                    isLoading = false,
                    initials = artist.initials,
                    genre = artist.genre,
                    age = artist.age,
                    marketValue = artist.marketValue,
                    firstName = savedStateHandle[KEY_FIRST_NAME] ?: artist.firstName,
                    lastName = savedStateHandle[KEY_LAST_NAME] ?: artist.lastName,
                    countryCode = savedStateHandle[KEY_COUNTRY] ?: artist.countryCode,
                ),
            )
        }
    }

    /**
     * @param action what the user did
     */
    fun handleUiAction(action: EditArtistViewAction) {
        when (action) {
            is EditArtistViewAction.FirstNameChanged -> {
                savedStateHandle[KEY_FIRST_NAME] = action.name
                updateState { copy(firstName = action.name, errorRes = null) }
            }

            is EditArtistViewAction.LastNameChanged -> {
                savedStateHandle[KEY_LAST_NAME] = action.name
                updateState { copy(lastName = action.name, errorRes = null) }
            }

            is EditArtistViewAction.CountryChosen -> {
                savedStateHandle[KEY_COUNTRY] = action.countryCode
                updateState { copy(countryCode = action.countryCode, errorRes = null) }
            }

            EditArtistViewAction.Save -> save()

            EditArtistViewAction.Back -> events.trySend(EditArtistViewEvent.Close)
        }
    }

    private fun save() {
        val state = mutableState.value
        if (!state.isSaveEnabled) return
        updateState { copy(isSaving = true, errorRes = null) }
        viewModelScope.launch {
            artistInteractor.updateArtist(
                artistId = artistId,
                firstName = state.firstName,
                lastName = state.lastName,
                countryCode = state.countryCode,
            )
                .onSuccess { events.trySend(EditArtistViewEvent.Close) }
                .onFailure { error ->
                    updateState { copy(isSaving = false, errorRes = error.messageRes) }
                }
        }
    }

    private fun updateState(transform: EditArtistViewState.() -> EditArtistViewState) {
        mutableState.update { recompute(it.transform()) }
    }

    private fun recompute(state: EditArtistViewState): EditArtistViewState {
        val artist = original ?: return state
        val isFirstValid = isValidArtistName(state.firstName)
        val isLastValid = isValidArtistName(state.lastName)
        val isChanged = state.firstName.trim() != artist.firstName ||
            state.lastName.trim() != artist.lastName ||
            state.countryCode != artist.countryCode
        return state.copy(
            countryName = countryDisplayName(state.countryCode),
            flag = countryFlagEmoji(state.countryCode),
            isFirstNameInvalid = !isFirstValid,
            isLastNameInvalid = !isLastValid,
            isSaveEnabled = !state.isSaving && isFirstValid && isLastValid && isChanged,
        )
    }

    /** Creates the view model for one artist. */
    @AssistedFactory
    interface Factory {

        /** @param artistId id of the artist */
        fun create(artistId: String): EditArtistViewModel
    }
}

/**
 * @property isLoading whether the artist is still being read from the cache
 * @property isNotFound whether the artist is not in the user's roster
 * @property initials letters for the avatar
 * @property genre playing genre
 * @property age age in full years
 * @property marketValue market value in whole US dollars
 * @property firstName the first name as typed
 * @property lastName the last name as typed
 * @property countryCode the chosen nationality
 * @property countryName localised name of [countryCode]
 * @property flag flag emoji of [countryCode]
 * @property isFirstNameInvalid whether [firstName] is empty or too long
 * @property isLastNameInvalid whether [lastName] is empty or too long
 * @property isSaveEnabled whether Save can be tapped
 * @property isSaving whether the save request is running
 * @property errorRes why the last save failed
 */
data class EditArtistViewState(
    val isLoading: Boolean = true,
    val isNotFound: Boolean = false,
    val initials: String = "",
    val genre: Genre = Genre.Pop,
    val age: Int = 0,
    val marketValue: Long = 0,
    val firstName: String = "",
    val lastName: String = "",
    val countryCode: String = "",
    val countryName: String = "",
    val flag: String = "",
    val isFirstNameInvalid: Boolean = false,
    val isLastNameInvalid: Boolean = false,
    val isSaveEnabled: Boolean = false,
    val isSaving: Boolean = false,
    @param:StringRes val errorRes: Int? = null,
)

/** What the user does on Edit artist. */
sealed interface EditArtistViewAction {

    /** @property name the new text of the first name field */
    data class FirstNameChanged(val name: String) : EditArtistViewAction

    /** @property name the new text of the last name field */
    data class LastNameChanged(val name: String) : EditArtistViewAction

    /** @property countryCode the country picked in the sheet */
    data class CountryChosen(val countryCode: String) : EditArtistViewAction

    data object Save : EditArtistViewAction

    data object Back : EditArtistViewAction
}

/** One-off results of Edit artist. */
sealed interface EditArtistViewEvent {

    data object Close : EditArtistViewEvent
}
