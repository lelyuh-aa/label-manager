package com.leliukh.labelmanager.backend.generation

import com.leliukh.labelmanager.backend.domain.ids.IdGenerator
import com.leliukh.labelmanager.backend.domain.models.Artist
import com.leliukh.labelmanager.backend.domain.models.Genre
import com.leliukh.labelmanager.backend.domain.rules.GameRules
import kotlin.random.Random
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/**
 * Creates artists with realistic names, ages and career hits.
 *
 * Most artists share the label's nationality, the rest come from any country of the [NameCatalog].
 *
 * @param random source of randomness
 * @param idGenerator source of artist ids
 */
class ArtistGenerator(
    private val random: Random,
    private val idGenerator: IdGenerator,
) {

    /**
     * @param labelId id of the label the artist joins
     * @param labelCountryCode the label's country, which most artists share
     * @param genre the artist's genre
     * @param today the current date, the age is counted from it
     * @return a new artist valued by [GameRules.initialValue]
     */
    fun generate(
        labelId: String,
        labelCountryCode: String,
        genre: Genre,
        today: LocalDate,
    ): Artist {
        val names = nationalityFor(labelCountryCode)
        val age = GameRules.initialAge(random)
        val birthDate =
            today
                .minus(age, DateTimeUnit.YEAR)
                .minus(random.nextInt(DAYS_IN_SHORT_YEAR), DateTimeUnit.DAY)
        val hits = chartHits(age)
        return Artist(
            id = idGenerator.next(),
            labelId = labelId,
            firstName = names.firstNames.random(random),
            lastName = names.lastNames.random(random),
            countryCode = names.countryCode,
            genre = genre,
            birthDate = birthDate,
            marketValue = GameRules.initialValue(age, hits),
            hits = hits,
        )
    }

    private fun nationalityFor(labelCountryCode: String): CountryNames {
        val home = NameCatalog.find(labelCountryCode)
        return if (home != null && random.nextDouble() < HOME_NATIONALITY_SHARE) {
            home
        } else {
            NameCatalog.countries.random(random)
        }
    }

    // About one hit every second year of a career that starts at 18.
    private fun chartHits(age: Int): Int =
        (0 until (age - FIRST_CAREER_AGE).coerceAtLeast(0)).count { random.nextBoolean() }

    private companion object {
        const val DAYS_IN_SHORT_YEAR = 365
        const val HOME_NATIONALITY_SHARE = 0.65
        const val FIRST_CAREER_AGE = 17
    }
}
