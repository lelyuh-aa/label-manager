package com.leliukh.labelmanager.features.entry.presentation.view.activity

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppTheme
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper
import com.leliukh.labelmanager.core.settings.domain.models.ThemeMode
import com.leliukh.labelmanager.features.entry.presentation.screen.AppNavigation
import com.leliukh.labelmanager.features.entry.presentation.screen.StartupPlaceholder
import com.leliukh.labelmanager.features.entry.presentation.viewmodel.EntryViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * The single activity of the app. Every screen is a Navigation 3 destination hosted here.
 *
 * The system splash screen stays up until the stored session has been read, then the
 * navigation graph starts on the right screen. [StartupPlaceholder] repeats the splash picture
 * underneath, in case the system removes the splash before the session is known. The palette
 * follows the user's choice on the Profile tab, and the system bar icons follow the palette.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val entryViewModel: EntryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { entryViewModel.viewState.value.isLoading }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by entryViewModel.viewState.collectAsStateWithLifecycle()
            val isDark = when (state.themeMode) {
                ThemeMode.System -> isSystemInDarkTheme()

                ThemeMode.Light -> false

                ThemeMode.Dark -> true
            }

            DisposableEffect(isDark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        lightScrim = Color.TRANSPARENT,
                        darkScrim = Color.TRANSPARENT,
                        detectDarkMode = { isDark },
                    ),
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim = Color.TRANSPARENT,
                        darkScrim = Color.TRANSPARENT,
                        detectDarkMode = { isDark },
                    ),
                )
                onDispose {}
            }

            AppTheme(darkTheme = isDark) {
                if (state.isLoading) {
                    StartupPlaceholder()
                } else {
                    // In landscape the navigation bar and the cutout sit at a side; keep the
                    // screens clear of them. Top and bottom insets are handled by each screen.
                    Box(
                        modifier = Modifier
                            .background(AppThemeWrapper.colors.background)
                            .windowInsetsPadding(
                                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
                            ),
                    ) {
                        AppNavigation(isSignedIn = state.isSignedIn)
                    }
                }
            }
        }
    }
}
