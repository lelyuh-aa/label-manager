package com.leliukh.labelmanager.features.auth.domain.interactor

import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.network.domain.map
import com.leliukh.labelmanager.core.network.domain.onSuccess
import com.leliukh.labelmanager.core.session.domain.models.AuthSession
import com.leliukh.labelmanager.core.session.domain.repository.SessionRepository
import com.leliukh.labelmanager.features.auth.domain.repository.AuthRepository
import com.leliukh.labelmanager.features.label.domain.repository.LabelRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Signs the user in, up and out: talks to the server through [AuthRepository] and keeps the
 * resulting session in [SessionRepository]. Storing or clearing the session is what switches
 * the app between the signed-in and signed-out states.
 *
 * Every session change also drops the cached label, so nothing of one account stays on the
 * device for the next. The password only passes through; it is never stored.
 *
 * @param authRepository the account endpoints
 * @param sessionRepository where the session is kept
 * @param labelRepository the cached label
 */
@Singleton
class AuthInteractor @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
    private val labelRepository: LabelRepository,
) {

    /**
     * @param email the account's email
     * @param password the account's password
     */
    suspend fun signIn(email: String, password: String): ApiResult<Unit> =
        authRepository.signIn(email, password).storeSession()

    /**
     * Creates the account and its generated label, then signs in.
     *
     * @param email the new account's email
     * @param password the new account's password
     */
    suspend fun signUp(email: String, password: String): ApiResult<Unit> =
        authRepository.signUp(email, password).storeSession()

    /**
     * Revokes the refresh token when the server can be reached and forgets the session either
     * way, so signing out always works offline.
     */
    suspend fun signOut() {
        sessionRepository.current()?.let { session -> authRepository.signOut(session.refreshToken) }
        sessionRepository.clear()
        labelRepository.clearCache()
    }

    private suspend fun ApiResult<AuthSession>.storeSession(): ApiResult<Unit> {
        onSuccess { session ->
            labelRepository.clearCache()
            sessionRepository.save(session)
        }
        return map { }
    }
}
