package com.leliukh.labelmanager.core.settings.domain.repository

import com.leliukh.labelmanager.core.settings.domain.models.AppSettings
import com.leliukh.labelmanager.core.settings.domain.models.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * The user's preferences, kept on the device.
 */
interface SettingsRepository {

    /** The current settings, emitted again after every change. */
    val settings: Flow<AppSettings>

    /** @param mode the new palette */
    suspend fun setThemeMode(mode: ThemeMode)

    /** @param isEnabled whether to announce sales */
    suspend fun setSaleAlertsEnabled(isEnabled: Boolean)

    /** @param isHidden whether to mask the label budget */
    suspend fun setBudgetHidden(isHidden: Boolean)
}
