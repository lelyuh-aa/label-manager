package com.leliukh.labelmanager.features.label.domain.repository

import com.leliukh.labelmanager.features.label.domain.models.Artist
import com.leliukh.labelmanager.features.label.domain.models.ArtistListing
import com.leliukh.labelmanager.features.label.domain.models.Genre
import com.leliukh.labelmanager.features.label.domain.models.Label
import kotlinx.datetime.LocalDate

/**
 * A new label of `label-1` with four artists of every genre.
 *
 * @param artists the roster
 */
fun testLabel(artists: List<Artist> = testRoster()) = Label(
    id = "label-1",
    name = "Rhein Valley Records",
    countryCode = "DE",
    budget = 3_000_000,
    value = artists.sumOf { it.marketValue },
    artists = artists,
)

/** 16 artists, four of every genre, all worth $1M. */
fun testRoster(): List<Artist> = Genre.entries.flatMap { genre ->
    List(4) { index -> testArtist(id = "$genre-$index", genre = genre) }
}

/**
 * @param id artist id
 * @param firstName first name
 * @param lastName last name
 * @param genre playing genre
 * @param marketValue market value
 * @param listing open marketing
 */
fun testArtist(
    id: String = "artist-1",
    firstName: String = "First",
    lastName: String = "Last",
    genre: Genre = Genre.Rock,
    marketValue: Long = 1_000_000,
    listing: ArtistListing? = null,
) = Artist(
    id = id,
    firstName = firstName,
    lastName = lastName,
    countryCode = "DE",
    genre = genre,
    birthDate = LocalDate(2000, 1, 1),
    age = 26,
    marketValue = marketValue,
    hits = 0,
    listing = listing,
)
