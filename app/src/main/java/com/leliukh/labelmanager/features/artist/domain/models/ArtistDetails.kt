package com.leliukh.labelmanager.features.artist.domain.models

import com.leliukh.labelmanager.features.label.domain.models.Artist
import kotlin.time.Instant

/**
 * An artist with the label that owns them and their deals.
 *
 * @property artist the artist
 * @property label the owning label
 * @property deals completed deals, newest first
 * @property isMine whether the artist belongs to the signed-in user's label
 */
data class ArtistDetails(
    val artist: Artist,
    val label: LabelSummary,
    val deals: List<Deal>,
    val isMine: Boolean,
)

/**
 * Short form of a label, wherever another label is referenced.
 *
 * @property id unique label id
 * @property name label name
 * @property countryCode ISO 3166-1 alpha-2 country
 */
data class LabelSummary(
    val id: String,
    val name: String,
    val countryCode: String,
)

/**
 * A completed deal of an artist between two labels.
 *
 * @property id unique deal id
 * @property from the selling label
 * @property to the buying label
 * @property price price the buyer paid, in whole US dollars
 * @property valueBefore market value before the deal
 * @property valueAfter market value after the deal
 * @property signedAt moment of the deal
 */
data class Deal(
    val id: String,
    val from: LabelSummary,
    val to: LabelSummary,
    val price: Long,
    val valueBefore: Long,
    val valueAfter: Long,
    val signedAt: Instant,
)
