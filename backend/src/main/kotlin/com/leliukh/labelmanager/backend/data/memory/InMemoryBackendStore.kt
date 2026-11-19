package com.leliukh.labelmanager.backend.data.memory

import com.leliukh.labelmanager.backend.data.BackendStore
import com.leliukh.labelmanager.backend.data.StoreReader
import com.leliukh.labelmanager.backend.data.StoreWriter
import com.leliukh.labelmanager.backend.domain.models.ActivityEvent
import com.leliukh.labelmanager.backend.domain.models.Listing
import com.leliukh.labelmanager.backend.domain.models.Artist
import com.leliukh.labelmanager.backend.domain.models.RefreshToken
import com.leliukh.labelmanager.backend.domain.models.Label
import com.leliukh.labelmanager.backend.domain.models.Deal
import com.leliukh.labelmanager.backend.domain.models.User
import java.io.File
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * [BackendStore] that keeps the state in memory and, when a file is given, mirrors it to a JSON
 * snapshot after every write.
 *
 * Transactions are serialised by a mutex. A write works on a copy of the state, which replaces the
 * current one only after the block returns and the snapshot is written, so a throwing block or a
 * failed disk write leaves the state untouched. The snapshot is written to a temporary file and
 * renamed over the old one, so a crash never leaves a half-written file. The state is loaded lazily
 * on the first access; an unreadable snapshot is discarded.
 *
 * @param snapshotFile file to persist the state to; null keeps it in memory only
 * @param ioDispatcher dispatcher for file access
 */
class InMemoryBackendStore(
    private val snapshotFile: File?,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BackendStore {

    private val mutex = Mutex()
    private var state: MemoryState? = null

    override suspend fun <T> read(block: StoreReader.() -> T): T = mutex.withLock {
        MemoryAccess(loadedState()).block()
    }

    override suspend fun <T> write(block: StoreWriter.() -> T): T = mutex.withLock {
        val working = loadedState().copy()
        val result = MemoryAccess(working).block()
        snapshotFile?.let { file -> withContext(ioDispatcher) { writeSnapshot(file, working) } }
        state = working
        result
    }

    private suspend fun loadedState(): MemoryState =
        state
            ?: withContext(ioDispatcher) {
                    MemoryState.from(snapshotFile?.let(::readSnapshot) ?: StoreSnapshot())
                }
                .also { state = it }

    private fun readSnapshot(file: File): StoreSnapshot? = runCatching {
        if (!file.exists()) return null
        snapshotJson.decodeFromString(StoreSnapshot.serializer(), file.readText())
    }
        .getOrNull()
        ?.takeIf { it.version == StoreSnapshot.CURRENT_VERSION }

    private fun writeSnapshot(file: File, state: MemoryState) {
        file.absoluteFile.parentFile?.mkdirs()
        val temporary = File(file.absolutePath + ".tmp")
        temporary.writeText(
            snapshotJson.encodeToString(StoreSnapshot.serializer(), state.toSnapshot())
        )
        if (!temporary.renameTo(file)) {
            file.delete()
            check(temporary.renameTo(file)) {
                "Cannot replace the snapshot at ${file.absolutePath}"
            }
        }
    }

    private companion object {
        val snapshotJson = Json { ignoreUnknownKeys = true }
    }
}

private class MemoryAccess(private val state: MemoryState) : StoreWriter {

    override fun findUser(userId: String): User? = state.users[userId]

    override fun findUserByEmail(email: String): User? =
        state.users.values.firstOrNull { it.email == email }

    override fun findLabel(labelId: String): Label? = state.labels[labelId]

    override fun allLabels(): List<Label> = state.labels.values.toList()

    override fun findArtist(artistId: String): Artist? = state.artists[artistId]

    override fun artistsOfLabel(labelId: String): List<Artist> =
        state.artists.values.filter { it.labelId == labelId }

    override fun findListing(listingId: String): Listing? = state.listings[listingId]

    override fun findListingOfArtist(artistId: String): Listing? =
        state.listings.values.firstOrNull { it.artistId == artistId }

    override fun allListings(): List<Listing> = state.listings.values.toList()

    override fun dealsOfArtist(artistId: String): List<Deal> =
        state.deals.values.filter { it.artistId == artistId }

    override fun activityOfLabel(labelId: String): List<ActivityEvent> =
        state.activity.values.filter { it.labelId == labelId }

    override fun findRefreshToken(tokenHash: String): RefreshToken? = state.refreshTokens[tokenHash]

    override fun simulatedUntil(): Instant? = state.simulatedUntil

    override fun saveUser(user: User) {
        state.users[user.id] = user
    }

    override fun saveLabel(label: Label) {
        state.labels[label.id] = label
    }

    override fun saveArtist(artist: Artist) {
        state.artists[artist.id] = artist
    }

    override fun saveListing(listing: Listing) {
        state.listings[listing.id] = listing
    }

    override fun deleteListing(listingId: String) {
        state.listings.remove(listingId)
    }

    override fun saveDeal(deal: Deal) {
        state.deals[deal.id] = deal
    }

    override fun saveActivity(event: ActivityEvent) {
        state.activity[event.id] = event
    }

    override fun markActivityRead(labelId: String, eventId: String?) {
        state.activity.replaceAll { id, event ->
            val isTarget = event.labelId == labelId && (eventId == null || id == eventId)
            if (isTarget && !event.isRead) event.copy(isRead = true) else event
        }
    }

    override fun saveRefreshToken(token: RefreshToken) {
        state.refreshTokens[token.tokenHash] = token
    }

    override fun deleteRefreshToken(tokenHash: String) {
        state.refreshTokens.remove(tokenHash)
    }

    override fun setSimulatedUntil(instant: Instant) {
        state.simulatedUntil = instant
    }
}
