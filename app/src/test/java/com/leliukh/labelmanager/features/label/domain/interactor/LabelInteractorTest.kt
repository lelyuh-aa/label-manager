package com.leliukh.labelmanager.features.label.domain.interactor

import com.leliukh.labelmanager.core.session.domain.repository.FakeSessionRepository
import com.leliukh.labelmanager.core.session.domain.repository.testSession
import com.leliukh.labelmanager.features.label.domain.models.CachedLabel
import com.leliukh.labelmanager.features.label.domain.repository.FakeLabelRepository
import com.leliukh.labelmanager.features.label.domain.repository.testLabel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LabelInteractorTest {

    private val sessions = FakeSessionRepository()
    private val labels = FakeLabelRepository()
    private val interactor = LabelInteractor(sessions, labels)

    @Test
    fun `the signed-in user's label is observed from the cache`() = runTest {
        val label = CachedLabel(testLabel(), labels.now)
        labels.cache.value = mapOf("label-1" to label)
        sessions.save(testSession())

        assertEquals(label, interactor.observeLabel().first())
    }

    @Test
    fun `nothing is observed while signed out`() = runTest {
        labels.cache.value = mapOf("label-1" to CachedLabel(testLabel(), labels.now))

        assertNull(interactor.observeLabel().first())
    }
}
