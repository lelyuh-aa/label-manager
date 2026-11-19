package com.leliukh.labelmanager.core.design_system.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing on a 4 dp grid plus the fixed sizes shared by components. Read them through
 * [AppThemeWrapper.dimens].
 *
 * @property space1 4 dp
 * @property space2 8 dp
 * @property space3 12 dp
 * @property space4 16 dp, the horizontal screen margin
 * @property space5 20 dp
 * @property space6 24 dp
 * @property space8 32 dp
 * @property screenTitleInset horizontal inset of large titles and section headers
 * @property controlHeight height of text fields, search fields and buttons
 * @property chipHeight height of chips and small buttons
 * @property avatar diameter of artist avatars in rows
 * @property minTouchTarget minimum size of anything tappable
 * @property navigationBarHeight height of the floating navigation bar
 * @property navigationBarBottomInset distance of the navigation bar from the bottom system inset
 */
@Immutable
data class AppDimens(
    val space1: Dp = 4.dp,
    val space2: Dp = 8.dp,
    val space3: Dp = 12.dp,
    val space4: Dp = 16.dp,
    val space5: Dp = 20.dp,
    val space6: Dp = 24.dp,
    val space8: Dp = 32.dp,
    val screenTitleInset: Dp = 22.dp,
    val controlHeight: Dp = 52.dp,
    val chipHeight: Dp = 32.dp,
    val avatar: Dp = 42.dp,
    val minTouchTarget: Dp = 48.dp,
    val navigationBarHeight: Dp = 66.dp,
    val navigationBarBottomInset: Dp = 12.dp,
)

/**
 * Corner shapes of the app. Read them through [AppThemeWrapper.shapes].
 *
 * @property card grouped lists, stat tiles and cards, 20 dp
 * @property control text fields, search fields and buttons, 16 dp
 * @property small icon tiles and avatar badges, 10 dp
 * @property sheet top corners of bottom sheets, 28 dp
 * @property pill fully rounded chips, capsules and the navigation bar
 */
@Immutable
data class AppShapes(
    val card: RoundedCornerShape = RoundedCornerShape(20.dp),
    val control: RoundedCornerShape = RoundedCornerShape(16.dp),
    val small: RoundedCornerShape = RoundedCornerShape(10.dp),
    val sheet: RoundedCornerShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    val pill: RoundedCornerShape = RoundedCornerShape(percent = 50),
)
