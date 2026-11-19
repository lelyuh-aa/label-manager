package com.leliukh.labelmanager.backend.data.memory

import com.leliukh.labelmanager.backend.domain.models.ActivityEvent
import com.leliukh.labelmanager.backend.domain.models.Listing
import com.leliukh.labelmanager.backend.domain.models.Artist
import com.leliukh.labelmanager.backend.domain.models.RefreshToken
import com.leliukh.labelmanager.backend.domain.models.Label
import com.leliukh.labelmanager.backend.domain.models.Deal
import com.leliukh.labelmanager.backend.domain.models.User
import kotlin.time.Instant
import kotlinx.serialization.Serializable

/**
 * The whole state of an [InMemoryBackendStore] as written to its snapshot file.
 *
 * @property version format version; a snapshot of another version is discarded
 * @property users accounts
 * @property labels user-owned labels and rival labels
 * @property artists artists of every label
 * @property listings open marketings
 * @property deals completed deals
 * @property activity activity events of user-owned labels
 * @property refreshTokens refresh tokens that can still be exchanged
 * @property simulatedUntil the moment the rival labels simulation has been run up to
 */
@Serializable
data class StoreSnapshot(
    val version: Int = CURRENT_VERSION,
    val users: List<User> = emptyList(),
    val labels: List<Label> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val listings: List<Listing> = emptyList(),
    val deals: List<Deal> = emptyList(),
    val activity: List<ActivityEvent> = emptyList(),
    val refreshTokens: List<RefreshToken> = emptyList(),
    val simulatedUntil: Instant? = null,
) {

    companion object {
        const val CURRENT_VERSION: Int = 1
    }
}

/** Mutable working copy of the store, keyed by id in insertion order. */
internal class MemoryState(
    val users: LinkedHashMap<String, User>,
    val labels: LinkedHashMap<String, Label>,
    val artists: LinkedHashMap<String, Artist>,
    val listings: LinkedHashMap<String, Listing>,
    val deals: LinkedHashMap<String, Deal>,
    val activity: LinkedHashMap<String, ActivityEvent>,
    val refreshTokens: LinkedHashMap<String, RefreshToken>,
    var simulatedUntil: Instant?,
) {

    fun copy(): MemoryState =
        MemoryState(
            users = LinkedHashMap(users),
            labels = LinkedHashMap(labels),
            artists = LinkedHashMap(artists),
            listings = LinkedHashMap(listings),
            deals = LinkedHashMap(deals),
            activity = LinkedHashMap(activity),
            refreshTokens = LinkedHashMap(refreshTokens),
            simulatedUntil = simulatedUntil,
        )

    fun toSnapshot(): StoreSnapshot =
        StoreSnapshot(
            users = users.values.toList(),
            labels = labels.values.toList(),
            artists = artists.values.toList(),
            listings = listings.values.toList(),
            deals = deals.values.toList(),
            activity = activity.values.toList(),
            refreshTokens = refreshTokens.values.toList(),
            simulatedUntil = simulatedUntil,
        )

    companion object {

        fun from(snapshot: StoreSnapshot): MemoryState =
            MemoryState(
                users = snapshot.users.associateByTo(LinkedHashMap()) { it.id },
                labels = snapshot.labels.associateByTo(LinkedHashMap()) { it.id },
                artists = snapshot.artists.associateByTo(LinkedHashMap()) { it.id },
                listings = snapshot.listings.associateByTo(LinkedHashMap()) { it.id },
                deals = snapshot.deals.associateByTo(LinkedHashMap()) { it.id },
                activity = snapshot.activity.associateByTo(LinkedHashMap()) { it.id },
                refreshTokens =
                    snapshot.refreshTokens.associateByTo(LinkedHashMap()) { it.tokenHash },
                simulatedUntil = snapshot.simulatedUntil,
            )
    }
}
