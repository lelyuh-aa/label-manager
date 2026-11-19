package com.leliukh.labelmanager.backend.domain.rules

import com.leliukh.labelmanager.backend.domain.ids.IdGenerator
import com.leliukh.labelmanager.backend.domain.models.Genre
import kotlin.random.Random
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameRulesTest {

    @Test
    fun `a new label signs four artists of every genre`() {
        Genre.entries.forEach { assertEquals(4, GameRules.ROSTER_COMPOSITION[it]) }
        assertEquals(16, GameRules.ROSTER_SIZE)
    }

    @Test
    fun `initial age is between 18 and 45 and reaches both ends`() {
        val random = Random(2)

        val ages = List(10_000) { GameRules.initialAge(random) }

        assertTrue(ages.all { it in 18..45 })
        assertEquals(18, ages.min())
        assertEquals(45, ages.max())
    }

    @Test
    fun `initial value grows with hits and favours young artists`() {
        assertEquals(400_000L, GameRules.initialValue(age = 30, hits = 0))
        assertEquals(800_000L, GameRules.initialValue(age = 30, hits = 10))
        assertEquals(550_000L, GameRules.initialValue(age = 22, hits = 0))
    }

    @Test
    fun `a deal raises the value by 5 percent per hit within 5 to 50 percent`() {
        assertEquals(1_050_000L, GameRules.valueAfterDeal(1_000_000L, hits = 0))
        assertEquals(1_150_000L, GameRules.valueAfterDeal(1_000_000L, hits = 3))
        assertEquals(1_500_000L, GameRules.valueAfterDeal(1_000_000L, hits = 25))
    }

    @Test
    fun `age counts full years only`() {
        val birthDate = LocalDate(2000, 6, 15)

        assertEquals(25, GameRules.ageOn(birthDate, LocalDate(2026, 6, 14)))
        assertEquals(26, GameRules.ageOn(birthDate, LocalDate(2026, 6, 15)))
    }

    @Test
    fun `ids look like version 4 UUIDs and repeat for the same seed`() {
        val first = IdGenerator(Random(4)).next()
        val second = IdGenerator(Random(4)).next()

        assertEquals(first, second)
        assertTrue(
            Regex("[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")
                .matches(first)
        )
    }
}
