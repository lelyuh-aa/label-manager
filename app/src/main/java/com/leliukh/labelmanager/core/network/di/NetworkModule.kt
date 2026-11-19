package com.leliukh.labelmanager.core.network.di

import com.leliukh.labelmanager.BuildConfig
import com.leliukh.labelmanager.core.network.data.ApiEndpoint
import com.leliukh.labelmanager.core.network.data.createApiClient
import com.leliukh.labelmanager.core.session.domain.repository.SessionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import javax.inject.Singleton

/**
 * Provides the API client. The [HttpClientEngine] and the [ApiEndpoint] come from the
 * flavor's own module, so switching between the in-process backend and a real server never
 * touches this code.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpClient(
        engine: HttpClientEngine,
        endpoint: ApiEndpoint,
        sessionRepository: SessionRepository,
    ): HttpClient = createApiClient(
        engine = engine,
        endpoint = endpoint,
        sessionRepository = sessionRepository,
        isLoggingEnabled = BuildConfig.DEBUG,
    )
}
