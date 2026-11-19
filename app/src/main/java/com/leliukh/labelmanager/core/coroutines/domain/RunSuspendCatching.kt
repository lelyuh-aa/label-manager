package com.leliukh.labelmanager.core.coroutines.domain

import kotlin.coroutines.cancellation.CancellationException

/**
 * [runCatching] for suspend code: the same [Result] API, but a [CancellationException] is
 * rethrown, so a cancelled coroutine stops instead of continuing with a "failure" result
 * after its scope is gone.
 *
 * @param block the code to run
 */
inline fun <T> runSuspendCatching(block: () -> T): Result<T> =
    runCatching(block).onFailure { if (it is CancellationException) throw it }
