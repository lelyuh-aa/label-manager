package com.leliukh.labelmanager.core.settings.domain.models

/** Light or dark palette. */
enum class ThemeMode {
    System,
    Light,
    Dark,
}

/**
 * Preferences the user sets on the Profile tab.
 *
 * @property themeMode light, dark, or following the system
 * @property isSaleAlertsEnabled whether sales of the user's artists are announced
 * @property isBudgetHidden whether the label budget is masked on the Roster and Market tabs
 */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.System,
    val isSaleAlertsEnabled: Boolean = true,
    val isBudgetHidden: Boolean = false,
)
