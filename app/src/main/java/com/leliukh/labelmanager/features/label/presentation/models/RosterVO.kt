package com.leliukh.labelmanager.features.label.presentation.models

import com.leliukh.labelmanager.features.label.domain.models.Genre
import kotlin.time.Instant

/**
 * One genre group of the roster list.
 *
 * @property genre the genre
 * @property artists its artists, sorted by last name
 */
data class RosterSectionVO(
    val genre: Genre,
    val artists: List<RosterArtistVO>,
)

/**
 * A row of the roster list.
 *
 * @property id artist id
 * @property fullName first and last name
 * @property initials letters for the avatar
 * @property genre playing genre
 * @property flag flag emoji of the nationality
 * @property countryName localised nationality
 * @property age age in full years
 * @property marketValue market value in whole US dollars
 * @property valueRise growth since the starting value, e.g. "30%"; null when it has not grown
 * @property askingPrice asking price while the artist is for sale
 */
data class RosterArtistVO(
    val id: String,
    val fullName: String,
    val initials: String,
    val genre: Genre,
    val flag: String,
    val countryName: String,
    val age: Int,
    val marketValue: Long,
    val valueRise: String?,
    val askingPrice: Long?,
)

/**
 * Notice that the roster on screen may be out of date.
 *
 * @property isOffline whether the last refresh failed for lack of a connection
 * @property syncedAt when the shown roster was loaded
 */
data class StaleDataVO(
    val isOffline: Boolean,
    val syncedAt: Instant,
)
