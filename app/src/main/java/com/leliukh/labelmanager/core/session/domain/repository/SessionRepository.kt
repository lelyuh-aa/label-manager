package com.leliukh.labelmanager.core.session.domain.repository

import com.leliukh.labelmanager.core.session.domain.models.AuthSession
import kotlinx.coroutines.flow.Flow

/**
 * Keeps the current [AuthSession] across app restarts.
 */
interface SessionRepository {

    /** The current session; null while signed out. */
    val session: Flow<AuthSession?>

    /**
     * @return the current session, or null while signed out
     */
    suspend fun current(): AuthSession?

    /**
     * Replaces the stored session, e.g. after a sign-in or a token refresh.
     *
     * @param session the new session
     */
    suspend fun save(session: AuthSession)

    /** Forgets the session: the user is signed out. */
    suspend fun clear()
}
