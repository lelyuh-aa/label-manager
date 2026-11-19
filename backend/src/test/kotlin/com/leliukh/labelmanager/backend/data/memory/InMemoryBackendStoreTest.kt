package com.leliukh.labelmanager.backend.data.memory

import com.leliukh.labelmanager.backend.domain.models.Label
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class InMemoryBackendStoreTest {

    @get:Rule val folder = TemporaryFolder()

    @Test
    fun `a throwing write changes nothing`() = runTest {
        val store = InMemoryBackendStore(snapshotFile = null)
        store.write { saveLabel(label(budget = 100)) }

        runCatching {
            store.write {
                saveLabel(label(budget = 0))
                error("rejected")
            }
        }

        assertEquals(100L, store.read { findLabel("label-1")?.budget })
    }

    @Test
    fun `the state survives a restart through the snapshot file`() = runTest {
        val file = folder.root.resolve("nested/store.json")
        val first = InMemoryBackendStore(file, Dispatchers.Unconfined)
        first.write {
            saveLabel(label(budget = 42))
            setSimulatedUntil(Instant.parse("2026-10-25T10:00:00Z"))
        }

        val second = InMemoryBackendStore(file, Dispatchers.Unconfined)

        assertEquals(42L, second.read { findLabel("label-1")?.budget })
        assertEquals(Instant.parse("2026-10-25T10:00:00Z"), second.read { simulatedUntil() })
        assertTrue(file.parentFile.listFiles().orEmpty().none { it.name.endsWith(".tmp") })
    }

    @Test
    fun `a corrupt snapshot is discarded`() = runTest {
        val file = folder.newFile("store.json").apply { writeText("{not json") }

        val store = InMemoryBackendStore(file, Dispatchers.Unconfined)

        assertNull(store.read { findLabel("label-1") })
        assertTrue(store.read { allLabels() }.isEmpty())
    }

    @Test
    fun `concurrent writes do not lose updates`() = runTest {
        val store = InMemoryBackendStore(snapshotFile = null)
        store.write { saveLabel(label(budget = 0)) }

        withContext(Dispatchers.Default) {
            List(200) {
                    async {
                        store.write {
                            val current = requireNotNull(findLabel("label-1"))
                            saveLabel(current.copy(budget = current.budget + 1))
                        }
                    }
                }
                .awaitAll()
        }

        assertEquals(200L, store.read { findLabel("label-1")?.budget })
    }

    private fun label(budget: Long) =
        Label(
            id = "label-1",
            name = "Rhein Valley Records",
            countryCode = "DE",
            budget = budget,
            ownerUserId = null,
        )
}
