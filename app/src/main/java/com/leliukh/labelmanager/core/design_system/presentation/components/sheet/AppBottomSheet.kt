package com.leliukh.labelmanager.core.design_system.presentation.components.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper

/**
 * Modal bottom sheet in the app's style: surface color, 28 dp top corners, a slim grabber
 * and an optional title. A tall sheet stops below the status bar.
 *
 * @param onDismiss called when the sheet is swiped away or the scrim is tapped
 * @param modifier modifier of the sheet
 * @param title optional title under the grabber
 * @param sheetState state of the sheet; skips the half-expanded state by default
 * @param content the sheet content
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = AppThemeWrapper.colors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier.statusBarsPadding(),
        sheetState = sheetState,
        shape = AppThemeWrapper.shapes.sheet,
        containerColor = colors.surface,
        contentColor = colors.textPrimary,
        scrimColor = colors.scrim,
        dragHandle = { Grabber() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp),
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = AppThemeWrapper.typography.title,
                    color = colors.textPrimary,
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 16.dp)
                        .semantics { heading() },
                )
            }

            content()
        }
    }
}

@Composable
private fun Grabber() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 5.dp)
                .background(AppThemeWrapper.colors.separator, RoundedCornerShape(percent = 50)),
        )
    }
}
