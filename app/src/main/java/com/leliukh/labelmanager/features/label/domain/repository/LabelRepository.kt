package com.leliukh.labelmanager.features.label.domain.repository

import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.features.label.domain.models.CachedLabel
import com.leliukh.labelmanager.features.label.domain.models.Label
import kotlinx.coroutines.flow.Flow

/**
 * The signed-in user's label, cached on the device. Screens observe the cache and ask for a
 * refresh; they never wait for the network to show what is already known.
 */
interface LabelRepository {

    /**
     * @param labelId id of the label
     * @return the cached label, emitted again after every refresh; null until it is first loaded
     */
    fun observeLabel(labelId: String): Flow<CachedLabel?>

    /** Loads the label with its roster from the API and replaces the cached copy. */
    suspend fun refreshLabel(): ApiResult<Label>

    /**
     * Renames the label or moves it to another country, then caches the result.
     *
     * @param name new label name
     * @param countryCode new ISO 3166-1 alpha-2 country
     */
    suspend fun updateLabel(name: String, countryCode: String): ApiResult<Label>

    /** Forgets every cached label, e.g. when the user signs out. */
    suspend fun clearCache()
}
