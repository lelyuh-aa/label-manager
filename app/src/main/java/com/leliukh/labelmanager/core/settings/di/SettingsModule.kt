package com.leliukh.labelmanager.core.settings.di

import android.content.Context
import com.leliukh.labelmanager.core.settings.data.repository.SettingsRepositoryImpl
import com.leliukh.labelmanager.core.settings.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val PREFERENCES_NAME = "settings"

/**
 * Bindings of the app settings.
 */
@Module
@InstallIn(SingletonComponent::class)
object SettingsModule {

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository =
        SettingsRepositoryImpl(context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE))
}
