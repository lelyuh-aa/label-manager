package com.leliukh.labelmanager.features.auth.data.repository

import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.network.domain.map
import com.leliukh.labelmanager.core.session.data.models.toSession
import com.leliukh.labelmanager.core.session.domain.models.AuthSession
import com.leliukh.labelmanager.features.auth.data.api.AuthApi
import com.leliukh.labelmanager.features.auth.domain.repository.AuthRepository

/**
 * [AuthRepository] over the API.
 *
 * The email is trimmed before it is sent; the password is passed through untouched.
 *
 * @param api the auth endpoints
 */
class AuthRepositoryImpl(
    private val api: AuthApi,
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): ApiResult<AuthSession> =
        api.login(email.trim(), password).map { it.toSession() }

    override suspend fun signUp(email: String, password: String): ApiResult<AuthSession> =
        api.register(email.trim(), password).map { it.toSession() }

    override suspend fun signOut(refreshToken: String): ApiResult<Unit> =
        api.logout(refreshToken)
}
