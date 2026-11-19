package com.leliukh.labelmanager.features.artist.domain.interactor

import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.network.domain.onSuccess
import com.leliukh.labelmanager.core.session.domain.repository.SessionRepository
import com.leliukh.labelmanager.features.artist.domain.models.ArtistDetails
import com.leliukh.labelmanager.features.artist.domain.repository.ArtistRepository
import com.leliukh.labelmanager.features.label.domain.models.Artist
import com.leliukh.labelmanager.features.label.domain.repository.LabelRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * An artist's profile and edits to the user's own artists.
 *
 * The session tells own artists from other labels'; after an edit the cached label is refreshed,
 * so the Roster tab shows the new name at once.
 *
 * @param sessionRepository source of the signed-in user's label id
 * @param artistRepository the artist endpoints
 * @param labelRepository the cached label
 */
@Singleton
class ArtistInteractor @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val artistRepository: ArtistRepository,
    private val labelRepository: LabelRepository,
) {

    /**
     * @param artistId id of any artist
     */
    suspend fun getArtist(artistId: String): ApiResult<ArtistDetails> =
        artistRepository.getArtist(artistId, myLabelId = sessionRepository.current()?.labelId)

    /**
     * @param artistId id of one of the user's artists
     * @param firstName new first name
     * @param lastName new last name
     * @param countryCode new ISO 3166-1 alpha-2 nationality
     */
    suspend fun updateArtist(
        artistId: String,
        firstName: String,
        lastName: String,
        countryCode: String,
    ): ApiResult<Artist> {
        val result = artistRepository.updateArtist(artistId, firstName, lastName, countryCode)
        result.onSuccess { labelRepository.refreshLabel() }
        return result
    }
}
