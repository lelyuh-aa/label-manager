package com.leliukh.labelmanager.features.label.di

import android.content.Context
import androidx.room.Room
import com.leliukh.labelmanager.features.label.data.api.LabelApi
import com.leliukh.labelmanager.features.label.data.db.LabelDatabase
import com.leliukh.labelmanager.features.label.data.db.dao.LabelDao
import com.leliukh.labelmanager.features.label.data.repository.LabelRepositoryImpl
import com.leliukh.labelmanager.features.label.domain.repository.LabelRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DATABASE_NAME = "label-cache.db"

/**
 * Bindings of the label feature.
 *
 * The database is only a cache of server data, so a schema change simply drops it: the next
 * refresh fills it again.
 */
@Module
@InstallIn(SingletonComponent::class)
object LabelModule {

    @Provides
    @Singleton
    fun provideLabelDatabase(@ApplicationContext context: Context): LabelDatabase =
        Room.databaseBuilder(context, LabelDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    @Singleton
    fun provideLabelDao(database: LabelDatabase): LabelDao = database.labelDao()

    @Provides
    @Singleton
    fun provideLabelRepository(api: LabelApi, dao: LabelDao): LabelRepository =
        LabelRepositoryImpl(api, dao)
}
