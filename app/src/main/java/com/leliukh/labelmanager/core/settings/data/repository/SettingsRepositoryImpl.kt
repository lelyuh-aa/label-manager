package com.leliukh.labelmanager.core.settings.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.leliukh.labelmanager.core.settings.domain.models.AppSettings
import com.leliukh.labelmanager.core.settings.domain.models.ThemeMode
import com.leliukh.labelmanager.core.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

private const val KEY_THEME_MODE = "themeMode"
private const val KEY_SALE_ALERTS = "saleAlerts"
private const val KEY_BUDGET_HIDDEN = "budgetHidden"

/**
 * [SettingsRepository] in a small preferences file. Nothing in it is personal, so it needs no
 * encryption; the theme is read on start, before the first frame.
 *
 * @param preferences the preferences file
 */
class SettingsRepositoryImpl(
    private val preferences: SharedPreferences,
) : SettingsRepository {

    override val settings: Flow<AppSettings> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(read())
        }
        trySend(read())
        preferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }.flowOn(Dispatchers.IO)

    override suspend fun setThemeMode(mode: ThemeMode) {
        withContext(Dispatchers.IO) { preferences.edit { putString(KEY_THEME_MODE, mode.name) } }
    }

    override suspend fun setSaleAlertsEnabled(isEnabled: Boolean) {
        withContext(Dispatchers.IO) { preferences.edit { putBoolean(KEY_SALE_ALERTS, isEnabled) } }
    }

    override suspend fun setBudgetHidden(isHidden: Boolean) {
        withContext(Dispatchers.IO) { preferences.edit { putBoolean(KEY_BUDGET_HIDDEN, isHidden) } }
    }

    private fun read(): AppSettings = AppSettings(
        themeMode = preferences.getString(KEY_THEME_MODE, null)
            ?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } }
            ?: ThemeMode.System,
        isSaleAlertsEnabled = preferences.getBoolean(KEY_SALE_ALERTS, true),
        isBudgetHidden = preferences.getBoolean(KEY_BUDGET_HIDDEN, false),
    )
}
