package com.leliukh.labelmanager.features.label.data.db.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * A cached label together with its artists, read in one transaction.
 *
 * @property label the label
 * @property artists its artists
 */
data class LabelWithArtists(
    @Embedded val label: LabelEntity,
    @Relation(parentColumn = "id", entityColumn = "labelId")
    val artists: List<ArtistEntity>,
)
