package com.leliukh.labelmanager.features.label.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The cached label, without its artists.
 *
 * @property id unique label id
 * @property name label name
 * @property countryCode ISO 3166-1 alpha-2 country
 * @property budget money available for deals, in whole US dollars
 * @property value sum of the artists' market values
 * @property syncedAtMillis when the label was loaded from the server, in epoch milliseconds
 */
@Entity(tableName = "labels")
data class LabelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val countryCode: String,
    val budget: Long,
    val value: Long,
    val syncedAtMillis: Long,
)
