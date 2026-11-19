package com.leliukh.labelmanager.features.activity.presentation.mapper

import com.leliukh.labelmanager.features.activity.domain.models.ActivityType
import com.leliukh.labelmanager.features.activity.domain.repository.testEvent
import com.leliukh.labelmanager.features.activity.presentation.models.ActivityFilter
import com.leliukh.labelmanager.features.activity.presentation.models.ActivityItemVO
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivityVOMapperTest {

    private val zone = TimeZone.UTC

    @Test
    fun `a header starts the feed and every new day`() {
        val today = testEvent("e2", at = "2026-10-26T10:00:00Z").toVO()
        val laterToday = testEvent("e1", at = "2026-10-26T11:00:00Z").toVO()
        val yesterday = testEvent("e0", at = "2026-10-25T23:00:00Z").toVO()

        assertEquals(
            ActivityItemVO.DayHeader(LocalDate(2026, 10, 26)),
            dayHeaderBetween(null, laterToday, zone),
        )
        assertNull(dayHeaderBetween(laterToday, today, zone))
        assertEquals(
            ActivityItemVO.DayHeader(LocalDate(2026, 10, 25)),
            dayHeaderBetween(today, yesterday, zone),
        )
    }

    @Test
    fun `a purchase shows the value growth`() {
        val event = testEvent("e1", type = ActivityType.ArtistBought).toVO()

        assertEquals("37%", event.valueRise)
    }

    @Test
    fun `the listings chip shows listing and withdrawal events`() {
        assertTrue(testEvent("e1", ActivityType.ArtistUnlisted).matches(ActivityFilter.Listings))
        assertFalse(testEvent("e1", ActivityType.ArtistSold).matches(ActivityFilter.Listings))
    }
}
