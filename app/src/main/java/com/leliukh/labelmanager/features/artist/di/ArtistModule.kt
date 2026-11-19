package com.leliukh.labelmanager.features.artist.di

import com.leliukh.labelmanager.features.artist.data.api.ArtistApi
import com.leliukh.labelmanager.features.artist.data.repository.ArtistRepositoryImpl
import com.leliukh.labelmanager.features.artist.domain.repository.ArtistRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Bindings of the artist feature.
 */
@Module
@InstallIn(SingletonComponent::class)
object ArtistModule {

    @Provides
    @Singleton
    fun provideArtistRepository(api: ArtistApi): ArtistRepository = ArtistRepositoryImpl(api)
}
