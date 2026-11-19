package com.leliukh.labelmanager.core.design_system.presentation.components.chip

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.leliukh.labelmanager.core.design_system.presentation.preview.PreviewSurface
import com.leliukh.labelmanager.core.design_system.presentation.preview.ThemePreviews
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper

private val ThumbShape = RoundedCornerShape(9.dp)

/**
 * Segmented control with a thumb that springs to the selected option.
 *
 * @param options labels of the segments
 * @param selectedIndex index of the selected segment
 * @param onSelect called with the index of the tapped segment
 * @param modifier modifier of the control
 */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeWrapper.colors

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .background(colors.fill, RoundedCornerShape(12.dp))
            .padding(3.dp),
    ) {
        val segmentWidth = maxWidth / options.size.coerceAtLeast(1)
        val thumbOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex,
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
            label = "segmentThumb",
        )

        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .shadow(if (colors.isDark) 0.dp else 1.dp, ThumbShape)
                .background(colors.fillSelected, ThumbShape),
        )

        Row(modifier = Modifier.fillMaxWidth().selectableGroup()) {
            options.forEachIndexed { index, option ->
                val isSelected = index == selectedIndex

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .selectable(
                            selected = isSelected,
                            role = Role.Tab,
                            onClick = { onSelect(index) },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = option,
                        style = if (isSelected) {
                            AppThemeWrapper.typography.callout.copy(fontWeight = FontWeight.SemiBold)
                        } else {
                            AppThemeWrapper.typography.callout
                        },
                        color = if (isSelected) colors.textPrimary else colors.textSecondary,
                    )
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun SegmentedControlPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SegmentedControl(options = listOf("Artists", "Labels"), selectedIndex = 0, onSelect = {})

            SegmentedControl(
                options = listOf("Light", "Dark", "System"),
                selectedIndex = 2,
                onSelect = {},
            )
        }
    }
}
