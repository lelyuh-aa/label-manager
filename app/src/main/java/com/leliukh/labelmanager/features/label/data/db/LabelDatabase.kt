package com.leliukh.labelmanager.features.label.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.leliukh.labelmanager.features.label.data.db.dao.LabelDao
import com.leliukh.labelmanager.features.label.data.db.entity.ArtistEntity
import com.leliukh.labelmanager.features.label.data.db.entity.LabelEntity

/**
 * Offline cache of the signed-in user's label. The server stays the source of truth: the cache
 * only lets the roster open instantly and without a connection.
 */
@Database(entities = [LabelEntity::class, ArtistEntity::class], version = 1)
abstract class LabelDatabase : RoomDatabase() {

    abstract fun labelDao(): LabelDao
}
