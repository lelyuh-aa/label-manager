package com.leliukh.labelmanager.features.artist.data.api

import com.leliukh.labelmanager.contract.dto.artist.ArtistDetailsDto
import com.leliukh.labelmanager.contract.dto.artist.ArtistDto
import com.leliukh.labelmanager.contract.dto.artist.UpdateArtistRequestDto
import com.leliukh.labelmanager.contract.routes.ApiRoutes
import com.leliukh.labelmanager.core.network.data.apiRequest
import com.leliukh.labelmanager.core.network.domain.ApiResult
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The `artists` endpoints of the API.
 *
 * @param client the shared API client
 */
@Singleton
class ArtistApi @Inject constructor(
    private val client: HttpClient,
) {

    /**
     * @param artistId id of any artist, own or another label's
     */
    suspend fun getArtist(artistId: String): ApiResult<ArtistDetailsDto> =
        apiRequest { client.get(artistPath(artistId)) }

    /**
     * @param artistId id of one of the caller's artists
     * @param request the new names and nationality
     */
    suspend fun updateArtist(
        artistId: String,
        request: UpdateArtistRequestDto,
    ): ApiResult<ArtistDto> = apiRequest {
        client.patch(artistPath(artistId)) { setBody(request) }
    }

    private fun artistPath(artistId: String): String =
        ApiRoutes.ARTIST.replace("{${ApiRoutes.ARTIST_ID}}", artistId)
}
