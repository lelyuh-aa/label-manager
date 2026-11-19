package com.leliukh.labelmanager.core.design_system.presentation.components.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.preview.PreviewSurface
import com.leliukh.labelmanager.core.design_system.presentation.preview.ThemePreviews
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper

/**
 * Quiet explanation on a neutral fill, e.g. which fields of a form cannot be edited.
 *
 * @param text the explanation
 * @param modifier modifier of the note
 */
@Composable
fun InfoNote(text: String, modifier: Modifier = Modifier) {
    val colors = AppThemeWrapper.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.fill, AppThemeWrapper.shapes.control)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = AppIcons.Info,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(18.dp),
        )

        Text(
            text = text,
            style = AppThemeWrapper.typography.subhead,
            color = colors.textSecondary,
        )
    }
}

@ThemePreviews
@Composable
private fun InfoNotePreview() {
    PreviewSurface {
        InfoNote(text = "Age, genre and market value are set by the game and can’t be edited.")
    }
}
