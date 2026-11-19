package com.leliukh.labelmanager.core.design_system.presentation.components.field

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leliukh.labelmanager.core.design_system.presentation.icons.AppIcons
import com.leliukh.labelmanager.core.design_system.presentation.preview.PreviewSurface
import com.leliukh.labelmanager.core.design_system.presentation.preview.ThemePreviews
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper

/**
 * A form field that opens a picker instead of taking text, e.g. the country. Looks like an
 * [AppTextField] with a chevron at the end.
 *
 * @param label label above the field
 * @param value the chosen value
 * @param onClick opens the picker
 * @param modifier modifier of the field
 * @param emoji optional emoji before the value, e.g. a flag
 * @param enabled whether the field reacts to taps
 */
@Composable
fun SelectField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    enabled: Boolean = true,
) {
    val colors = AppThemeWrapper.colors

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = AppThemeWrapper.typography.subheadMedium,
            color = colors.textSecondary,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppThemeWrapper.dimens.controlHeight)
                .clip(AppThemeWrapper.shapes.control)
                .background(colors.fill)
                .clickable(enabled = enabled, role = Role.Button, onClickLabel = label) {
                    onClick()
                }
                .padding(start = 16.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (emoji != null) {
                Text(text = emoji, fontSize = 20.sp)
            }

            Text(
                text = value,
                style = AppThemeWrapper.typography.body,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            Icon(
                imageVector = AppIcons.ChevronRight,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun SelectFieldPreview() {
    PreviewSurface {
        SelectField(label = "Country", value = "Germany", emoji = "🇩🇪", onClick = {})
    }
}
