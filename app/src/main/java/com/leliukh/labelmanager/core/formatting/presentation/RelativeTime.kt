package com.leliukh.labelmanager.core.formatting.presentation

import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.leliukh.labelmanager.R
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * How long ago a moment was, in the device language, e.g. "12 min. ago"; "Just now" within
 * the last minute.
 *
 * @param instant the moment
 * @param now the current moment
 */
@Composable
fun relativeTime(instant: Instant, now: Instant): String =
    if (now - instant < 1.minutes) {
        stringResource(R.string.common_just_now)
    } else {
        DateUtils.getRelativeTimeSpanString(
            instant.toEpochMilliseconds(),
            now.toEpochMilliseconds(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE,
        ).toString()
    }
