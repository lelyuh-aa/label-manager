package com.leliukh.labelmanager.features.label.domain.repository

import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.network.domain.onSuccess
import com.leliukh.labelmanager.features.label.domain.models.CachedLabel
import com.leliukh.labelmanager.features.label.domain.models.Label
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlin.time.Instant

/**
 * [LabelRepository] with an in-memory cache and a preset refresh result.
 *
 * @param result answer to every refresh; a success is also written to the cache
 */
class FakeLabelRepository(var result: ApiResult<Label> = ApiResult.Success(testLabel())) :
    LabelRepository {

    /** Cached labels by id, for setting up and asserting. */
    val cache = MutableStateFlow<Map<String, CachedLabel>>(emptyMap())

    /** Sync time written by successful refreshes. */
    var now: Instant = Instant.parse("2026-10-26T12:00:00Z")

    var refreshes = 0
        private set

    /** Names and countries sent to [updateLabel]. */
    val updates = mutableListOf<Pair<String, String>>()

    override fun observeLabel(labelId: String): Flow<CachedLabel?> = cache.map { it[labelId] }

    override suspend fun refreshLabel(): ApiResult<Label> {
        refreshes++
        return result.onSuccess { label -> cache.value += label.id to CachedLabel(label, now) }
    }

    override suspend fun updateLabel(name: String, countryCode: String): ApiResult<Label> {
        updates += name to countryCode
        return result.onSuccess { label -> cache.value += label.id to CachedLabel(label, now) }
    }

    override suspend fun clearCache() {
        cache.value = emptyMap()
    }
}
