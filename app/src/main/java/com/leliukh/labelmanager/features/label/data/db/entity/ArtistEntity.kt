package com.leliukh.labelmanager.features.label.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.leliukh.labelmanager.features.label.domain.models.Genre

/**
 * A cached artist of a [LabelEntity]. Removing the label removes its artists.
 *
 * @property id unique artist id
 * @property labelId id of the owning label
 * @property firstName first name
 * @property lastName last name
 * @property countryCode ISO 3166-1 alpha-2 nationality
 * @property genre playing genre, stored by name
 * @property birthDateEpochDays date of birth as days since 1970-01-01
 * @property age age in full years when the label was loaded
 * @property marketValue market value in whole US dollars
 * @property hits career hits
 * @property listingId id of the open marketing; null when the artist is not for sale
 * @property askingPrice asking price of the open listing
 * @property listedAtMillis when the artist was listed, in epoch milliseconds
 */
@Entity(
    tableName = "artists",
    foreignKeys = [
        ForeignKey(
            entity = LabelEntity::class,
            parentColumns = ["id"],
            childColumns = ["labelId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("labelId")],
)
data class ArtistEntity(
    @PrimaryKey val id: String,
    val labelId: String,
    val firstName: String,
    val lastName: String,
    val countryCode: String,
    val genre: Genre,
    val birthDateEpochDays: Int,
    val age: Int,
    val marketValue: Long,
    val hits: Int,
    val listingId: String?,
    val askingPrice: Long?,
    val listedAtMillis: Long?,
)
