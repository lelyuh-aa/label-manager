package com.leliukh.labelmanager.features.label.presentation.mapper

import com.leliukh.labelmanager.features.label.domain.models.ArtistListing
import com.leliukh.labelmanager.features.label.domain.models.Genre
import com.leliukh.labelmanager.features.label.domain.repository.testArtist
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Instant

class RosterVOMapperTest {

    @Test
    fun `artists are grouped in playing order and sorted by last name`() {
        val artists = listOf(
            testArtist(id = "att", lastName = "Abbot", genre = Genre.Electronic),
            testArtist(id = "def-z", lastName = "Zeller", genre = Genre.Rock),
            testArtist(id = "gk", lastName = "Moreau", genre = Genre.Pop),
            testArtist(id = "def-a", lastName = "Achterberg", genre = Genre.Rock),
        )

        val sections = artists.toRosterSections(query = "")

        assertEquals(
            listOf(Genre.Pop, Genre.Rock, Genre.Electronic),
            sections.map { it.genre },
        )
        assertEquals(listOf("def-a", "def-z"), sections[1].artists.map { it.id })
    }

    @Test
    fun `a row shows initials, the value growth and the asking price`() {
        val artist = testArtist(
            firstName = "Daan",
            lastName = "de Vries",
            marketValue = 1_300_000,
            listing = ArtistListing("l1", 1_600_000, Instant.parse("2026-10-25T10:00:00Z")),
        ).toRosterArtistVO()

        assertEquals("DV", artist.initials)
        assertEquals("30%", artist.valueRise)
        assertEquals(1_600_000L, artist.askingPrice)
    }

    @Test
    fun `an artist at the starting value shows no growth`() {
        assertNull(testArtist(marketValue = 1_000_000).toRosterArtistVO().valueRise)
    }
}
