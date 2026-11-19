package com.leliukh.labelmanager.features.label.data.api

import com.leliukh.labelmanager.contract.dto.label.LabelDto
import com.leliukh.labelmanager.contract.dto.label.UpdateLabelRequestDto
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
 * The `label` endpoints of the API.
 *
 * @param client the shared API client
 */
@Singleton
class LabelApi @Inject constructor(
    private val client: HttpClient,
) {

    /** The signed-in user's label with its roster. */
    suspend fun getLabel(): ApiResult<LabelDto> = apiRequest { client.get(ApiRoutes.LABEL) }

    /**
     * @param name new label name
     * @param countryCode new ISO 3166-1 alpha-2 country
     */
    suspend fun updateLabel(name: String, countryCode: String): ApiResult<LabelDto> = apiRequest {
        client.patch(ApiRoutes.LABEL) { setBody(UpdateLabelRequestDto(name, countryCode)) }
    }
}
