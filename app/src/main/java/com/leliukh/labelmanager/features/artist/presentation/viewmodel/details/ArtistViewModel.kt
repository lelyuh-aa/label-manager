package com.leliukh.labelmanager.features.artist.presentation.viewmodel.details

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.contract.dto.error.ErrorCodeDto
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.network.domain.onFailure
import com.leliukh.labelmanager.core.network.domain.onSuccess
import com.leliukh.labelmanager.core.network.domain.serverCode
import com.leliukh.labelmanager.core.ui_utils.presentation.messageRes
import com.leliukh.labelmanager.features.market.domain.interactor.MarketInteractor
import com.leliukh.labelmanager.features.market.presentation.mapper.toRoute
import com.leliukh.labelmanager.features.market.presentation.models.BuyConfirmVO
import com.leliukh.labelmanager.features.market.presentation.models.MarketListingVO
import com.leliukh.labelmanager.features.market.presentation.navigation.DealDoneRoute
import com.leliukh.labelmanager.features.artist.domain.interactor.ArtistInteractor
import com.leliukh.labelmanager.features.artist.presentation.mapper.toProfileVO
import com.leliukh.labelmanager.features.artist.presentation.models.ArtistProfileVO
import com.leliukh.labelmanager.features.label.domain.interactor.LabelInteractor
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Artist profile: the artist, their label, value and deal history, with the market action
 * that fits: list or withdraw an own artist, buy another label's listed artist.
 *
 * The profile is loaded every time the screen comes to the foreground, so it shows the new
 * name or listing right after another screen closes. A reload keeps the shown profile until
 * the new one arrives; only the first load shows a skeleton or an error.
 *
 * @param artistId id of the artist
 * @param artistInteractor source of the profile
 * @param marketInteractor buys and withdraws listings
 * @param labelInteractor the user's label, for the budget
 */
@HiltViewModel(assistedFactory = ArtistViewModel.Factory::class)
class ArtistViewModel @AssistedInject constructor(
    @Assisted private val artistId: String,
    private val artistInteractor: ArtistInteractor,
    private val marketInteractor: MarketInteractor,
    private val labelInteractor: LabelInteractor,
) : ViewModel() {

    private var loadJob: Job? = null
    private val mutableState = MutableStateFlow(ArtistViewState())
    private val events = Channel<ArtistViewEvent>(Channel.BUFFERED)

    val viewState: StateFlow<ArtistViewState> = mutableState.asStateFlow()

    val uiEventsFlow: Flow<ArtistViewEvent> = events.receiveAsFlow()

    /**
     * @param action what the user did
     */
    fun handleUiAction(action: ArtistViewAction) {
        when (action) {
            ArtistViewAction.ScreenShown, ArtistViewAction.Retry -> load()

            ArtistViewAction.EditClicked -> events.trySend(ArtistViewEvent.OpenEdit(artistId))

            ArtistViewAction.PutOnListClicked ->
                events.trySend(ArtistViewEvent.OpenCreateListing(artistId))

            ArtistViewAction.RemoveFromListClicked -> withdraw()

            ArtistViewAction.BuyClicked -> openBuy()

            ArtistViewAction.ConfirmPurchase -> purchase()

            ArtistViewAction.DismissBuy -> mutableState.update { it.copy(buyConfirm = null) }

            ArtistViewAction.SellClicked -> {
                mutableState.update { it.copy(buyConfirm = null) }
                events.trySend(ArtistViewEvent.OpenSellArtist)
            }

            ArtistViewAction.Back -> events.trySend(ArtistViewEvent.Close)
        }
    }

    private fun load() {
        if (loadJob?.isActive == true) return
        mutableState.update { it.copy(isError = false, isLoading = it.profile == null) }
        loadJob = viewModelScope.launch {
            val budget = labelInteractor.cachedLabel()?.label?.budget
            artistInteractor.getArtist(artistId)
                .onSuccess { details ->
                    val profile = details.toProfileVO()
                    mutableState.update {
                        it.copy(isLoading = false, profile = profile, budget = budget)
                    }
                }
                .onFailure {
                    mutableState.update { it.copy(isLoading = false, isError = it.profile == null) }
                }
        }
    }

    private fun withdraw() {
        val listing = mutableState.value.profile?.listing ?: return
        if (mutableState.value.isWorking) return
        mutableState.update { it.copy(isWorking = true, errorRes = null) }
        viewModelScope.launch {
            val result = marketInteractor.withdrawListing(listing.listingId)
            val errorRes = (result as? ApiResult.Failure)?.error?.messageRes
            mutableState.update { it.copy(isWorking = false, errorRes = errorRes) }
            load()
        }
    }

    private fun openBuy() {
        val state = mutableState.value
        val profile = state.profile ?: return
        val listing = profile.listing ?: return
        val budget = state.budget ?: return
        val marketListing = MarketListingVO(
            listingId = listing.listingId,
            artistId = profile.artistId,
            fullName = "${profile.firstName} ${profile.lastName}",
            initials = profile.initials,
            genre = profile.genre,
            flag = profile.flag,
            countryName = profile.countryName,
            age = profile.age,
            sellerName = profile.labelName,
            askingPrice = listing.askingPrice,
            listedAt = listing.listedAt,
            isMine = profile.isMine,
        )
        mutableState.update { it.copy(buyConfirm = BuyConfirmVO(marketListing, budget)) }
    }

    private fun purchase() {
        val confirm = mutableState.value.buyConfirm ?: return
        if (confirm.isBuying || !confirm.canAfford) return
        mutableState.update { it.copy(buyConfirm = confirm.copy(isBuying = true, errorRes = null)) }
        viewModelScope.launch {
            when (val result = marketInteractor.purchase(confirm.listing.listingId)) {
                is ApiResult.Success -> {
                    mutableState.update { it.copy(buyConfirm = null) }
                    events.trySend(ArtistViewEvent.OpenDealDone(result.data.toRoute()))
                }

                is ApiResult.Failure -> {
                    val isGone = result.error.serverCode == ErrorCodeDto.LISTING_NOT_AVAILABLE
                    val errorRes = if (isGone) R.string.market_buy_gone else result.error.messageRes
                    mutableState.update {
                        it.copy(buyConfirm = confirm.copy(isBuying = false, errorRes = errorRes))
                    }
                    if (isGone) load()
                }
            }
        }
    }

    /** Creates the view model for one artist. */
    @AssistedFactory
    interface Factory {

        /** @param artistId id of the artist */
        fun create(artistId: String): ArtistViewModel
    }
}

/**
 * @property isLoading whether the first load is running
 * @property isError whether the first load failed
 * @property profile the loaded profile
 * @property budget the user's budget, for buying; null when unknown
 * @property isWorking whether a listing is being withdrawn
 * @property errorRes why the last action failed
 * @property buyConfirm the open buy confirmation; null while it is closed
 */
data class ArtistViewState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val profile: ArtistProfileVO? = null,
    val budget: Long? = null,
    val isWorking: Boolean = false,
    @param:StringRes val errorRes: Int? = null,
    val buyConfirm: BuyConfirmVO? = null,
)

/** What the user does on the artist profile. */
sealed interface ArtistViewAction {

    /** The screen came to the foreground. */
    data object ScreenShown : ArtistViewAction

    data object Retry : ArtistViewAction

    data object EditClicked : ArtistViewAction

    data object PutOnListClicked : ArtistViewAction

    data object RemoveFromListClicked : ArtistViewAction

    data object BuyClicked : ArtistViewAction

    data object ConfirmPurchase : ArtistViewAction

    data object DismissBuy : ArtistViewAction

    data object SellClicked : ArtistViewAction

    data object Back : ArtistViewAction
}

/** One-off results of the artist profile. */
sealed interface ArtistViewEvent {

    /** @property artistId the artist to edit */
    data class OpenEdit(val artistId: String) : ArtistViewEvent

    /** @property artistId the artist to list */
    data class OpenCreateListing(val artistId: String) : ArtistViewEvent

    /** @property route what the deal-done moment shows */
    data class OpenDealDone(val route: DealDoneRoute) : ArtistViewEvent

    data object OpenSellArtist : ArtistViewEvent

    data object Close : ArtistViewEvent
}
