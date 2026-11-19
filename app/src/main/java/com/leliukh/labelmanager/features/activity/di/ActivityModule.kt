package com.leliukh.labelmanager.features.activity.di

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.leliukh.labelmanager.features.activity.data.api.ActivityApi
import com.leliukh.labelmanager.features.activity.data.repository.ActivityRepositoryImpl
import com.leliukh.labelmanager.features.activity.data.storage.SaleAlertRepositoryImpl
import com.leliukh.labelmanager.features.activity.domain.repository.ActivityRepository
import com.leliukh.labelmanager.features.activity.domain.repository.SaleAlertRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val PREFERENCES_NAME = "sale-alerts"

/**
 * Bindings of the activity feature.
 */
@Module
@InstallIn(SingletonComponent::class)
object ActivityModule {

    @Provides
    @Singleton
    fun provideActivityRepository(api: ActivityApi): ActivityRepository =
        ActivityRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideSaleAlertRepository(@ApplicationContext context: Context): SaleAlertRepository {
        val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        return SaleAlertRepositoryImpl(preferences)
    }

    @Provides
    @Singleton
    fun provideNotificationManager(
        @ApplicationContext context: Context,
    ): NotificationManagerCompat = NotificationManagerCompat.from(context)
}
