package com.leliukh.labelmanager.features.artist.data.repository

import com.leliukh.labelmanager.contract.dto.artist.UpdateArtistRequestDto
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.network.domain.map
import com.leliukh.labelmanager.features.artist.data.api.ArtistApi
import com.leliukh.labelmanager.features.artist.data.models.toDomain
import com.leliukh.labelmanager.features.artist.domain.models.ArtistDetails
import com.leliukh.labelmanager.features.artist.domain.repository.ArtistRepository
import com.leliukh.labelmanager.features.label.data.models.toDomain
import com.leliukh.labelmanager.features.label.domain.models.Artist

/**
 * [ArtistRepository] over the API.
 *
 * @param api the artist endpoints
 */
class ArtistRepositoryImpl(
    private val api: ArtistApi,
) : ArtistRepository {

    override suspend fun getArtist(artistId: String, myLabelId: String?): ApiResult<ArtistDetails> =
        api.getArtist(artistId).map { it.toDomain(myLabelId) }

    override suspend fun updateArtist(
        artistId: String,
        firstName: String,
        lastName: String,
        countryCode: String,
    ): ApiResult<Artist> {
        val request = UpdateArtistRequestDto(
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            countryCode = countryCode,
        )
        return api.updateArtist(artistId, request).map { it.toDomain() }
    }
}
