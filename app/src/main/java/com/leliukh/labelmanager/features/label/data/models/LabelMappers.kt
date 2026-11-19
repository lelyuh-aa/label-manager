package com.leliukh.labelmanager.features.label.data.models

import com.leliukh.labelmanager.contract.dto.artist.ListingSummaryDto
import com.leliukh.labelmanager.contract.dto.artist.ArtistDto
import com.leliukh.labelmanager.contract.dto.artist.GenreDto
import com.leliukh.labelmanager.contract.dto.label.LabelDto
import com.leliukh.labelmanager.features.label.domain.models.Artist
import com.leliukh.labelmanager.features.label.domain.models.ArtistListing
import com.leliukh.labelmanager.features.label.domain.models.Genre
import com.leliukh.labelmanager.features.label.domain.models.Label

fun LabelDto.toDomain(): Label = Label(
    id = id,
    name = name,
    countryCode = countryCode,
    budget = budget,
    value = value,
    artists = artists.map { it.toDomain() },
)

fun ArtistDto.toDomain(): Artist = Artist(
    id = id,
    firstName = firstName,
    lastName = lastName,
    countryCode = countryCode,
    genre = genre.toDomain(),
    birthDate = birthDate,
    age = age,
    marketValue = marketValue,
    hits = hits,
    listing = listing?.toDomain(),
)

fun GenreDto.toDomain(): Genre = when (this) {
    GenreDto.POP -> Genre.Pop

    GenreDto.ROCK -> Genre.Rock

    GenreDto.HIP_HOP -> Genre.HipHop

    GenreDto.ELECTRONIC -> Genre.Electronic
}

private fun ListingSummaryDto.toDomain(): ArtistListing = ArtistListing(
    id = id,
    askingPrice = askingPrice,
    listedAt = listedAt,
)
