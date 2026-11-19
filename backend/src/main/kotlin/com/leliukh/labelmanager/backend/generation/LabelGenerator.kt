package com.leliukh.labelmanager.backend.generation

import com.leliukh.labelmanager.backend.domain.ids.IdGenerator
import com.leliukh.labelmanager.backend.domain.models.Artist
import com.leliukh.labelmanager.backend.domain.models.Label
import com.leliukh.labelmanager.backend.domain.rules.GameRules
import kotlin.random.Random
import kotlinx.datetime.LocalDate

/**
 * A new label together with its roster.
 *
 * @property label the label
 * @property artists the roster of [GameRules.ROSTER_SIZE] artists
 */
data class GeneratedLabel(
    val label: Label,
    val artists: List<Artist>,
)

/**
 * Creates a label with a label-like name, a random budget and a full roster, following [GameRules].
 *
 * @param random source of randomness
 * @param idGenerator source of label and artist ids
 */
class LabelGenerator(
    private val random: Random,
    private val idGenerator: IdGenerator,
) {

    private val artistGenerator = ArtistGenerator(random, idGenerator)

    /**
     * @param ownerUserId id of the owning account; null for a rival label
     * @param takenNames names already used by other labels, compared case-insensitively
     * @param today the current date, artist ages are counted from it
     * @param countryCode the label's country; a random known country when null
     * @return the label and its roster, four artists of every genre
     */
    fun generate(
        ownerUserId: String?,
        takenNames: Set<String>,
        today: LocalDate,
        countryCode: String? = null,
    ): GeneratedLabel {
        val country = countryCode?.let(NameCatalog::find) ?: NameCatalog.countries.random(random)
        val label =
            Label(
                id = idGenerator.next(),
                name = uniqueName(country, takenNames.map(String::lowercase).toSet()),
                countryCode = country.countryCode,
                budget = GameRules.INITIAL_BUDGET,
                ownerUserId = ownerUserId,
            )
        val artists =
            GameRules.ROSTER_COMPOSITION.flatMap { (genre, count) ->
                List(count) {
                    artistGenerator.generate(label.id, label.countryCode, genre, today)
                }
            }
        return GeneratedLabel(label, artists)
    }

    private fun uniqueName(country: CountryNames, takenLowercase: Set<String>): String {
        repeat(NAME_ATTEMPTS) {
            val name = country.labelPatterns.random(random).format(country.places.random(random))
            if (name.lowercase() !in takenLowercase) return name
        }
        val base = country.labelPatterns.first().format(country.places.first())
        return generateSequence(2) { it + 1 }
            .map { "$base $it" }
            .first { it.lowercase() !in takenLowercase }
    }

    private companion object {
        const val NAME_ATTEMPTS = 30
    }
}
