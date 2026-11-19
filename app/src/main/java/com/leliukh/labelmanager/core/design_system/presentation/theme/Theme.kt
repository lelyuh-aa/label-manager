package com.leliukh.labelmanager.core.design_system.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalAppColors = staticCompositionLocalOf { LightAppColors }
private val LocalAppTypography = staticCompositionLocalOf { DefaultAppTypography }
private val LocalAppDimens = staticCompositionLocalOf { AppDimens() }
private val LocalAppShapes = staticCompositionLocalOf { AppShapes() }

/**
 * Applies the app's design system to [content].
 *
 * Material components inside get a [ColorScheme] mapped from the same tokens, so a stock
 * component never shows a color from outside the palette.
 *
 * @param darkTheme whether to use the dark palette
 * @param content the UI to theme
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkAppColors else LightAppColors

    CompositionLocalProvider(
        LocalAppColors provides colors,
        LocalAppTypography provides DefaultAppTypography,
        LocalAppDimens provides AppDimens(),
        LocalAppShapes provides AppShapes(),
    ) {
        MaterialTheme(
            colorScheme = colors.toColorScheme(),
            content = content,
        )
    }
}

/**
 * Entry point to the design-system values of the current [AppTheme], e.g.
 * `AppThemeWrapper.colors.textPrimary` or `AppThemeWrapper.typography.headline`.
 */
object AppThemeWrapper {

    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current

    val typography: AppTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalAppTypography.current

    val dimens: AppDimens
        @Composable
        @ReadOnlyComposable
        get() = LocalAppDimens.current

    val shapes: AppShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalAppShapes.current
}

private fun AppColors.toColorScheme(): ColorScheme {
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = accent,
        onPrimary = onAccent,
        primaryContainer = accentSoft,
        onPrimaryContainer = accentText,
        secondary = accentText,
        onSecondary = onAccent,
        background = background,
        onBackground = textPrimary,
        surface = surface,
        onSurface = textPrimary,
        surfaceVariant = fill,
        onSurfaceVariant = textSecondary,
        surfaceContainerLowest = surface,
        surfaceContainerLow = surface,
        surfaceContainer = surface,
        surfaceContainerHigh = surface,
        surfaceContainerHighest = fill,
        outline = separator,
        outlineVariant = separator,
        error = negative,
        onError = onAccent,
        errorContainer = negativeSoft,
        onErrorContainer = negative,
        scrim = scrim,
    )
}
