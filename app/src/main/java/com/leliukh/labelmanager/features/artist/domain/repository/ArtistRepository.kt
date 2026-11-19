package com.leliukh.labelmanager.features.artist.domain.repository

import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.features.artist.domain.models.ArtistDetails
import com.leliukh.labelmanager.features.label.domain.models.Artist

/**
 * Any artist of the game, read from the server.
 */
interface ArtistRepository {

    /**
     * @param artistId id of the artist
     * @param myLabelId id of the signed-in user's label, to mark own artists
     */
    suspend fun getArtist(artistId: String, myLabelId: String?): ApiResult<ArtistDetails>

    /**
     * Renames one of the user's artists or changes their nationality. Names are trimmed.
     *
     * @param artistId id of the artist
     * @param firstName new first name
     * @param lastName new last name
     * @param countryCode new ISO 3166-1 alpha-2 nationality
     */
    suspend fun updateArtist(
        artistId: String,
        firstName: String,
        lastName: String,
        countryCode: String,
    ): ApiResult<Artist>
}
