package com.leliukh.labelmanager.features.label.domain.models

import kotlinx.datetime.LocalDate
import kotlin.time.Instant

/** Market value every generated artist starts with, in whole US dollars. */
const val STARTING_MARKET_VALUE = 1_000_000L

/**
 * Playing genre.
 */
enum class Genre {
    Pop,
    Rock,
    HipHop,
    Electronic,
}

/**
 * The signed-in user's label.
 *
 * @property id unique label id
 * @property name label name
 * @property countryCode ISO 3166-1 alpha-2 country
 * @property budget money available for deals, in whole US dollars
 * @property value sum of the artists' market values
 * @property artists the roster
 */
data class Label(
    val id: String,
    val name: String,
    val countryCode: String,
    val budget: Long,
    val value: Long,
    val artists: List<Artist>,
)

/**
 * An artist of the label.
 *
 * @property id unique artist id
 * @property firstName first name
 * @property lastName last name
 * @property countryCode ISO 3166-1 alpha-2 nationality
 * @property genre playing genre
 * @property birthDate date of birth
 * @property age age in full years
 * @property marketValue market value in whole US dollars
 * @property hits career hits
 * @property listing the open marketing, if the artist is for sale
 */
data class Artist(
    val id: String,
    val firstName: String,
    val lastName: String,
    val countryCode: String,
    val genre: Genre,
    val birthDate: LocalDate,
    val age: Int,
    val marketValue: Long,
    val hits: Int,
    val listing: ArtistListing?,
)

/**
 * An artist's open marketing.
 *
 * @property id unique listing id
 * @property askingPrice price a buyer pays, in whole US dollars
 * @property listedAt moment of listing
 */
data class ArtistListing(
    val id: String,
    val askingPrice: Long,
    val listedAt: Instant,
)

/**
 * The label as last loaded from the server and kept on the device.
 *
 * @property label the label
 * @property syncedAt when it was loaded
 */
data class CachedLabel(
    val label: Label,
    val syncedAt: Instant,
)
