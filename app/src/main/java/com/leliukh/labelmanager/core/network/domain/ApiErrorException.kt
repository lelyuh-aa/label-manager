package com.leliukh.labelmanager.core.network.domain

/**
 * An [ApiError] where an API wants a [Throwable], e.g. a failed page of a Paging source. The
 * screen reads [error] back to explain the failure.
 *
 * @property error why the request failed
 */
class ApiErrorException(val error: ApiError) : Exception(error.toString())
