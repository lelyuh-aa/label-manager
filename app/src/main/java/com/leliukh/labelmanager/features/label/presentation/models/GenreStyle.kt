package com.leliukh.labelmanager.features.label.presentation.models

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.design_system.presentation.theme.AppThemeWrapper
import com.leliukh.labelmanager.core.design_system.presentation.theme.ColorPair
import com.leliukh.labelmanager.features.label.domain.models.Genre

/** Short code of the genre shown on avatars, e.g. "POP". */
@get:StringRes
val Genre.shortLabelRes: Int
    get() = when (this) {
        Genre.Pop -> R.string.genre_short_pop

        Genre.Rock -> R.string.genre_short_rock

        Genre.HipHop -> R.string.genre_short_hip_hop

        Genre.Electronic -> R.string.genre_short_electronic
    }

/** Name of the genre, e.g. "Pop". */
@get:StringRes
val Genre.nameRes: Int
    get() = when (this) {
        Genre.Pop -> R.string.genre_name_pop

        Genre.Rock -> R.string.genre_name_rock

        Genre.HipHop -> R.string.genre_name_hip_hop

        Genre.Electronic -> R.string.genre_name_electronic
    }

/** Plural title of the genre for list sections, e.g. "Pop". */
@get:StringRes
val Genre.titleRes: Int
    get() = when (this) {
        Genre.Pop -> R.string.genre_title_pop

        Genre.Rock -> R.string.genre_title_rock

        Genre.HipHop -> R.string.genre_title_hip_hop

        Genre.Electronic -> R.string.genre_title_electronic
    }

/** Colors of the genre from the current theme. */
val Genre.colors: ColorPair
    @Composable
    @ReadOnlyComposable
    get() {
        val genres = AppThemeWrapper.colors.genres
        return when (this) {
            Genre.Pop -> genres.pop

            Genre.Rock -> genres.rock

            Genre.HipHop -> genres.hipHop

            Genre.Electronic -> genres.electronic
        }
    }
