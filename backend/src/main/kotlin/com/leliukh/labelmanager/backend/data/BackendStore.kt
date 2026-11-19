package com.leliukh.labelmanager.backend.data

import com.leliukh.labelmanager.backend.domain.models.ActivityEvent
import com.leliukh.labelmanager.backend.domain.models.Listing
import com.leliukh.labelmanager.backend.domain.models.Artist
import com.leliukh.labelmanager.backend.domain.models.RefreshToken
import com.leliukh.labelmanager.backend.domain.models.Label
import com.leliukh.labelmanager.backend.domain.models.Deal
import com.leliukh.labelmanager.backend.domain.models.User
import kotlin.time.Instant

/**
 * Persistent state of the backend with transactional access.
 *
 * A [write] block runs atomically and in isolation from other writes: either every change it makes
 * is stored, or, when it throws, none is. Blocks are plain functions, so they cannot suspend while
 * holding the transaction.
 */
interface BackendStore {

    /**
     * @param block reads the state
     * @return the block's result
     */
    suspend fun <T> read(block: StoreReader.() -> T): T

    /**
     * @param block reads and changes the state; throwing rolls every change back
     * @return the block's result
     */
    suspend fun <T> write(block: StoreWriter.() -> T): T
}

/** Read access inside a [BackendStore] transaction. */
interface StoreReader {

    /**
     * @param userId account id
     * @return the account, or null
     */
    fun findUser(userId: String): User?

    /**
     * @param email normalised email
     * @return the account with this email, or null
     */
    fun findUserByEmail(email: String): User?

    /**
     * @param labelId label id
     * @return the label, or null
     */
    fun findLabel(labelId: String): Label?

    /** @return every label, user-owned and rival labels */
    fun allLabels(): List<Label>

    /**
     * @param artistId artist id
     * @return the artist, or null
     */
    fun findArtist(artistId: String): Artist?

    /**
     * @param labelId label id
     * @return the label's roster
     */
    fun artistsOfLabel(labelId: String): List<Artist>

    /**
     * @param listingId listing id
     * @return the open listing, or null
     */
    fun findListing(listingId: String): Listing?

    /**
     * @param artistId artist id
     * @return the artist's open listing, or null
     */
    fun findListingOfArtist(artistId: String): Listing?

    /** @return every open listing */
    fun allListings(): List<Listing>

    /**
     * @param artistId artist id
     * @return the artist's deals in any order
     */
    fun dealsOfArtist(artistId: String): List<Deal>

    /**
     * @param labelId label id
     * @return the label's activity events in any order
     */
    fun activityOfLabel(labelId: String): List<ActivityEvent>

    /**
     * @param tokenHash hash of a refresh token
     * @return the stored refresh token, or null
     */
    fun findRefreshToken(tokenHash: String): RefreshToken?

    /** @return the moment the rival labels simulation has been run up to; null before seeding */
    fun simulatedUntil(): Instant?
}

/**
 * Write access inside a [BackendStore.write] transaction. Every `save` inserts or replaces by id.
 */
interface StoreWriter : StoreReader {

    /** @param user account to store */
    fun saveUser(user: User)

    /** @param label label to store */
    fun saveLabel(label: Label)

    /** @param artist artist to store */
    fun saveArtist(artist: Artist)

    /** @param listing listing to store */
    fun saveListing(listing: Listing)

    /** @param listingId id of the listing to remove */
    fun deleteListing(listingId: String)

    /** @param deal deal to store */
    fun saveDeal(deal: Deal)

    /** @param event activity event to store */
    fun saveActivity(event: ActivityEvent)

    /**
     * @param labelId label whose activity events become read
     * @param eventId the one event to mark read; null marks all of them
     */
    fun markActivityRead(labelId: String, eventId: String? = null)

    /** @param token refresh token to store */
    fun saveRefreshToken(token: RefreshToken)

    /** @param tokenHash hash of the refresh token to remove */
    fun deleteRefreshToken(tokenHash: String)

    /** @param instant the moment the rival labels simulation has been run up to */
    fun setSimulatedUntil(instant: Instant)
}
