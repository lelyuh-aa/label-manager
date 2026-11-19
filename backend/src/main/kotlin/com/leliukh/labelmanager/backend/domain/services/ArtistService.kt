package com.leliukh.labelmanager.backend.domain.services

import com.leliukh.labelmanager.backend.data.BackendStore
import com.leliukh.labelmanager.backend.domain.errors.DomainException
import com.leliukh.labelmanager.backend.domain.models.ArtistDetailsView
import com.leliukh.labelmanager.backend.domain.models.ArtistView
import com.leliukh.labelmanager.backend.domain.models.Deal
import com.leliukh.labelmanager.backend.domain.validation.validate
import com.leliukh.labelmanager.contract.rules.ContractLimits

/**
 * Artist details and editing by the owner.
 *
 * @param store persistent state
 */
class ArtistService(private val store: BackendStore) {

    /**
     * @param artistId id of any artist
     * @return the artist with the owning label and deal history, newest first
     */
    suspend fun details(artistId: String): ArtistDetailsView = store.read {
        val artist = requireArtist(artistId)
        ArtistDetailsView(
            artist = artistView(artist),
            label = requireLabel(artist.labelId),
            deals =
                dealsOfArtist(artistId)
                    .sortedWith(compareByDescending<Deal> { it.signedAt }.thenBy { it.id })
                    .map { dealView(it) },
        )
    }

    /**
     * @param callerLabelId id of the caller's label, which must own the artist
     * @param artistId id of the artist
     * @param firstName new first name, trimmed; null keeps the current one
     * @param lastName new last name, trimmed; null keeps the current one
     * @param countryCode new ISO 3166-1 alpha-2 nationality; null keeps the current one
     * @return the updated artist
     */
    suspend fun update(
        callerLabelId: String,
        artistId: String,
        firstName: String?,
        lastName: String?,
        countryCode: String?,
    ): ArtistView {
        val first = firstName?.trim()
        val last = lastName?.trim()
        val nameRange = ContractLimits.ARTIST_NAME_MIN_LENGTH..ContractLimits.ARTIST_NAME_MAX_LENGTH
        validate {
            first?.let { length("firstName", it, nameRange) }
            last?.let { length("lastName", it, nameRange) }
            countryCode?.let { country("countryCode", it) }
        }
        return store.write {
            val artist = requireArtist(artistId)
            if (artist.labelId != callerLabelId) {
                throw DomainException.Forbidden("Only the owner can edit an artist")
            }
            val updated =
                artist.copy(
                    firstName = first ?: artist.firstName,
                    lastName = last ?: artist.lastName,
                    countryCode = countryCode ?: artist.countryCode,
                )
            saveArtist(updated)
            artistView(updated)
        }
    }
}
