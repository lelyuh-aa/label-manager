package com.leliukh.labelmanager.features.market.di

import com.leliukh.labelmanager.features.market.data.api.MarketApi
import com.leliukh.labelmanager.features.market.data.repository.MarketRepositoryImpl
import com.leliukh.labelmanager.features.market.domain.repository.MarketRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Bindings of the market feature.
 */
@Module
@InstallIn(SingletonComponent::class)
object MarketModule {

    @Provides
    @Singleton
    fun provideMarketRepository(api: MarketApi): MarketRepository = MarketRepositoryImpl(api)
}
