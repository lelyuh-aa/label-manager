package com.leliukh.labelmanager.features.label.domain.interactor

import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.session.domain.repository.SessionRepository
import com.leliukh.labelmanager.features.label.domain.models.CachedLabel
import com.leliukh.labelmanager.features.label.domain.models.Label
import com.leliukh.labelmanager.features.label.domain.repository.LabelRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The signed-in user's label: the session says which label it is, [LabelRepository] keeps it.
 *
 * @param sessionRepository source of the signed-in user's label id
 * @param labelRepository the cached label
 */
@Singleton
class LabelInteractor @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val labelRepository: LabelRepository,
) {

    /**
     * @return the signed-in user's cached label; switches to the new label when another user
     *   signs in, and emits null while signed out or before the first load
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeLabel(): Flow<CachedLabel?> = sessionRepository.session
        .map { session -> session?.labelId }
        .distinctUntilChanged()
        .flatMapLatest { labelId ->
            if (labelId == null) flowOf(null) else labelRepository.observeLabel(labelId)
        }

    /** Loads the label from the server into the cache. */
    suspend fun refreshLabel(): ApiResult<Label> = labelRepository.refreshLabel()

    /** @return the signed-in user's cached label, or null when it is not cached */
    suspend fun cachedLabel(): CachedLabel? = observeLabel().first()

    /**
     * @param name new label name
     * @param countryCode new ISO 3166-1 alpha-2 country
     */
    suspend fun updateLabel(name: String, countryCode: String): ApiResult<Label> =
        labelRepository.updateLabel(name, countryCode)
}
