package com.leliukh.labelmanager.features.market.domain.interactor

import com.leliukh.labelmanager.core.network.domain.ApiError
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.session.domain.repository.FakeSessionRepository
import com.leliukh.labelmanager.core.session.domain.repository.testSession
import com.leliukh.labelmanager.features.activity.domain.repository.activityInteractor
import com.leliukh.labelmanager.features.market.domain.repository.FakeMarketRepository
import com.leliukh.labelmanager.features.label.domain.repository.FakeLabelRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class MarketInteractorTest {

    private val market = FakeMarketRepository()
    private val labels = FakeLabelRepository()
    private val interactor = MarketInteractor(FakeSessionRepository(testSession()), market, labels, activityInteractor(labels))

    @Test
    fun `a purchase refreshes the cached roster`() = runTest {
        interactor.purchase("listing-1")

        assertEquals(1, labels.refreshes)
    }

    @Test
    fun `a new listing and a withdrawal refresh the cached roster`() = runTest {
        interactor.createListing("artist-1", 1_500_000)
        interactor.withdrawListing("listing-1")

        assertEquals(2, labels.refreshes)
    }

    @Test
    fun `a failed purchase leaves the cache alone`() = runTest {
        market.purchaseResult = ApiResult.Failure(ApiError.Network)

        interactor.purchase("listing-1")

        assertEquals(0, labels.refreshes)
    }
}
