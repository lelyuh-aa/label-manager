package com.leliukh.labelmanager.backend.testing

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * A clock tests move by hand.
 *
 * @param now the current moment
 */
class TestClock(var now: Instant = Instant.parse("2026-10-25T12:00:00Z")) : Clock {

    override fun now(): Instant = now

    /** @param duration how far to move the clock forward */
    fun advanceBy(duration: Duration) {
        now += duration
    }
}
