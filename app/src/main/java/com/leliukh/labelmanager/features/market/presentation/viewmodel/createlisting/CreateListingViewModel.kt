package com.leliukh.labelmanager.features.market.presentation.viewmodel.createlisting

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.contract.dto.error.ErrorCodeDto
import com.leliukh.labelmanager.contract.rules.ContractLimits
import com.leliukh.labelmanager.core.formatting.presentation.countryDisplayName
import com.leliukh.labelmanager.core.formatting.presentation.countryFlagEmoji
import com.leliukh.labelmanager.core.network.domain.onFailure
import com.leliukh.labelmanager.core.network.domain.onSuccess
import com.leliukh.labelmanager.core.network.domain.serverCode
import com.leliukh.labelmanager.core.ui_utils.presentation.messageRes
import com.leliukh.labelmanager.features.market.domain.interactor.MarketInteractor
import com.leliukh.labelmanager.features.label.domain.interactor.LabelInteractor
import com.leliukh.labelmanager.features.label.domain.models.Genre
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
import kotlinx.datetime.LocalDate

private const val KEY_PRICE = "price"
private const val MAX_PRICE_DIGITS = 10

/** Markups offered as quick prices, in percent above the market value. */
private val QUICK_MARKUPS = listOf(0, 10, 25, 50)

/**
 * Sell an artist, step two: the asking price. Starts at the market value; quick prices add a
 * markup; the price is compared with the value as it is typed. Listing closes the sell flow.
 *
 * @param artistId id of the artist
 * @param marketInteractor creates the listing
 * @param labelInteractor the cached roster the artist comes from
 * @param savedStateHandle keeps the typed price
 */
@HiltViewModel(assistedFactory = CreateListingViewModel.Factory::class)
class CreateListingViewModel @AssistedInject constructor(
    @Assisted private val artistId: String,
    private val marketInteractor: MarketInteractor,
    private val labelInteractor: LabelInteractor,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val mutableState = MutableStateFlow(CreateListingViewState())
    private val events = Channel<CreateListingViewEvent>(Channel.BUFFERED)

    val viewState: StateFlow<CreateListingViewState> = mutableState.asStateFlow()

    val uiEventsFlow: Flow<CreateListingViewEvent> = events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val artist = labelInteractor.cachedLabel()?.label?.artists?.find { it.id == artistId }
            if (artist == null || artist.listing != null) {
                events.trySend(CreateListingViewEvent.Close(isListed = false))
                return@launch
            }
            val price = savedStateHandle[KEY_PRICE] ?: artist.marketValue
            mutableState.value = CreateListingViewState(
                isLoading = false,
                fullName = "${artist.firstName} ${artist.lastName}",
                initials = artist.initials,
                genre = artist.genre,
                flag = countryFlagEmoji(artist.countryCode),
                countryName = countryDisplayName(artist.countryCode),
                age = artist.age,
                birthDate = artist.birthDate,
                marketValue = artist.marketValue,
                quickPrices = QUICK_MARKUPS.map { markup ->
                    QuickPriceVO(markup, artist.marketValue * (100 + markup) / 100)
                },
            ).withPrice(price)
        }
    }

    /**
     * @param action what the user did
     */
    fun handleUiAction(action: CreateListingViewAction) {
        when (action) {
            is CreateListingViewAction.PriceTyped -> {
                val digits = action.text.filter(Char::isDigit).take(MAX_PRICE_DIGITS)
                setPrice(digits.toLongOrNull())
            }

            is CreateListingViewAction.QuickPriceChosen -> setPrice(action.price)

            CreateListingViewAction.Submit -> submit()

            CreateListingViewAction.Cancel ->
                events.trySend(CreateListingViewEvent.Close(isListed = false))
        }
    }

    private fun setPrice(price: Long?) {
        savedStateHandle[KEY_PRICE] = price
        mutableState.update { it.withPrice(price).copy(errorRes = null) }
    }

    private fun submit() {
        val state = mutableState.value
        val price = state.price ?: return
        if (!state.isPriceValid || state.isSubmitting) return
        mutableState.update { it.copy(isSubmitting = true, errorRes = null) }
        viewModelScope.launch {
            marketInteractor.createListing(artistId, price)
                .onSuccess { events.trySend(CreateListingViewEvent.Close(isListed = true)) }
                .onFailure { error ->
                    val errorRes = if (error.serverCode == ErrorCodeDto.ARTIST_ALREADY_LISTED) {
                        R.string.create_listing_already_listed
                    } else {
                        error.messageRes
                    }
                    mutableState.update { it.copy(isSubmitting = false, errorRes = errorRes) }
                }
        }
    }

    /** Creates the view model for one artist. */
    @AssistedFactory
    interface Factory {

        /** @param artistId id of the artist */
        fun create(artistId: String): CreateListingViewModel
    }
}

/**
 * @property isLoading whether the artist is still being read from the cache
 * @property fullName first and last name
 * @property initials letters for the avatar
 * @property genre playing genre
 * @property flag flag emoji of the nationality
 * @property countryName localised nationality
 * @property age age in full years
 * @property birthDate date of birth
 * @property marketValue market value in whole US dollars
 * @property quickPrices prices offered with one tap
 * @property price the asking price; null while the field is empty
 * @property isPriceValid whether [price] is within the limits of the API
 * @property isSubmitting whether the listing request is running
 * @property errorRes why the last attempt failed
 */
data class CreateListingViewState(
    val isLoading: Boolean = true,
    val fullName: String = "",
    val initials: String = "",
    val genre: Genre = Genre.Pop,
    val flag: String = "",
    val countryName: String = "",
    val age: Int = 0,
    val birthDate: LocalDate = LocalDate(2000, 1, 1),
    val marketValue: Long = 0,
    val quickPrices: List<QuickPriceVO> = emptyList(),
    val price: Long? = null,
    val isPriceValid: Boolean = false,
    val isSubmitting: Boolean = false,
    @param:StringRes val errorRes: Int? = null,
) {

    /** The price minus the market value; positive above the value. */
    val difference: Long
        get() = (price ?: 0) - marketValue

    /**
     * @param price the new asking price
     */
    fun withPrice(price: Long?): CreateListingViewState = copy(
        price = price,
        isPriceValid = price != null &&
            price in ContractLimits.MIN_ASKING_PRICE..ContractLimits.MAX_ASKING_PRICE,
    )
}

/**
 * A price offered with one tap.
 *
 * @property markupPercent how far above the market value, in percent; zero for the value itself
 * @property price the price
 */
data class QuickPriceVO(
    val markupPercent: Int,
    val price: Long,
)

/** What the user does when setting the asking price. */
sealed interface CreateListingViewAction {

    /** @property text the new text of the price field */
    data class PriceTyped(val text: String) : CreateListingViewAction

    /** @property price the tapped quick price */
    data class QuickPriceChosen(val price: Long) : CreateListingViewAction

    data object Submit : CreateListingViewAction

    data object Cancel : CreateListingViewAction
}

/** One-off results of setting the asking price. */
sealed interface CreateListingViewEvent {

    /** @property isListed whether the artist was put on the market */
    data class Close(val isListed: Boolean) : CreateListingViewEvent
}
