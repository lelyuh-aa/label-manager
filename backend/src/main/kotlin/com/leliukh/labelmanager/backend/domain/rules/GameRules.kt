package com.leliukh.labelmanager.backend.domain.rules

import com.leliukh.labelmanager.backend.domain.models.Genre
import kotlin.random.Random
import kotlinx.datetime.LocalDate
import kotlinx.datetime.yearsUntil

/** The rules of the game: how a label starts and how artists are valued. */
object GameRules {

    /** Artists a new label signs per genre: four of each. */
    val ROSTER_COMPOSITION: Map<Genre, Int> = Genre.entries.associateWith { ARTISTS_PER_GENRE }

    val ROSTER_SIZE: Int = ROSTER_COMPOSITION.values.sum()

    const val INITIAL_BUDGET: Long = 3_000_000L

    const val MIN_ARTIST_AGE: Int = 18
    const val MAX_ARTIST_AGE: Int = 45

    const val BASE_ARTIST_VALUE: Long = 400_000L
    const val VALUE_PER_HIT: Long = 40_000L
    const val YOUTH_BONUS: Long = 150_000L
    const val YOUTH_MAX_AGE: Int = 25

    const val RAISE_PERCENT_PER_HIT: Int = 5
    const val MIN_RAISE_PERCENT: Int = 5
    const val MAX_RAISE_PERCENT: Int = 50

    private const val ARTISTS_PER_GENRE = 4
    private const val VALUE_STEP = 10_000L
    private const val PERCENT_TOTAL = 100L

    /**
     * @param random source of randomness
     * @return a random artist age between 18 and 45, inclusive
     */
    fun initialAge(random: Random): Int = random.nextInt(MIN_ARTIST_AGE, MAX_ARTIST_AGE + 1)

    /**
     * The value of a newly generated artist: a base, plus every chart hit, plus a bonus for young
     * artists, rounded to ten thousand dollars.
     *
     * @param age the artist's age
     * @param hits the artist's chart hits
     * @return the initial market value
     */
    fun initialValue(age: Int, hits: Int): Long {
        val youth = if (age <= YOUTH_MAX_AGE) YOUTH_BONUS else 0L
        return roundToStep(BASE_ARTIST_VALUE + VALUE_PER_HIT * hits + youth)
    }

    /**
     * Raises a market value after a deal by 5 % per chart hit, at least 5 % and at most 50 %,
     * rounded to whole dollars.
     *
     * @param value market value before the deal
     * @param hits the artist's chart hits
     * @return market value after the deal
     */
    fun valueAfterDeal(value: Long, hits: Int): Long {
        val percent = (hits * RAISE_PERCENT_PER_HIT).coerceIn(MIN_RAISE_PERCENT, MAX_RAISE_PERCENT)
        return value + (value * percent + PERCENT_TOTAL / 2) / PERCENT_TOTAL
    }

    /**
     * @param birthDate date of birth
     * @param today the current date
     * @return age in full years
     */
    fun ageOn(birthDate: LocalDate, today: LocalDate): Int = birthDate.yearsUntil(today)

    private fun roundToStep(value: Long): Long = (value + VALUE_STEP / 2) / VALUE_STEP * VALUE_STEP
}
