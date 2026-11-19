package com.leliukh.labelmanager.features.artist.presentation.mapper

import com.leliukh.labelmanager.features.artist.domain.repository.testDetails
import com.leliukh.labelmanager.features.artist.presentation.models.HistoryEntryVO
import com.leliukh.labelmanager.features.label.domain.models.ArtistListing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

class ArtistProfileVOMapperTest {

    @Test
    fun `the history ends with the founding roster of the first label`() {
        val history = testDetails().toProfileVO().history

        assertEquals(2, history.size)
        assertEquals("45%", (history[0] as HistoryEntryVO.Move).rise)
        assertEquals("Garonne Records", (history[1] as HistoryEntryVO.Founding).labelName)
    }

    @Test
    fun `an artist who never moved started at the current label`() {
        val profile = testDetails(deals = emptyList()).toProfileVO()

        assertNull(profile.valueRise)
        val founding = profile.history.single() as HistoryEntryVO.Founding
        assertEquals("Rhein Valley Records", founding.labelName)
    }

    @Test
    fun `the asking price is compared with the market value`() {
        val details = testDetails().let {
            it.copy(
                artist = it.artist.copy(
                    marketValue = 1_000_000,
                    listing = ArtistListing("l1", 1_600_000, Instant.parse("2026-10-25T10:00:00Z")),
                ),
            )
        }

        val comparison = details.toProfileVO().listing!!.priceVsValue

        assertEquals("60%", comparison.percent)
        assertTrue(comparison.isAbove)
    }
}
