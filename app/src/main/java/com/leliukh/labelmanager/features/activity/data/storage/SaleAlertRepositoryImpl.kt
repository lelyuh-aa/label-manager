package com.leliukh.labelmanager.features.activity.data.storage

import android.content.SharedPreferences
import androidx.core.content.edit
import com.leliukh.labelmanager.features.activity.domain.repository.SaleAlertRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Instant

private const val KEY_LAST_ALERTED_AT = "lastAlertedAtMillis"

/**
 * [SaleAlertRepository] in a small preferences file. It holds one timestamp and nothing
 * personal, so it needs no encryption.
 *
 * @param preferences the preferences file
 */
class SaleAlertRepositoryImpl(
    private val preferences: SharedPreferences,
) : SaleAlertRepository {

    override suspend fun lastAlertedAt(): Instant? = withContext(Dispatchers.IO) {
        preferences.getLong(KEY_LAST_ALERTED_AT, -1L)
            .takeIf { it >= 0 }
            ?.let(Instant::fromEpochMilliseconds)
    }

    override suspend fun setLastAlertedAt(instant: Instant) {
        withContext(Dispatchers.IO) {
            preferences.edit { putLong(KEY_LAST_ALERTED_AT, instant.toEpochMilliseconds()) }
        }
    }
}
