package com.leliukh.labelmanager.core.session.data.repository

import androidx.datastore.core.DataStore
import com.leliukh.labelmanager.core.session.data.models.SessionFileNO
import com.leliukh.labelmanager.core.session.data.models.toDomain
import com.leliukh.labelmanager.core.session.data.models.toStored
import com.leliukh.labelmanager.core.session.domain.models.AuthSession
import com.leliukh.labelmanager.core.session.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * [SessionRepository] backed by an encrypted DataStore file.
 *
 * @param dataStore the session file, encrypted with Tink AES-256-GCM
 */
class SessionRepositoryImpl(
    private val dataStore: DataStore<SessionFileNO>,
) : SessionRepository {

    override val session: Flow<AuthSession?> = dataStore.data
        .map { file -> file.session?.toDomain() }
        .distinctUntilChanged()

    override suspend fun current(): AuthSession? = session.first()

    override suspend fun save(session: AuthSession) {
        dataStore.updateData { SessionFileNO(session = session.toStored()) }
    }

    override suspend fun clear() {
        dataStore.updateData { SessionFileNO() }
    }
}
