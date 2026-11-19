package com.leliukh.labelmanager.core.design_system.presentation.components.field

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.design_system.presentation.components.button.CircleIconButton
import com.leliukh.labelmanager.core.design_system.presentation.components.button.CircleIconButtonStyle
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.preview.PreviewSurface
import com.leliukh.labelmanager.core.design_system.presentation.preview.ThemePreviews
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper

/**
 * Search field with a magnifier and a clear button that appears once something is typed.
 *
 * @param query current query
 * @param onQueryChange called with the edited query; the clear button sends an empty string
 * @param placeholder text shown while the query is empty
 * @param modifier modifier of the field
 */
@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeWrapper.colors
    val focusManager = LocalFocusManager.current

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = AppThemeWrapper.typography.body.copy(color = colors.textPrimary),
        cursorBrush = SolidColor(colors.accent),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppThemeWrapper.dimens.controlHeight)
                    .background(colors.fill, AppThemeWrapper.shapes.control)
                    .padding(start = 16.dp, end = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = AppIcons.Search,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(19.dp),
                )

                FieldText(
                    isEmpty = query.isEmpty(),
                    placeholder = placeholder,
                    innerTextField = innerTextField,
                    modifier = Modifier.weight(1f),
                )

                AnimatedVisibility(
                    visible = query.isNotEmpty(),
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                ) {
                    CircleIconButton(
                        icon = AppIcons.Close,
                        contentDescription = stringResource(R.string.common_clear_search),
                        onClick = { onQueryChange("") },
                        style = CircleIconButtonStyle.Plain,
                    )
                }
            }
        },
    )
}

@ThemePreviews
@Composable
private fun SearchFieldPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SearchField(query = "", onQueryChange = {}, placeholder = "Search artists")

            SearchField(query = "Marchand", onQueryChange = {}, placeholder = "Search artists")
        }
    }
}
