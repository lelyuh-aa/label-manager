package com.leliukh.labelmanager.features.label.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.leliukh.labelmanager.features.label.data.db.entity.ArtistEntity
import com.leliukh.labelmanager.features.label.data.db.entity.LabelEntity
import com.leliukh.labelmanager.features.label.data.db.entity.LabelWithArtists
import kotlinx.coroutines.flow.Flow

/**
 * Reads and writes the cached label.
 */
@Dao
interface LabelDao {

    /**
     * @param labelId id of the label
     * @return the label with its artists, emitted again after every change; null while the
     *   label is not cached
     */
    @Transaction
    @Query("SELECT * FROM labels WHERE id = :labelId")
    fun observeLabel(labelId: String): Flow<LabelWithArtists?>

    /**
     * Replaces the cached label and its whole roster in one transaction, so observers never see
     * a half-written roster and artists who left the label disappear.
     *
     * @param label the label
     * @param artists all of its artists
     */
    @Transaction
    suspend fun replaceLabel(label: LabelEntity, artists: List<ArtistEntity>) {
        upsertLabel(label)
        deleteArtists(label.id)
        insertArtists(artists)
    }

    /** Removes every cached label and, through the foreign key, every artist. */
    @Query("DELETE FROM labels")
    suspend fun clear()

    @Upsert
    suspend fun upsertLabel(label: LabelEntity)

    @Query("DELETE FROM artists WHERE labelId = :labelId")
    suspend fun deleteArtists(labelId: String)

    @Insert
    suspend fun insertArtists(artists: List<ArtistEntity>)
}
