package com.leliukh.labelmanager.core.network.di

import com.leliukh.labelmanager.BuildConfig
import com.leliukh.labelmanager.core.network.data.ApiEndpoint
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import javax.inject.Singleton

/**
 * `remote` flavor: real HTTP over OkHttp to the server at [BuildConfig.BASE_URL].
 */
@Module
@InstallIn(SingletonComponent::class)
object RemoteEngineModule {

    @Provides
    @Singleton
    fun provideHttpClientEngine(): HttpClientEngine = OkHttp.create()

    @Provides
    @Singleton
    fun provideApiEndpoint(): ApiEndpoint = ApiEndpoint(baseUrl = BuildConfig.BASE_URL)
}
