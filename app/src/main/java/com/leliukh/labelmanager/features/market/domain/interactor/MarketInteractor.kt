package com.leliukh.labelmanager.features.market.domain.interactor

import androidx.paging.PagingData
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.network.domain.onSuccess
import com.leliukh.labelmanager.core.session.domain.repository.SessionRepository
import com.leliukh.labelmanager.features.activity.domain.interactor.ActivityInteractor
import com.leliukh.labelmanager.features.market.domain.models.Listing
import com.leliukh.labelmanager.features.market.domain.models.ListingFilter
import com.leliukh.labelmanager.features.market.domain.models.Purchase
import com.leliukh.labelmanager.features.market.domain.repository.MarketRepository
import com.leliukh.labelmanager.features.label.domain.repository.LabelRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The market as the signed-in user sees it.
 *
 * Own listings are marked from the session. Every deal changes the user's budget or roster,
 * so after a purchase, a new listing or a withdrawal the cached label is refreshed and the
 * Roster tab is up to date without a pull. Every deal also adds an activity event, so the
 * activity is checked at once and the badge does not wait for the next poll.
 *
 * @param sessionRepository source of the signed-in user's label id
 * @param marketRepository the market endpoints
 * @param labelRepository the cached label
 * @param activityInteractor the activity feed and its unread count
 */
@Singleton
class MarketInteractor @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val marketRepository: MarketRepository,
    private val labelRepository: LabelRepository,
    private val activityInteractor: ActivityInteractor,
) {

    /**
     * @param filter which listings to show
     * @return the listings page by page
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun listings(filter: ListingFilter): Flow<PagingData<Listing>> =
        flow { emit(sessionRepository.current()?.labelId) }
            .flatMapLatest { myLabelId -> marketRepository.listings(filter, myLabelId) }

    /**
     * @param filter which listings to count
     */
    suspend fun countListings(filter: ListingFilter): ApiResult<Int> =
        marketRepository.countListings(filter)

    /**
     * @param listingId another label's listing
     */
    suspend fun purchase(listingId: String): ApiResult<Purchase> =
        marketRepository.purchase(listingId).refreshingLabel()

    /**
     * @param artistId one of the user's artists
     * @param askingPrice price a buyer will pay
     */
    suspend fun createListing(artistId: String, askingPrice: Long): ApiResult<Unit> =
        marketRepository.createListing(artistId, askingPrice).refreshingLabel()

    /**
     * @param listingId one of the user's listings
     */
    suspend fun withdrawListing(listingId: String): ApiResult<Unit> =
        marketRepository.withdrawListing(listingId).refreshingLabel()

    private suspend fun <T> ApiResult<T>.refreshingLabel(): ApiResult<T> {
        onSuccess {
            labelRepository.refreshLabel()
            activityInteractor.check()
        }
        return this
    }
}
