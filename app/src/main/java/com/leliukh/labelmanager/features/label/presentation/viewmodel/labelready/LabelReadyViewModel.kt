package com.leliukh.labelmanager.features.label.presentation.viewmodel.labelready

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leliukh.labelmanager.core.formatting.presentation.countryDisplayName
import com.leliukh.labelmanager.core.formatting.presentation.countryFlagEmoji
import com.leliukh.labelmanager.core.network.domain.onFailure
import com.leliukh.labelmanager.core.network.domain.onSuccess
import com.leliukh.labelmanager.features.label.domain.models.Genre
import com.leliukh.labelmanager.features.label.domain.models.Label
import com.leliukh.labelmanager.features.label.domain.repository.LabelRepository
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

/**
 * The one-time "your label is ready" moment after sign-up: the generated label, its budget and
 * the roster by genre.
 *
 * @param labelRepository source of the new label
 */
@HiltViewModel
class LabelReadyViewModel @Inject constructor(
    private val labelRepository: LabelRepository,
) : ViewModel() {

    private val mutableState = MutableStateFlow(LabelReadyViewState())
    private val events = Channel<LabelReadyViewEvent>(Channel.BUFFERED)

    val viewState: StateFlow<LabelReadyViewState> = mutableState.asStateFlow()

    val uiEventsFlow: Flow<LabelReadyViewEvent> = events.receiveAsFlow()

    init {
        load()
    }

    /**
     * @param action what the user did
     */
    fun handleUiAction(action: LabelReadyViewAction) {
        when (action) {
            LabelReadyViewAction.Retry -> load()

            LabelReadyViewAction.Continue -> events.trySend(LabelReadyViewEvent.OpenHome)
        }
    }

    private fun load() {
        mutableState.update { it.copy(isLoading = true, isError = false) }
        viewModelScope.launch {
            labelRepository.refreshLabel()
                .onSuccess { label -> mutableState.value = label.toViewState() }
                .onFailure { mutableState.update { it.copy(isLoading = false, isError = true) } }
        }
    }
}

/**
 * @property isLoading whether the label is still loading
 * @property isError whether loading failed
 * @property labelName name of the label
 * @property countryName localised name of the label's country
 * @property flag flag emoji of the label's country
 * @property budget budget in whole US dollars
 * @property rosterValue sum of the artists' values
 * @property artistCount number of artists
 * @property genres number of artists per genre, in playing order
 */
data class LabelReadyViewState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val labelName: String = "",
    val countryName: String = "",
    val flag: String = "",
    val budget: Long = 0,
    val rosterValue: Long = 0,
    val artistCount: Int = 0,
    val genres: List<GenreCountVO> = emptyList(),
)

/**
 * @property genre the genre
 * @property count artists in it
 */
data class GenreCountVO(
    val genre: Genre,
    val count: Int,
)

/** What the user does on the label-ready screen. */
sealed interface LabelReadyViewAction {

    data object Retry : LabelReadyViewAction

    data object Continue : LabelReadyViewAction
}

/** One-off results of the label-ready screen. */
sealed interface LabelReadyViewEvent {

    data object OpenHome : LabelReadyViewEvent
}

private fun Label.toViewState(): LabelReadyViewState {
    val counts = artists.groupingBy { it.genre }.eachCount()
    return LabelReadyViewState(
        isLoading = false,
        labelName = name,
        countryName = countryDisplayName(countryCode),
        flag = countryFlagEmoji(countryCode),
        budget = budget,
        rosterValue = value,
        artistCount = artists.size,
        genres = Genre.entries.map { GenreCountVO(it, counts[it] ?: 0) },
    )
}
