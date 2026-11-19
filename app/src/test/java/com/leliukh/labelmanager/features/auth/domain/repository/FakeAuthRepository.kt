package com.leliukh.labelmanager.features.auth.domain.repository

import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.session.domain.models.AuthSession
import com.leliukh.labelmanager.core.session.domain.repository.testSession

/**
 * [AuthRepository] that answers with preset results and remembers what it was asked.
 *
 * @param result answer to every sign-in and sign-up
 * @param signOutResult answer to every sign-out
 */
class FakeAuthRepository(
    var result: ApiResult<AuthSession> = ApiResult.Success(testSession()),
    var signOutResult: ApiResult<Unit> = ApiResult.Success(Unit),
) : AuthRepository {

    val signIns = mutableListOf<Pair<String, String>>()
    val signUps = mutableListOf<Pair<String, String>>()
    val signOuts = mutableListOf<String>()

    override suspend fun signIn(email: String, password: String): ApiResult<AuthSession> {
        signIns += email to password
        return result
    }

    override suspend fun signUp(email: String, password: String): ApiResult<AuthSession> {
        signUps += email to password
        return result
    }

    override suspend fun signOut(refreshToken: String): ApiResult<Unit> {
        signOuts += refreshToken
        return signOutResult
    }
}
