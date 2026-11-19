package com.leliukh.labelmanager.features.auth.domain.interactor

import com.leliukh.labelmanager.core.network.domain.ApiError
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.session.domain.repository.FakeSessionRepository
import com.leliukh.labelmanager.core.session.domain.repository.testSession
import com.leliukh.labelmanager.features.auth.domain.repository.FakeAuthRepository
import com.leliukh.labelmanager.features.label.domain.models.CachedLabel
import com.leliukh.labelmanager.features.label.domain.repository.FakeLabelRepository
import com.leliukh.labelmanager.features.label.domain.repository.testLabel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthInteractorTest {

    private val repository = FakeAuthRepository()
    private val sessions = FakeSessionRepository()
    private val labels = FakeLabelRepository()
    private val interactor = AuthInteractor(repository, sessions, labels)

    @Test
    fun `a successful sign-in stores the returned session`() = runTest {
        val result = interactor.signIn("manager@rheinvalley.fm", "backstage2026")

        assertEquals(ApiResult.Success(Unit), result)
        assertEquals(testSession(), sessions.state.value)
    }

    @Test
    fun `a failed sign-up stores nothing`() = runTest {
        repository.result = ApiResult.Failure(ApiError.Network)

        val result = interactor.signUp("manager@rheinvalley.fm", "backstage2026")

        assertEquals(ApiResult.Failure(ApiError.Network), result)
        assertNull(sessions.state.value)
    }

    @Test
    fun `signing out forgets the session and the label even when offline`() = runTest {
        sessions.save(testSession(refresh = "refresh-1"))
        labels.cache.value = mapOf("label-1" to CachedLabel(testLabel(), labels.now))
        repository.signOutResult = ApiResult.Failure(ApiError.Network)

        interactor.signOut()

        assertEquals(listOf("refresh-1"), repository.signOuts)
        assertNull(sessions.state.value)
        assertTrue(labels.cache.value.isEmpty())
    }
}
