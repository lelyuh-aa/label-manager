package com.leliukh.labelmanager.core.design_system.presentation.preview

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppTheme
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper

/**
 * Renders a preview twice, in the light and in the dark palette.
 */
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class ThemePreviews

/**
 * Wraps a component preview in [AppTheme] on the screen background with a small margin.
 *
 * @param content the component to preview
 */
@Composable
fun PreviewSurface(content: @Composable () -> Unit) {
    AppTheme {
        Box(
            modifier = Modifier
                .background(AppThemeWrapper.colors.background)
                .padding(16.dp),
        ) {
            content()
        }
    }
}
