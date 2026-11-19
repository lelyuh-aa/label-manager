package com.leliukh.labelmanager.features.activity.domain.interactor

import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.features.activity.domain.models.ActivityType
import com.leliukh.labelmanager.features.activity.domain.repository.FakeActivityRepository
import com.leliukh.labelmanager.features.activity.domain.repository.FakeSaleAlertRepository
import com.leliukh.labelmanager.features.activity.domain.repository.testEvent
import com.leliukh.labelmanager.features.label.domain.repository.FakeLabelRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

class ActivityInteractorTest {

    private val activity = FakeActivityRepository()
    private val alerts = FakeSaleAlertRepository()
    private val labels = FakeLabelRepository()
    private val interactor = ActivityInteractor(activity, labels, alerts)

    @Test
    fun `the first check only remembers where the feed is`() = runTest {
        activity.events = listOf(testEvent("e1"))

        val sales = (interactor.newSales() as ApiResult.Success).data

        assertTrue(sales.isEmpty())
        assertEquals(Instant.parse("2026-10-26T10:00:00Z"), alerts.lastAlertedAt)
    }

    @Test
    fun `later checks announce new unread sales only, oldest first`() = runTest {
        alerts.lastAlertedAt = Instant.parse("2026-10-26T09:00:00Z")
        activity.events = listOf(
            testEvent("e4", at = "2026-10-26T10:30:00Z"),
            testEvent("e3", type = ActivityType.ArtistListed, at = "2026-10-26T10:20:00Z"),
            testEvent("e2", at = "2026-10-26T10:10:00Z"),
            testEvent("e1", at = "2026-10-26T08:00:00Z"),
        )

        val sales = (interactor.newSales() as ApiResult.Success).data

        assertEquals(listOf("e2", "e4"), sales.map { it.id })
    }

    @Test
    fun `more unread events refresh the roster and mark all read clears the badge`() = runTest {
        activity.events = listOf(testEvent("e1"), testEvent("e2"))

        interactor.check()
        assertEquals(2, interactor.unreadCount.value)
        assertEquals(1, labels.refreshes)

        interactor.markAllRead()
        assertEquals(0, interactor.unreadCount.value)
    }
}
