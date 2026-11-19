package com.leliukh.labelmanager.backend.coroutines

import kotlin.coroutines.cancellation.CancellationException

/**
 * [runCatching] for suspending code: the same [Result] API, but a [CancellationException] is
 * rethrown instead of being captured, so a cancelled coroutine stops instead of continuing with a
 * failure result.
 *
 * @param block code to run
 * @return the block's value, or the exception it threw
 */
inline fun <T> runSuspendCatching(block: () -> T): Result<T> =
    runCatching(block).onFailure { if (it is CancellationException) throw it }
