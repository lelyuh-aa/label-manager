package com.leliukh.labelmanager.features.auth.domain.repository

import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.session.domain.models.AuthSession

/**
 * The account endpoints of the server. Storing the returned session is up to the caller.
 */
interface AuthRepository {

    /**
     * @param email the account's email
     * @param password the account's password
     * @return the new session
     */
    suspend fun signIn(email: String, password: String): ApiResult<AuthSession>

    /**
     * Creates the account and its generated label.
     *
     * @param email the new account's email
     * @param password the new account's password
     * @return the new account's session
     */
    suspend fun signUp(email: String, password: String): ApiResult<AuthSession>

    /**
     * Revokes the refresh token on the server.
     *
     * @param refreshToken the token to revoke
     */
    suspend fun signOut(refreshToken: String): ApiResult<Unit>
}
