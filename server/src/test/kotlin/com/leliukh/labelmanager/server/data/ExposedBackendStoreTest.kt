package com.leliukh.labelmanager.server.data

import com.leliukh.labelmanager.backend.domain.models.ActivityEvent
import com.leliukh.labelmanager.backend.domain.models.ActivityType
import com.leliukh.labelmanager.backend.domain.models.Listing
import com.leliukh.labelmanager.backend.domain.models.Artist
import com.leliukh.labelmanager.backend.domain.models.Genre
import com.leliukh.labelmanager.backend.domain.models.RefreshToken
import com.leliukh.labelmanager.backend.domain.models.Label
import com.leliukh.labelmanager.backend.domain.models.Deal
import com.leliukh.labelmanager.backend.domain.models.User
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ExposedBackendStoreTest {

    @get:Rule val folder = TemporaryFolder()

    private val now = Instant.parse("2026-10-25T12:00:00Z")

    @Test
    fun `every entity round-trips through SQLite and survives a reopen`() = runTest {
        val file = folder.root.resolve("data/test.db")
        val store = ExposedBackendStore.sqlite(file)
        store.write {
            saveLabel(label)
            saveUser(user)
            saveArtist(artist)
            saveListing(listing)
            saveDeal(deal)
            saveActivity(event)
            saveRefreshToken(token)
            setSimulatedUntil(now)
        }

        val reopened = ExposedBackendStore.sqlite(file)

        reopened.read {
            assertEquals(label, findLabel(label.id))
            assertEquals(user, findUserByEmail(user.email))
            assertEquals(artist, findArtist(artist.id))
            assertEquals(listOf(artist), artistsOfLabel(label.id))
            assertEquals(listing, findListingOfArtist(artist.id))
            assertEquals(listOf(deal), dealsOfArtist(artist.id))
            assertEquals(listOf(event), activityOfLabel(label.id))
            assertEquals(token, findRefreshToken(token.tokenHash))
            assertEquals(now, simulatedUntil())
        }
    }

    @Test
    fun `updates, deletes and marking activity read`() = runTest {
        val store = ExposedBackendStore.sqlite(folder.root.resolve("test.db"))
        store.write {
            saveLabel(label)
            saveListing(listing)
            saveActivity(event)
            saveRefreshToken(token)
        }

        store.write {
            saveLabel(label.copy(budget = 1))
            deleteListing(listing.id)
            markActivityRead(label.id)
            deleteRefreshToken(token.tokenHash)
        }

        store.read {
            assertEquals(1L, findLabel(label.id)?.budget)
            assertNull(findListing(listing.id))
            assertTrue(activityOfLabel(label.id).single().isRead)
            assertNull(findRefreshToken(token.tokenHash))
        }
    }

    @Test
    fun `a throwing write rolls back`() = runTest {
        val store = ExposedBackendStore.sqlite(folder.root.resolve("test.db"))
        store.write { saveLabel(label) }

        runCatching {
            store.write {
                saveLabel(label.copy(budget = 0))
                error("rejected")
            }
        }

        assertEquals(label.budget, store.read { findLabel(label.id)?.budget })
    }

    @Test
    fun `concurrent writes do not lose updates`() = runTest {
        val store = ExposedBackendStore.sqlite(folder.root.resolve("test.db"))
        store.write { saveLabel(label.copy(budget = 0)) }

        withContext(Dispatchers.Default) {
            List(50) {
                    async {
                        store.write {
                            val current = requireNotNull(findLabel(label.id))
                            saveLabel(current.copy(budget = current.budget + 1))
                        }
                    }
                }
                .awaitAll()
        }

        assertEquals(50L, store.read { findLabel(label.id)?.budget })
    }

    private val label = Label("label-1", "Rhein Valley Records", "DE", 3_482_150, ownerUserId = "user-1")
    private val user = User("user-1", "producer@example.com", "hash", label.id, now)
    private val artist =
        Artist(
            id = "artist-1",
            labelId = label.id,
            firstName = "Lukas",
            lastName = "Brenner",
            countryCode = "DE",
            genre = Genre.POP,
            birthDate = LocalDate(1999, 3, 14),
            marketValue = 1_000_000,
            hits = 1,
        )
    private val listing = Listing("listing-1", artist.id, label.id, 1_500_000, now)
    private val deal =
        Deal(
            "deal-1",
            artist.id,
            "Lukas Brenner",
            "label-2",
            label.id,
            900_000,
            1_000_000,
            1_380_000,
            now,
        )
    private val event =
        ActivityEvent(
            id = "event-1",
            labelId = label.id,
            type = ActivityType.ARTIST_BOUGHT,
            occurredAt = now,
            artistId = artist.id,
            artistFirstName = "Lukas",
            artistLastName = "Brenner",
            artistGenre = Genre.POP,
            counterpartyLabelId = "label-2",
            amount = 900_000,
            valueBefore = 1_000_000,
            valueAfter = 1_380_000,
        )
    private val token = RefreshToken("abc123", user.id, now)
}
