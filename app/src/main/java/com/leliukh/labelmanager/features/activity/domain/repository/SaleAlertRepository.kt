package com.leliukh.labelmanager.features.activity.domain.repository

import kotlin.time.Instant

/**
 * Remembers up to which moment sales were already announced, so a sale is notified once.
 */
interface SaleAlertRepository {

    /** @return the moment of the last announced sale; null before the first one */
    suspend fun lastAlertedAt(): Instant?

    /** @param instant the moment of the newest announced sale */
    suspend fun setLastAlertedAt(instant: Instant)
}
