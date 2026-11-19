package com.leliukh.labelmanager.features.activity.data.api

import com.leliukh.labelmanager.contract.dto.activity.ActivityPageDto
import com.leliukh.labelmanager.contract.routes.ApiQuery
import com.leliukh.labelmanager.contract.routes.ApiRoutes
import com.leliukh.labelmanager.core.network.data.apiRequest
import com.leliukh.labelmanager.core.network.domain.ApiResult
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The `activity` endpoints of the API.
 *
 * @param client the shared API client
 */
@Singleton
class ActivityApi @Inject constructor(
    private val client: HttpClient,
) {

    /**
     * @param cursor cursor of the page; null for the newest one
     * @param limit page size
     */
    suspend fun getActivity(cursor: String?, limit: Int): ApiResult<ActivityPageDto> =
        apiRequest {
            client.get(ApiRoutes.ACTIVITY) {
                parameter(ApiQuery.CURSOR, cursor)
                parameter(ApiQuery.LIMIT, limit)
            }
        }

    /** Marks every event of the caller's label read. */
    suspend fun markAllRead(): ApiResult<Unit> =
        apiRequest { client.post(ApiRoutes.ACTIVITY_READ) }

    /** @param eventId the event to mark read */
    suspend fun markRead(eventId: String): ApiResult<Unit> =
        apiRequest { client.post(ApiRoutes.activityEventRead(eventId)) }
}
