package com.leliukh.labelmanager.core.design_system.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Color tokens of the app. Read them through [AppThemeWrapper.colors], never hardcode a color
 * in a screen.
 *
 * The palette is derived from one brand accent, `#5B5BD6`: backgrounds carry a faint tint of
 * it, selected states use the pastel [accentSoft] with [accentText] on top. Green and red are
 * reserved for money going up or down.
 *
 * @property isDark whether this is the dark palette
 * @property background screen background
 * @property surface cards, grouped lists, sheets
 * @property fill search fields, segmented controls, neutral chips
 * @property fillSelected the selected segment of a segmented control
 * @property separator hairlines between list rows
 * @property textPrimary main text
 * @property textSecondary secondary text and placeholders
 * @property textTertiary chevrons and disabled content
 * @property accent primary buttons, active indicators, badges
 * @property onAccent content on [accent]
 * @property accentSoft pastel fill of selected and active elements
 * @property accentText accent-colored text and icons on light fills
 * @property positive a value went up
 * @property positiveSoft background of positive badges
 * @property negative a value went down, destructive actions, errors
 * @property negativeSoft background of error banners
 * @property warning cautions such as an insufficient budget
 * @property warningSoft background of warnings
 * @property info neutral informational accents
 * @property infoSoft background of informational accents
 * @property navigationBarSolid fill of the floating navigation bar
 * @property navigationBarBorder outline of the floating navigation bar
 * @property navigationInactive labels and icons of inactive tabs
 * @property shadow tint of elevation shadows
 * @property scrim dim layer behind bottom sheets and dialogs
 * @property genres colors of the four playing genres
 */
@Immutable
data class AppColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val fill: Color,
    val fillSelected: Color,
    val separator: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val onAccent: Color,
    val accentSoft: Color,
    val accentText: Color,
    val positive: Color,
    val positiveSoft: Color,
    val negative: Color,
    val negativeSoft: Color,
    val warning: Color,
    val warningSoft: Color,
    val info: Color,
    val infoSoft: Color,
    val navigationBarSolid: Color,
    val navigationBarBorder: Color,
    val navigationInactive: Color,
    val shadow: Color,
    val scrim: Color,
    val genres: GenreColors,
)

/**
 * Foreground and background colors of each playing genre, used by avatars and chips.
 *
 * @property pop pop colors
 * @property rock rock colors
 * @property hipHop hip-hop colors
 * @property electronic electronic colors
 */
@Immutable
data class GenreColors(
    val pop: ColorPair,
    val rock: ColorPair,
    val hipHop: ColorPair,
    val electronic: ColorPair,
)

/**
 * A content color and the background it sits on.
 *
 * @property content text and icon color
 * @property container background color
 */
@Immutable
data class ColorPair(
    val content: Color,
    val container: Color,
)

internal val LightAppColors = AppColors(
    isDark = false,
    background = Color(0xFFF7F7FD),
    surface = Color(0xFFFFFFFF),
    fill = Color(0xFFECECF4),
    fillSelected = Color(0xFFFFFFFF),
    separator = Color(0xFFE4E4F0),
    textPrimary = Color(0xFF14121C),
    textSecondary = Color(0xFF686577),
    textTertiary = Color(0xFFC7C3D3),
    accent = Color(0xFF5B5BD6),
    onAccent = Color(0xFFFFFFFF),
    accentSoft = Color(0xFFEAEAFA),
    accentText = Color(0xFF4E4EB8),
    positive = Color(0xFF0E8A5F),
    positiveSoft = Color(0xFFDDF4EA),
    negative = Color(0xFFC8364C),
    negativeSoft = Color(0xFFFDE6EA),
    warning = Color(0xFFA85A00),
    warningSoft = Color(0xFFFFF0DC),
    info = Color(0xFF1F5FBF),
    infoSoft = Color(0xFFE3EDFF),
    navigationBarSolid = Color(0xFFFFFFFF),
    navigationBarBorder = Color(0x145B5BD6),
    navigationInactive = Color(0xFF56536A),
    shadow = Color(0xFF5B5BD6),
    scrim = Color(0x66100F1C),
    genres = GenreColors(
        pop = ColorPair(content = Color(0xFF9A5B00), container = Color(0xFFFFF0D9)),
        rock = ColorPair(content = Color(0xFF1F5FBF), container = Color(0xFFE3EDFF)),
        hipHop = ColorPair(content = Color(0xFF0B7A66), container = Color(0xFFDBF4EE)),
        electronic = ColorPair(content = Color(0xFFC02E4C), container = Color(0xFFFFE3E9)),
    ),
)

internal val DarkAppColors = AppColors(
    isDark = true,
    background = Color(0xFF11101E),
    surface = Color(0xFF1E1D30),
    fill = Color(0xFF242439),
    fillSelected = Color(0xFF393853),
    separator = Color(0xFF302F47),
    textPrimary = Color(0xFFF4F2FA),
    textSecondary = Color(0xFFA9A5B8),
    textTertiary = Color(0xFF55515F),
    accent = Color(0xFF9696E5),
    onAccent = Color(0xFF1B1B40),
    accentSoft = Color(0x2E9696E5),
    accentText = Color(0xFFB5B5ED),
    positive = Color(0xFF4FD1A1),
    positiveSoft = Color(0x264FD1A1),
    negative = Color(0xFFFF7A8A),
    negativeSoft = Color(0x26FF7A8A),
    warning = Color(0xFFFFB45C),
    warningSoft = Color(0x26FFB45C),
    info = Color(0xFF8AB8FF),
    infoSoft = Color(0x298AB8FF),
    navigationBarSolid = Color(0xFF27253E),
    navigationBarBorder = Color(0x12FFFFFF),
    navigationInactive = Color(0xFFB9B5C8),
    shadow = Color(0xFF000000),
    scrim = Color(0x99000000),
    genres = GenreColors(
        pop = ColorPair(content = Color(0xFFFFC864), container = Color(0x26FFC864)),
        rock = ColorPair(content = Color(0xFF8AB8FF), container = Color(0x268AB8FF)),
        hipHop = ColorPair(content = Color(0xFF5FDBC3), container = Color(0x265FDBC3)),
        electronic = ColorPair(content = Color(0xFFFF8FA6), container = Color(0x26FF8FA6)),
    ),
)
