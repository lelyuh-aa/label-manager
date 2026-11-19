package com.leliukh.labelmanager.features.entry.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppTheme
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper

private val SplashIconSize = 240.dp

/**
 * What the app draws while the stored session is still being read: the same background and
 * logo as the system splash screen.
 *
 * Normally the system splash covers this moment. If the splash is dismissed early (some
 * launchers, starting with the screen off) or the read takes longer, the user still sees the
 * splash picture instead of an empty window, and the switch to the first screen stays seamless.
 */
@Composable
fun StartupPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppThemeWrapper.colors.background),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.size(SplashIconSize),
        )
    }
}

@Preview(name = "Startup", widthDp = 412, heightDp = 917)
@Composable
private fun StartupPlaceholderPreview() {
    AppTheme(darkTheme = false) {
        StartupPlaceholder()
    }
}
