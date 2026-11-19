package com.leliukh.labelmanager.backend.domain.paging

import com.leliukh.labelmanager.backend.domain.errors.DomainException
import com.leliukh.labelmanager.backend.domain.models.Page
import kotlin.io.encoding.Base64

/**
 * Pages a list by a sort key and the item id instead of an offset.
 *
 * The cursor names the last item of the previous page, so items added or removed between two
 * requests never cause a repeated or skipped item. Items are ordered by ascending [key], then by
 * ascending [id]; a descending order uses a negated key.
 *
 * @param cursor cursor from the previous page; null for the first page
 * @param limit maximum page size
 * @param key sort key of an item
 * @param id unique id of an item
 * @return the page
 * @throws DomainException.ValidationFailed for a cursor this function did not create
 */
fun <T> List<T>.keysetPage(
    cursor: String?,
    limit: Int,
    key: (T) -> Long,
    id: (T) -> String,
): Page<T> {
    val after = cursor?.let(::decodeCursor)
    val sorted = sortedWith(compareBy<T>(key).thenBy(id))
    val remaining =
        if (after == null) {
            sorted
        } else {
            sorted.filter {
                key(it) > after.first || (key(it) == after.first && id(it) > after.second)
            }
        }
    val items = remaining.take(limit)
    val nextCursor =
        if (remaining.size > limit) {
            items.last().let { encodeCursor(key(it), id(it)) }
        } else {
            null
        }
    return Page(items, nextCursor, size)
}

private val cursorBase64 = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)

private fun encodeCursor(key: Long, id: String): String =
    cursorBase64.encode("$key:$id".encodeToByteArray())

private fun decodeCursor(cursor: String): Pair<Long, String> {
    val decoded = runCatching { cursorBase64.decode(cursor).decodeToString() }.getOrNull()
    val key = decoded?.substringBefore(":", "")?.toLongOrNull()
    val id = decoded?.substringAfter(":", "")
    if (key == null || id.isNullOrEmpty()) {
        throw DomainException.ValidationFailed(mapOf("cursor" to "is not a valid cursor"))
    }
    return key to id
}
