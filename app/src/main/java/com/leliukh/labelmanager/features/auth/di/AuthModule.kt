package com.leliukh.labelmanager.features.auth.di

import com.leliukh.labelmanager.features.auth.data.api.AuthApi
import com.leliukh.labelmanager.features.auth.data.repository.AuthRepositoryImpl
import com.leliukh.labelmanager.features.auth.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Bindings of the auth feature.
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthRepository(api: AuthApi): AuthRepository = AuthRepositoryImpl(api)
}
