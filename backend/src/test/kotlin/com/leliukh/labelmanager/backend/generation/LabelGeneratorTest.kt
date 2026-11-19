package com.leliukh.labelmanager.backend.generation

import com.leliukh.labelmanager.backend.domain.ids.IdGenerator
import com.leliukh.labelmanager.backend.domain.models.Genre
import com.leliukh.labelmanager.backend.domain.rules.GameRules
import com.leliukh.labelmanager.contract.dto.reference.Countries
import kotlin.random.Random
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LabelGeneratorTest {

    @Test
    fun `a new label has 16 artists, four of every genre`() {
        val (_, artists) = generator(seed = 1).generate("user-1", emptySet(), today)

        assertEquals(16, artists.size)
        Genre.entries.forEach { genre -> assertEquals(4, artists.count { it.genre == genre }) }
    }

    @Test
    fun `every artist is valued by age and hits and aged 18 to 45`() {
        repeat(50) { seed ->
            val (label, artists) = generator(seed).generate(null, emptySet(), today)

            artists.forEach { artist ->
                val age = GameRules.ageOn(artist.birthDate, today)
                assertTrue(age in 18..45)
                assertEquals(GameRules.initialValue(age, artist.hits), artist.marketValue)
                assertEquals(label.id, artist.labelId)
                assertTrue(artist.firstName.isNotBlank() && artist.lastName.isNotBlank())
                assertTrue(artist.hits >= 0)
            }
        }
    }

    @Test
    fun `the budget is 3 million and countries are accepted by the API`() {
        repeat(50) { seed ->
            val (label, artists) = generator(seed).generate("user", emptySet(), today)

            assertEquals(3_000_000L, label.budget)
            assertNotNull(Countries.find(label.countryCode))
            artists.forEach { assertNotNull(Countries.find(it.countryCode)) }
        }
    }

    @Test
    fun `the owner is kept and the requested country is used`() {
        val userLabel = generator(seed = 2).generate("user-7", emptySet(), today, "IT").label
        val rival = generator(seed = 2).generate(null, emptySet(), today).label

        assertEquals("user-7", userLabel.ownerUserId)
        assertEquals("IT", userLabel.countryCode)
        assertNull(rival.ownerUserId)
    }

    @Test
    fun `label names do not repeat taken names`() {
        val generator = generator(seed = 3)
        val taken = mutableSetOf<String>()

        repeat(200) {
            val name = generator.generate(null, taken, today, "DE").label.name
            assertTrue(name.lowercase() !in taken.map(String::lowercase))
            taken += name
        }
    }

    @Test
    fun `the same seed generates the same label`() {
        assertEquals(
            generator(seed = 4).generate("u", emptySet(), today),
            generator(seed = 4).generate("u", emptySet(), today),
        )
    }

    @Test
    fun `every catalog country is accepted by the API`() {
        NameCatalog.countries.forEach {
            assertNotNull(it.countryCode, Countries.find(it.countryCode))
        }
    }

    private val today = LocalDate(2026, 10, 25)

    private fun generator(seed: Int): LabelGenerator {
        val random = Random(seed)
        return LabelGenerator(random, IdGenerator(random))
    }
}
