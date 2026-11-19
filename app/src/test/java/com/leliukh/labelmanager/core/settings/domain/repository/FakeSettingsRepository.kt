package com.leliukh.labelmanager.core.settings.domain.repository

import com.leliukh.labelmanager.core.settings.domain.models.AppSettings
import com.leliukh.labelmanager.core.settings.domain.models.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** [SettingsRepository] in memory. */
class FakeSettingsRepository : SettingsRepository {

    override val settings = MutableStateFlow(AppSettings())

    override suspend fun setThemeMode(mode: ThemeMode) {
        settings.update { it.copy(themeMode = mode) }
    }

    override suspend fun setSaleAlertsEnabled(isEnabled: Boolean) {
        settings.update { it.copy(isSaleAlertsEnabled = isEnabled) }
    }

    override suspend fun setBudgetHidden(isHidden: Boolean) {
        settings.update { it.copy(isBudgetHidden = isHidden) }
    }
}
