package com.leliukh.labelmanager.core.design_system.presentation.icons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp
import com.leliukh.labelmanager.core.design_system.presentation.preview.PreviewSurface
import com.leliukh.labelmanager.core.design_system.presentation.preview.ThemePreviews
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper

/**
 * Stroke icons of the app, drawn from the Lucide icon set (ISC license) on a 24 × 24 grid.
 *
 * Each icon is built once on first use. Tint them with [Icon]'s `tint`; the stroke color in
 * the vector itself is only a placeholder.
 */
object AppIcons {

    val Pencil: ImageVector by lazy {
        lucide(
            "Pencil",
            "M21.174 6.812a1 1 0 0 0-3.986-3.987L3.842 16.174a2 2 0 0 0-.5.83l-1.321 4.352" +
                "a.5.5 0 0 0 .623.622l4.353-1.32a2 2 0 0 0 .83-.497z",
            "m15 5 4 4",
        )
    }

    val Ellipsis: ImageVector by lazy {
        lucide("Ellipsis", circle(12f, 12f, 1f), circle(19f, 12f, 1f), circle(5f, 12f, 1f))
    }

    val Eye: ImageVector by lazy {
        lucide(
            "Eye",
            "M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696" +
                " 10.75 10.75 0 0 1-19.876 0",
            circle(12f, 12f, 3f),
        )
    }

    val EyeOff: ImageVector by lazy {
        lucide(
            "EyeOff",
            "M10.733 5.076a10.744 10.744 0 0 1 11.205 6.575 1 1 0 0 1 0 .696" +
                " 10.747 10.747 0 0 1-1.444 2.49",
            "M14.084 14.158a3 3 0 0 1-4.242-4.242",
            "M17.479 17.499a10.75 10.75 0 0 1-15.417-5.151 1 1 0 0 1 0-.696" +
                " 10.75 10.75 0 0 1 4.446-5.143",
            "m2 2 20 20",
        )
    }

    val Search: ImageVector by lazy {
        lucide("Search", circle(11f, 11f, 8f), "m21 21-4.3-4.3")
    }

    val Sliders: ImageVector by lazy {
        lucide(
            "Sliders",
            "M21 4h-7", "M10 4H3", "M21 12h-9", "M8 12H3", "M21 20h-5", "M12 20H3",
            "M14 2v4", "M8 10v4", "M16 18v4",
        )
    }

    val Music: ImageVector by lazy {
        lucide(
            "Music",
            "M9 18V5l12-2v13",
            "M6 15a3 3 0 1 0 0 6a3 3 0 1 0 0-6z",
            "M18 13a3 3 0 1 0 0 6a3 3 0 1 0 0-6z",
        )
    }

    val Wallet: ImageVector by lazy {
        lucide(
            "Wallet",
            "M19 7V4a1 1 0 0 0-1-1H5a2 2 0 0 0 0 4h15a1 1 0 0 1 1 1v4h-3a2 2 0 0 0 0 4h3" +
                "a1 1 0 0 0 1-1v-2a1 1 0 0 0-1-1",
            "M3 5v14a2 2 0 0 0 2 2h15a1 1 0 0 0 1-1v-4",
        )
    }

    val Users: ImageVector by lazy {
        lucide(
            "Users",
            "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2",
            circle(9f, 7f, 4f),
            "M22 21v-2a4 4 0 0 0-3-3.87",
            "M16 3.13a4 4 0 0 1 0 7.75",
        )
    }

    val Swap: ImageVector by lazy {
        lucide("Deal", "M8 3 4 7l4 4", "M4 7h16", "m16 21 4-4-4-4", "M20 17H4")
    }

    val Bell: ImageVector by lazy {
        lucide(
            "Bell",
            "M10.268 21a2 2 0 0 0 3.464 0",
            "M3.262 15.326A1 1 0 0 0 4 17h16a1 1 0 0 0 .74-1.673C19.41 13.956 18 12.499 18 8" +
                "A6 6 0 0 0 6 8c0 4.499-1.411 5.956-2.738 7.326",
        )
    }

    val Profile: ImageVector by lazy {
        lucide(
            "Profile",
            circle(12f, 12f, 10f),
            circle(12f, 10f, 3f),
            "M7 20.662V19a2 2 0 0 1 2-2h6a2 2 0 0 1 2 2v1.662",
        )
    }

    val Plus: ImageVector by lazy { lucide("Plus", "M5 12h14", "M12 5v14") }

    val Minus: ImageVector by lazy { lucide("Minus", "M5 12h14") }

    val Close: ImageVector by lazy { lucide("Close", "M18 6 6 18", "m6 6 12 12") }

    val Check: ImageVector by lazy { lucide("Check", "M20 6 9 17l-5-5") }

    val ChevronRight: ImageVector by lazy { lucide("ChevronRight", "m9 18 6-6-6-6") }

    val ChevronLeft: ImageVector by lazy { lucide("ChevronLeft", "m15 18-6-6 6-6") }

    val ChevronDown: ImageVector by lazy { lucide("ChevronDown", "m6 9 6 6 6-6") }

    val ArrowUp: ImageVector by lazy { lucide("ArrowUp", "m5 12 7-7 7 7", "M12 19V5") }

    val ArrowDown: ImageVector by lazy { lucide("ArrowDown", "M12 5v14", "m19 12-7 7-7-7") }

    val ArrowRight: ImageVector by lazy { lucide("ArrowRight", "M5 12h14", "m12 5 7 7-7 7") }

    val TrendingUp: ImageVector by lazy {
        lucide("TrendingUp", "M22 7L13.5 15.5L8.5 10.5L2 17", "M16 7h6v6")
    }

    val Tag: ImageVector by lazy {
        lucide(
            "Tag",
            "M12.586 2.586A2 2 0 0 0 11.172 2H4a2 2 0 0 0-2 2v7.172a2 2 0 0 0 .586 1.414" +
                "l8.704 8.704a2.426 2.426 0 0 0 3.42 0l6.58-6.58a2.426 2.426 0 0 0 0-3.42z",
            circle(7.5f, 7.5f, 0.5f),
        )
    }

    val LogOut: ImageVector by lazy {
        lucide(
            "LogOut",
            "m16 17 5-5-5-5",
            "M21 12H9",
            "M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4",
        )
    }

    val Globe: ImageVector by lazy {
        lucide(
            "Globe",
            circle(12f, 12f, 10f),
            "M12 2a14.5 14.5 0 0 0 0 20 14.5 14.5 0 0 0 0-20",
            "M2 12h20",
        )
    }

    val Moon: ImageVector by lazy { lucide("Moon", "M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z") }

    val Info: ImageVector by lazy {
        lucide("Info", circle(12f, 12f, 10f), "M12 16v-4", "M12 8h.01")
    }

    val WifiOff: ImageVector by lazy {
        lucide(
            "WifiOff",
            "M12 20h.01",
            "M8.5 16.429a5 5 0 0 1 7 0",
            "M5 12.859a10 10 0 0 1 5.17-2.69",
            "M19 12.859a10 10 0 0 0-2.007-1.523",
            "M2 8.82a15 15 0 0 1 4.177-2.643",
            "M22 8.82a15 15 0 0 0-11.288-3.764",
            "m2 2 20 20",
        )
    }

    val Refresh: ImageVector by lazy {
        lucide(
            "Refresh",
            "M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8",
            "M21 3v5h-5",
            "M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16",
            "M8 16H3v5",
        )
    }

    val Calendar: ImageVector by lazy {
        lucide(
            "Calendar",
            "M8 2v4",
            "M16 2v4",
            "M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z",
            "M3 10h18",
        )
    }

    val Lock: ImageVector by lazy {
        lucide(
            "Lock",
            "M5 11h14a2 2 0 0 1 2 2v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2z",
            "M7 11V7a5 5 0 0 1 10 0v4",
        )
    }

    val Mail: ImageVector by lazy {
        lucide(
            "Mail",
            "M4 4h16a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z",
            "m22 7-8.991 5.727a2 2 0 0 1-2.009 0L2 7",
        )
    }

    val Warning: ImageVector by lazy {
        lucide(
            "Warning",
            "m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3",
            "M12 9v4",
            "M12 17h.01",
        )
    }

    val Trophy: ImageVector by lazy {
        lucide(
            "Trophy",
            "M10 14.66v1.626a2 2 0 0 1-.976 1.696A5 5 0 0 0 7 21.978",
            "M14 14.66v1.626a2 2 0 0 0 .976 1.696A5 5 0 0 1 17 21.978",
            "M18 9h1.5a1 1 0 0 0 0-5H18",
            "M4 22h16",
            "M6 9a6 6 0 0 0 12 0V3a1 1 0 0 0-1-1H7a1 1 0 0 0-1 1z",
            "M6 9H4.5a1 1 0 0 1 0-5H6",
        )
    }

    val Clock: ImageVector by lazy {
        lucide("Clock", circle(12f, 12f, 10f), "M12 6v6l4 2")
    }

    val Coins: ImageVector by lazy {
        lucide(
            "Coins",
            circle(12f, 12f, 10f),
            "M16 8h-6a2 2 0 1 0 0 4h4a2 2 0 1 1 0 4H8",
            "M12 18V6",
        )
    }

    internal val all: List<ImageVector>
        get() = listOf(
            Pencil, Ellipsis, Eye, EyeOff, Search, Sliders, Music, Wallet, Users, Swap,
            Bell, Profile, Plus, Minus, Close, Check, ChevronRight, ChevronLeft, ChevronDown,
            ArrowUp, ArrowDown, ArrowRight, TrendingUp, Tag, LogOut, Globe, Moon, Info, WifiOff,
            Refresh, Calendar, Lock, Mail, Warning, Trophy, Clock, Coins,
        )
}

private fun lucide(name: String, vararg paths: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        paths.forEach { path ->
            addPath(
                pathData = addPathNodes(path),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()

private fun circle(cx: Float, cy: Float, r: Float): String =
    "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0"

@ThemePreviews
@Composable
private fun AppIconsPreview() {
    PreviewSurface {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AppIcons.all.forEach { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = icon.name,
                    tint = AppThemeWrapper.colors.textPrimary,
                )
            }
        }
    }
}
