package com.leliukh.labelmanager.contract.serialization

import com.leliukh.labelmanager.contract.dto.auth.AuthResponseDto
import com.leliukh.labelmanager.contract.dto.auth.UserDto
import com.leliukh.labelmanager.contract.dto.artist.ArtistDto
import com.leliukh.labelmanager.contract.dto.artist.GenreDto
import com.leliukh.labelmanager.contract.dto.reference.Countries
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

class ApiJsonTest {

    @Test
    fun `instants and dates are written as ISO 8601 strings`() {
        val json = ApiJson.encodeToString(AuthResponseDto.serializer(), authResponse)

        assertTrue(json.contains("\"accessTokenExpiresAt\":\"2026-10-25T12:00:00Z\""))
        assertEquals(authResponse, ApiJson.decodeFromString(AuthResponseDto.serializer(), json))
    }

    @Test
    fun `null fields are omitted and unknown fields are ignored`() {
        val json = ApiJson.encodeToString(ArtistDto.serializer(), artist)

        assertFalse(json.contains("listing"))
        val withExtraField = json.dropLast(1) + ",\"trackNumber\":7}"
        assertNull(ApiJson.decodeFromString(ArtistDto.serializer(), withExtraField).listing)
    }

    @Test
    fun `country codes are unique and sorted by name`() {
        val codes = Countries.all.map { it.code }

        assertEquals(codes.size, codes.toSet().size)
        assertEquals(Countries.all.sortedBy { it.name }, Countries.all)
        assertEquals("Germany", Countries.find("DE")?.name)
    }

    private val authResponse = AuthResponseDto(
        accessToken = "access",
        accessTokenExpiresAt = Instant.parse("2026-10-25T12:00:00Z"),
        refreshToken = "refresh",
        user = UserDto(id = "user-1", email = "producer@example.com", labelId = "label-1"),
    )

    private val artist = ArtistDto(
        id = "artist-1",
        labelId = "label-1",
        firstName = "Lukas",
        lastName = "Brenner",
        countryCode = "DE",
        genre = GenreDto.POP,
        birthDate = LocalDate(1999, 3, 14),
        age = 27,
        marketValue = 1_000_000,
        hits = 0,
    )
}
