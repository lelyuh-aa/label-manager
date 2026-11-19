package com.leliukh.labelmanager.contract.dto.common

import kotlinx.serialization.Serializable

/**
 * One page of a cursor-paginated list.
 *
 * @property items the items of this page
 * @property nextCursor value for the `cursor` query parameter of the next page; null on the
 * last page
 * @property totalCount number of items matching the request across all pages
 */
@Serializable
public data class PageDto<T>(
    val items: List<T>,
    val nextCursor: String? = null,
    val totalCount: Int,
)
