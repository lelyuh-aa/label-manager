package com.leliukh.labelmanager.core.design_system.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.leliukh.labelmanager.R

/**
 * Text styles of the app, all set in Geist. Read them through [AppThemeWrapper.typography].
 *
 * Money styles use tabular figures so amounts line up and do not jitter while they animate.
 *
 * @property largeTitle screen titles, 30 sp bold
 * @property title sheet and hero titles, 22 sp semibold
 * @property headline section headers, 18 sp semibold
 * @property body default text, 16 sp regular
 * @property bodyMedium list row titles, 16 sp medium
 * @property bodySemibold emphasised body text and button labels, 16 sp semibold
 * @property callout secondary controls and chips, 14 sp medium
 * @property subhead secondary lines of rows, 13 sp regular
 * @property subheadMedium labels above values, 13 sp medium
 * @property caption badges and small labels, 12 sp semibold
 * @property micro navigation bar labels, 11 sp semibold
 * @property badge genre badges on avatars, 9 sp bold
 * @property moneyHero the big number of a screen, 38 sp bold
 * @property moneyLarge stat tiles, 22 sp semibold
 * @property money amounts in rows, 16 sp semibold
 * @property moneySmall deltas and compact amounts, 12 sp semibold
 */
@Immutable
data class AppTypography(
    val largeTitle: TextStyle,
    val title: TextStyle,
    val headline: TextStyle,
    val body: TextStyle,
    val bodyMedium: TextStyle,
    val bodySemibold: TextStyle,
    val callout: TextStyle,
    val subhead: TextStyle,
    val subheadMedium: TextStyle,
    val caption: TextStyle,
    val micro: TextStyle,
    val badge: TextStyle,
    val moneyHero: TextStyle,
    val moneyLarge: TextStyle,
    val money: TextStyle,
    val moneySmall: TextStyle,
)

private val Geist = FontFamily(
    Font(R.font.geist_regular, FontWeight.Normal),
    Font(R.font.geist_medium, FontWeight.Medium),
    Font(R.font.geist_semibold, FontWeight.SemiBold),
    Font(R.font.geist_bold, FontWeight.Bold),
)

private const val TABULAR_FIGURES = "tnum"

private val BaseStyle = TextStyle(
    fontFamily = Geist,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    ),
)

internal val DefaultAppTypography = AppTypography(
    largeTitle = BaseStyle.copy(
        fontSize = 30.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.03).em,
    ),
    title = BaseStyle.copy(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.02).em,
    ),
    headline = BaseStyle.copy(
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.02).em,
    ),
    body = BaseStyle.copy(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal),
    bodyMedium = BaseStyle.copy(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = (-0.01).em,
    ),
    bodySemibold = BaseStyle.copy(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.01).em,
    ),
    callout = BaseStyle.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    subhead = BaseStyle.copy(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal),
    subheadMedium = BaseStyle.copy(
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Medium,
    ),
    caption = BaseStyle.copy(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    micro = BaseStyle.copy(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold),
    badge = BaseStyle.copy(fontSize = 9.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold),
    moneyHero = BaseStyle.copy(
        fontSize = 38.sp,
        lineHeight = 42.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.035).em,
        fontFeatureSettings = TABULAR_FIGURES,
    ),
    moneyLarge = BaseStyle.copy(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.03).em,
        fontFeatureSettings = TABULAR_FIGURES,
    ),
    money = BaseStyle.copy(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.02).em,
        fontFeatureSettings = TABULAR_FIGURES,
    ),
    moneySmall = BaseStyle.copy(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.SemiBold,
        fontFeatureSettings = TABULAR_FIGURES,
    ),
)
