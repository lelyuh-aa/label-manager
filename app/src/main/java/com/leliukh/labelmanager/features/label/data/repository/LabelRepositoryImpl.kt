package com.leliukh.labelmanager.features.label.data.repository

import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.network.domain.map
import com.leliukh.labelmanager.core.network.domain.onSuccess
import com.leliukh.labelmanager.features.label.data.api.LabelApi
import com.leliukh.labelmanager.features.label.data.db.dao.LabelDao
import com.leliukh.labelmanager.features.label.data.models.toDomain
import com.leliukh.labelmanager.features.label.data.models.toEntity
import com.leliukh.labelmanager.features.label.domain.models.CachedLabel
import com.leliukh.labelmanager.features.label.domain.models.Label
import com.leliukh.labelmanager.features.label.domain.repository.LabelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

/**
 * [LabelRepository] over the API with a Room cache.
 *
 * @param api the label endpoints
 * @param dao the cache
 * @param clock source of the sync time
 */
class LabelRepositoryImpl(
    private val api: LabelApi,
    private val dao: LabelDao,
    private val clock: Clock = Clock.System,
) : LabelRepository {

    override fun observeLabel(labelId: String): Flow<CachedLabel?> =
        dao.observeLabel(labelId).map { it?.toDomain() }

    override suspend fun refreshLabel(): ApiResult<Label> =
        api.getLabel().map { it.toDomain() }.cached()

    override suspend fun updateLabel(name: String, countryCode: String): ApiResult<Label> =
        api.updateLabel(name.trim(), countryCode).map { it.toDomain() }.cached()

    override suspend fun clearCache() {
        dao.clear()
    }

    private suspend fun ApiResult<Label>.cached(): ApiResult<Label> {
        onSuccess { label ->
            dao.replaceLabel(
                label = label.toEntity(syncedAt = clock.now()),
                artists = label.artists.map { it.toEntity(labelId = label.id) },
            )
        }
        return this
    }
}
