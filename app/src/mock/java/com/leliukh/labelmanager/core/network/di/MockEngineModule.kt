package com.leliukh.labelmanager.core.network.di

import android.content.Context
import com.leliukh.labelmanager.backend.BackendConfig
import com.leliukh.labelmanager.backend.LabelBackendFactory
import com.leliukh.labelmanager.backend.api.LabelBackend
import com.leliukh.labelmanager.core.network.data.ApiEndpoint
import com.leliukh.labelmanager.core.network.data.createBackendEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.engine.HttpClientEngine
import java.io.File
import javax.inject.Singleton

private const val BACKEND_SNAPSHOT = "mock-backend.json"
private const val ON_DEVICE_HASH_ITERATIONS = 20_000
private const val MOCK_BASE_URL = "http://label-manager.mock/"

/**
 * `mock` flavor: the API runs in-process. Accounts, labels and the market survive restarts in
 * a snapshot under no-backup storage, so they are not restored onto another device.
 *
 * Password hashing uses fewer PBKDF2 iterations than the server: here the "server" runs on the
 * phone, and the server's cost would add over a second to every sign-in on a mid-range device.
 */
@Module
@InstallIn(SingletonComponent::class)
object MockEngineModule {

    @Provides
    @Singleton
    fun provideLabelBackend(@ApplicationContext context: Context): LabelBackend =
        LabelBackendFactory.inMemory(
            snapshotFile = File(context.noBackupFilesDir, BACKEND_SNAPSHOT),
            config = BackendConfig(passwordHashIterations = ON_DEVICE_HASH_ITERATIONS),
        )

    @Provides
    @Singleton
    fun provideHttpClientEngine(backend: LabelBackend): HttpClientEngine =
        createBackendEngine(backend)

    @Provides
    @Singleton
    fun provideApiEndpoint(): ApiEndpoint = ApiEndpoint(baseUrl = MOCK_BASE_URL)
}
