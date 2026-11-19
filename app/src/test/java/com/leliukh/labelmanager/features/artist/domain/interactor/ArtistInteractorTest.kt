package com.leliukh.labelmanager.features.artist.domain.interactor

import com.leliukh.labelmanager.core.network.domain.ApiError
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.session.domain.repository.FakeSessionRepository
import com.leliukh.labelmanager.core.session.domain.repository.testSession
import com.leliukh.labelmanager.features.artist.domain.repository.FakeArtistRepository
import com.leliukh.labelmanager.features.label.domain.repository.FakeLabelRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArtistInteractorTest {

    private val sessions = FakeSessionRepository(testSession())
    private val artists = FakeArtistRepository()
    private val labels = FakeLabelRepository()
    private val interactor = ArtistInteractor(sessions, artists, labels)

    @Test
    fun `an artist of the user's label is marked as theirs`() = runTest {
        val details = (interactor.getArtist("artist-1") as ApiResult.Success).data

        assertTrue(details.isMine)
    }

    @Test
    fun `a saved edit refreshes the cached roster`() = runTest {
        interactor.updateArtist("artist-1", "Théo", "Marchand-Roux", "FR")

        assertEquals(1, labels.refreshes)
    }

    @Test
    fun `a failed edit leaves the cache alone`() = runTest {
        artists.updateResult = ApiResult.Failure(ApiError.Network)

        interactor.updateArtist("artist-1", "Théo", "Marchand-Roux", "FR")

        assertFalse(labels.refreshes > 0)
    }
}
