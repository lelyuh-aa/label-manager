package com.leliukh.labelmanager.features.auth.presentation.screen.welcome

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper

private val HeroCorner = 48.dp
private val SideInset = 20.dp
private val TopGap = 18.dp
private val BottomGap = 24.dp
private val SpindleRadius = 2.5.dp
private val LineWidth = 1.5.dp

private const val LABEL_OF_RECORD = 0.44f
private const val GROOVES = 7
private const val SHINE_START = 200f
private const val SHINE_SWEEP = 50f

/**
 * Where the record sits inside the hero, in dp, so the screen can put the logo on its centre
 * label.
 *
 * The record fills the space from just below the status bar to just above the hero's rounded
 * bottom edge, and the label is a share of the record, so the picture keeps its proportions on
 * any screen size and nothing reaches the text below the hero.
 *
 * @property top y of the record's top edge
 * @property bottom y of the record's bottom edge
 * @property labelRadius radius of the centre label
 */
internal data class VinylGeometry(
    val top: Dp,
    val bottom: Dp,
    val labelRadius: Dp,
) {

    /** y of the record's centre. */
    val center: Dp
        get() = (top + bottom) / 2

    companion object {

        /**
         * @param width width of the hero
         * @param heroHeight height of the hero
         * @param statusBarHeight height of the status bar drawn over the hero
         */
        fun of(width: Dp, heroHeight: Dp, statusBarHeight: Dp): VinylGeometry {
            val top = statusBarHeight + TopGap
            val bottom = heroHeight - BottomGap
            val recordRadius = min((bottom - top) / 2, width / 2 - SideInset)
            return VinylGeometry(
                top = top,
                bottom = bottom,
                labelRadius = recordRadius * LABEL_OF_RECORD,
            )
        }
    }
}

/**
 * The pastel hero behind the Welcome content: a vinyl record with its grooves, centre label and
 * a soft shine, drawn in hairlines of the accent color.
 *
 * @param height height of the hero
 * @param geometry where the record goes
 */
@Composable
internal fun VinylBackground(height: Dp, geometry: VinylGeometry) {
    val colors = AppThemeWrapper.colors
    val lineColor = colors.accent.copy(alpha = if (colors.isDark) 0.24f else 0.2f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(
                colors.accentSoft,
                RoundedCornerShape(bottomStart = HeroCorner, bottomEnd = HeroCorner),
            ),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRecord(geometry = geometry, color = lineColor)
        }
    }
}

private fun DrawScope.drawRecord(geometry: VinylGeometry, color: Color) {
    val stroke = Stroke(width = LineWidth.toPx())
    val center = Offset(size.width / 2, geometry.center.toPx())
    val recordRadius = minOf(
        (geometry.bottom - geometry.top).toPx() / 2,
        size.width / 2 - SideInset.toPx(),
    )
    val labelRadius = geometry.labelRadius.toPx()

    drawCircle(color = color, radius = recordRadius, center = center, style = stroke)
    val step = (recordRadius - labelRadius) / (GROOVES + 1)
    repeat(GROOVES) { index ->
        drawCircle(
            color = color.copy(alpha = color.alpha * if (index % 2 == 0) 1f else 0.6f),
            radius = labelRadius + step * (index + 1),
            center = center,
            style = stroke,
        )
    }
    drawCircle(color = color, radius = labelRadius, center = center, style = stroke)
    drawCircle(color = color, radius = SpindleRadius.toPx(), center = center)

    // A highlight across the outer grooves, as light catches a real record.
    val shineRadius = recordRadius - step
    drawArc(
        color = color.copy(alpha = minOf(1f, color.alpha * 2)),
        startAngle = SHINE_START,
        sweepAngle = SHINE_SWEEP,
        useCenter = false,
        topLeft = Offset(center.x - shineRadius, center.y - shineRadius),
        size = Size(shineRadius * 2, shineRadius * 2),
        style = Stroke(width = stroke.width * 2, cap = StrokeCap.Round),
    )
}
