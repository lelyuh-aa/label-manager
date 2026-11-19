package com.leliukh.labelmanager.features.label.data.repository

import androidx.room.Room
import com.leliukh.labelmanager.contract.dto.artist.ArtistDto
import com.leliukh.labelmanager.contract.dto.artist.GenreDto
import com.leliukh.labelmanager.contract.dto.label.LabelDto
import com.leliukh.labelmanager.contract.serialization.ApiJson
import com.leliukh.labelmanager.core.network.data.ApiEndpoint
import com.leliukh.labelmanager.core.network.data.createApiClient
import com.leliukh.labelmanager.core.session.domain.repository.FakeSessionRepository
import com.leliukh.labelmanager.features.label.data.api.LabelApi
import com.leliukh.labelmanager.features.label.data.db.LabelDatabase
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.time.Clock
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LabelRepositoryImplTest {

    private val database = Room
        .inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), LabelDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    private var served = label(artists = listOf(artist("p1"), artist("p2")))
    private val now = Instant.parse("2026-10-26T12:00:00Z")
    private val repository = LabelRepositoryImpl(
        api = LabelApi(
            createApiClient(
                engine = MockEngine {
                    respond(
                        content = ApiJson.encodeToString(LabelDto.serializer(), served),
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                },
                endpoint = ApiEndpoint("http://label.test/"),
                sessionRepository = FakeSessionRepository(),
                isLoggingEnabled = false,
            ),
        ),
        dao = database.labelDao(),
        clock = object : Clock {
            override fun now(): Instant = now
        },
    )

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `a refresh caches the label with its roster and the sync time`() = runTest {
        repository.refreshLabel()

        val cached = repository.observeLabel("label-1").first()
        assertEquals(listOf("p1", "p2"), cached?.label?.artists?.map { it.id })
        assertEquals(LocalDate(2001, 5, 17), cached?.label?.artists?.first()?.birthDate)
        assertEquals(now, cached?.syncedAt)
    }

    @Test
    fun `an artist who left the label disappears from the cache`() = runTest {
        repository.refreshLabel()
        served = label(artists = listOf(artist("p2")))

        repository.refreshLabel()

        val artists = repository.observeLabel("label-1").first()?.label?.artists
        assertEquals(listOf("p2"), artists?.map { it.id })
    }

    @Test
    fun `clearing the cache forgets the label`() = runTest {
        repository.refreshLabel()

        repository.clearCache()

        assertNull(repository.observeLabel("label-1").first())
    }

    private fun label(artists: List<ArtistDto>) = LabelDto(
        id = "label-1",
        name = "Rhein Valley Records",
        countryCode = "DE",
        budget = 3_000_000,
        value = artists.sumOf { it.marketValue },
        artists = artists,
    )

    private fun artist(id: String) = ArtistDto(
        id = id,
        labelId = "label-1",
        firstName = "Lukas",
        lastName = "Brenner",
        countryCode = "DE",
        genre = GenreDto.POP,
        birthDate = LocalDate(2001, 5, 17),
        age = 25,
        marketValue = 1_000_000,
        hits = 0,
    )
}
