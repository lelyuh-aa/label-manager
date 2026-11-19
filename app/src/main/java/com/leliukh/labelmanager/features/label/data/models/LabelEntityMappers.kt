package com.leliukh.labelmanager.features.label.data.models

import com.leliukh.labelmanager.features.label.data.db.entity.ArtistEntity
import com.leliukh.labelmanager.features.label.data.db.entity.LabelEntity
import com.leliukh.labelmanager.features.label.data.db.entity.LabelWithArtists
import com.leliukh.labelmanager.features.label.domain.models.CachedLabel
import com.leliukh.labelmanager.features.label.domain.models.Artist
import com.leliukh.labelmanager.features.label.domain.models.ArtistListing
import com.leliukh.labelmanager.features.label.domain.models.Label
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

/**
 * @param syncedAt when the label was loaded from the server
 */
fun Label.toEntity(syncedAt: Instant): LabelEntity = LabelEntity(
    id = id,
    name = name,
    countryCode = countryCode,
    budget = budget,
    value = value,
    syncedAtMillis = syncedAt.toEpochMilliseconds(),
)

/**
 * @param labelId id of the owning label
 */
fun Artist.toEntity(labelId: String): ArtistEntity = ArtistEntity(
    id = id,
    labelId = labelId,
    firstName = firstName,
    lastName = lastName,
    countryCode = countryCode,
    genre = genre,
    birthDateEpochDays = birthDate.toEpochDays().toInt(),
    age = age,
    marketValue = marketValue,
    hits = hits,
    listingId = listing?.id,
    askingPrice = listing?.askingPrice,
    listedAtMillis = listing?.listedAt?.toEpochMilliseconds(),
)

fun LabelWithArtists.toDomain(): CachedLabel = CachedLabel(
    label = Label(
        id = label.id,
        name = label.name,
        countryCode = label.countryCode,
        budget = label.budget,
        value = label.value,
        artists = artists.map { it.toDomain() },
    ),
    syncedAt = Instant.fromEpochMilliseconds(label.syncedAtMillis),
)

private fun ArtistEntity.toDomain(): Artist = Artist(
    id = id,
    firstName = firstName,
    lastName = lastName,
    countryCode = countryCode,
    genre = genre,
    birthDate = LocalDate.fromEpochDays(birthDateEpochDays),
    age = age,
    marketValue = marketValue,
    hits = hits,
    listing = toListing(),
)

private fun ArtistEntity.toListing(): ArtistListing? {
    if (listingId == null || askingPrice == null || listedAtMillis == null) return null
    return ArtistListing(
        id = listingId,
        askingPrice = askingPrice,
        listedAt = Instant.fromEpochMilliseconds(listedAtMillis),
    )
}
