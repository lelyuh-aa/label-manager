package com.leliukh.labelmanager.features.home.presentation.models

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons

/**
 * Tabs of the home screen, in navigation bar order.
 *
 * @property labelRes tab name
 * @property icon tab icon
 */
enum class HomeTab(@StringRes val labelRes: Int, val icon: ImageVector) {
    Roster(R.string.home_tab_roster, AppIcons.Users),
    Market(R.string.home_tab_market, AppIcons.Swap),
    Activity(R.string.home_tab_activity, AppIcons.Bell),
    Profile(R.string.home_tab_profile, AppIcons.Profile),
}
