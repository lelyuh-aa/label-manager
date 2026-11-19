package com.leliukh.labelmanager.features.label.presentation.mapper

import com.leliukh.labelmanager.core.formatting.presentation.countryDisplayName
import com.leliukh.labelmanager.core.formatting.presentation.countryFlagEmoji
import com.leliukh.labelmanager.core.formatting.presentation.formatPercentChange
import com.leliukh.labelmanager.core.formatting.presentation.normalizedForSearch
import com.leliukh.labelmanager.features.label.domain.models.Artist
import com.leliukh.labelmanager.features.label.domain.models.Genre
import com.leliukh.labelmanager.features.label.domain.models.STARTING_MARKET_VALUE
import com.leliukh.labelmanager.features.label.presentation.models.RosterArtistVO
import com.leliukh.labelmanager.features.label.presentation.models.RosterSectionVO
import java.text.Collator

/**
 * Groups the roster by genre in playing order, keeping only artists whose name contains
 * [query]. The match ignores case and accents, so "theo" finds "Théo". Empty groups are left
 * out.
 *
 * @param query text typed into the search field
 */
fun List<Artist>.toRosterSections(query: String): List<RosterSectionVO> {
    val needle = query.trim().normalizedForSearch()
    val collator = Collator.getInstance()
    val byGenre = filter { needle.isEmpty() || needle in it.fullName.normalizedForSearch() }
        .sortedWith(compareBy(collator) { artist: Artist -> artist.lastName }
            .thenBy(collator) { it.firstName })
        .groupBy { it.genre }
    return Genre.entries.mapNotNull { genre ->
        byGenre[genre]?.let { artists ->
            RosterSectionVO(genre = genre, artists = artists.map { it.toRosterArtistVO() })
        }
    }
}

fun Artist.toRosterArtistVO(): RosterArtistVO = RosterArtistVO(
    id = id,
    fullName = fullName,
    initials = initials,
    genre = genre,
    flag = countryFlagEmoji(countryCode),
    countryName = countryDisplayName(countryCode),
    age = age,
    marketValue = marketValue,
    valueRise = if (marketValue > STARTING_MARKET_VALUE) {
        formatPercentChange(STARTING_MARKET_VALUE, marketValue)
    } else {
        null
    },
    askingPrice = listing?.askingPrice,
)

/** Letters for the avatar: the first name's initial and the last word of the last name's. */
val Artist.initials: String
    get() {
        val lastWord = lastName.trim().substringAfterLast(' ')
        return "${firstName.firstOrNull() ?: ""}${lastWord.firstOrNull() ?: ""}".uppercase()
    }

private val Artist.fullName: String
    get() = "$firstName $lastName"
