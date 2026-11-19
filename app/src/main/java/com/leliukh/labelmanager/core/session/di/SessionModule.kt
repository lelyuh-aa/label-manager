package com.leliukh.labelmanager.core.session.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.FileStorage
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import androidx.datastore.tink.AeadSerializer
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.leliukh.labelmanager.core.session.data.models.SessionFileNO
import com.leliukh.labelmanager.core.session.data.repository.SessionRepositoryImpl
import com.leliukh.labelmanager.core.session.data.serializer.SessionFileSerializer
import com.leliukh.labelmanager.core.session.domain.repository.SessionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

private const val SESSION_FILE = "session.json"
private const val KEYSET_NAME = "session_keyset"
private const val KEYSET_PREFERENCES = "session_keyset_prefs"
private const val MASTER_KEY_URI = "android-keystore://label_manager_session_key"
private const val KEY_TEMPLATE = "AES256_GCM"

/**
 * Provides the session storage: a DataStore file encrypted with Tink.
 *
 * The data key is AES-256-GCM, kept in a Tink keyset that is itself encrypted by a master
 * key in the Android Keystore, so the tokens never touch the disk in plain text. The file
 * name is bound as associated data, which stops a ciphertext from another file being swapped
 * in. An unreadable file (e.g. after a keystore reset) is replaced by an empty session, which
 * simply signs the user out.
 */
@Module
@InstallIn(SingletonComponent::class)
object SessionModule {

    @Provides
    @Singleton
    fun provideSessionDataStore(
        @ApplicationContext context: Context,
    ): DataStore<SessionFileNO> {
        AeadConfig.register()
        val aead = AndroidKeysetManager.Builder()
            .withSharedPref(context, KEYSET_NAME, KEYSET_PREFERENCES)
            .withKeyTemplate(KeyTemplates.get(KEY_TEMPLATE))
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
            .getPrimitive(RegistryConfiguration.get(), Aead::class.java)
        val serializer = AeadSerializer(
            aead = aead,
            wrappedSerializer = SessionFileSerializer,
            associatedData = SESSION_FILE.encodeToByteArray(),
        )
        return DataStore.Builder(
            storage = FileStorage(serializer) { context.dataStoreFile(SESSION_FILE) },
            context = Dispatchers.IO + SupervisorJob(),
        )
            .setCorruptionHandler(ReplaceFileCorruptionHandler { SessionFileNO() })
            .build()
    }

    @Provides
    @Singleton
    fun provideSessionRepository(dataStore: DataStore<SessionFileNO>): SessionRepository =
        SessionRepositoryImpl(dataStore)
}
