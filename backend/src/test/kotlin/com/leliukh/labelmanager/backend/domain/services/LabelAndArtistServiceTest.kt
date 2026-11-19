package com.leliukh.labelmanager.backend.domain.services

import com.leliukh.labelmanager.backend.domain.errors.DomainException
import com.leliukh.labelmanager.backend.testing.ServicesFixture
import com.leliukh.labelmanager.backend.testing.expectFailure
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LabelAndArtistServiceTest {

    private val fixture = ServicesFixture()

    @Test
    fun `the owner renames the label and changes its country`() = runTest {
        val session = fixture.register("producer")

        val label = fixture.labels.update(session.user.labelId, "  Elbe Tapes ", "PL").label

        assertEquals("Elbe Tapes", label.name)
        assertEquals("PL", label.countryCode)
        assertEquals("PL", fixture.labels.label(session.user.labelId).label.countryCode)
    }

    @Test
    fun `label edits are validated`() = runTest {
        val session = fixture.register("producer")

        val error =
            expectFailure<DomainException.ValidationFailed> {
                fixture.labels.update(session.user.labelId, "X", "XX")
            }

        assertEquals(setOf("name", "countryCode"), error.fieldErrors.keys)
    }

    @Test
    fun `absent fields stay as they are`() = runTest {
        val session = fixture.register("producer")
        val before = fixture.labels.label(session.user.labelId).label

        val after = fixture.labels.update(session.user.labelId, name = null, countryCode = "IE").label

        assertEquals(before.name, after.name)
        assertEquals("IE", after.countryCode)
    }

    @Test
    fun `only the owner edits an artist`() = runTest {
        val owner = fixture.register("owner")
        val stranger = fixture.register("stranger")
        val artistId = fixture.anyArtistOf(owner.user.labelId)

        expectFailure<DomainException.Forbidden> {
            fixture.artists.update(stranger.user.labelId, artistId, "Hacked", null, null)
        }
        val updated =
            fixture.artists.update(owner.user.labelId, artistId, " Chidi ", "Okafor", "NG").artist

        assertEquals("Chidi", updated.firstName)
        assertEquals("Okafor", updated.lastName)
        assertEquals("NG", updated.countryCode)
        assertEquals(updated, fixture.artists.details(artistId).artist.artist)
    }

    @Test
    fun `artist edits are validated and missing artists are reported`() = runTest {
        val owner = fixture.register("owner")
        val artistId = fixture.anyArtistOf(owner.user.labelId)

        val error =
            expectFailure<DomainException.ValidationFailed> {
                fixture.artists.update(owner.user.labelId, artistId, "", "x".repeat(31), "ZZ")
            }

        assertEquals(setOf("firstName", "lastName", "countryCode"), error.fieldErrors.keys)
        expectFailure<DomainException.NotFound> { fixture.artists.details("missing") }
    }
}
