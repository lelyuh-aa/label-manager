package com.leliukh.labelmanager.backend.domain.services

import com.leliukh.labelmanager.backend.data.BackendStore
import com.leliukh.labelmanager.backend.domain.models.ActivityPage
import com.leliukh.labelmanager.backend.domain.models.ActivityView
import com.leliukh.labelmanager.backend.domain.models.Page
import com.leliukh.labelmanager.backend.domain.paging.keysetPage

/**
 * The activity feed of a user-owned label.
 *
 * @param store persistent state
 */
class ActivityService(private val store: BackendStore) {

    /**
     * @param labelId id of the caller's label
     * @param cursor cursor of the previous page; null for the first page
     * @param limit page size
     * @return events newest first, with the unread count
     */
    suspend fun page(labelId: String, cursor: String?, limit: Int): ActivityPage = store.read {
        val events = activityOfLabel(labelId)
        val page =
            events.keysetPage(
                cursor = cursor,
                limit = limit,
                key = { -it.occurredAt.toEpochMilliseconds() },
                id = { it.id },
            )
        ActivityPage(
            page =
                Page(
                    items =
                        page.items.map { ActivityView(it, it.counterpartyLabelId?.let(::findLabel)) },
                    nextCursor = page.nextCursor,
                    totalCount = page.totalCount,
                ),
            unreadCount = events.count { !it.isRead },
        )
    }

    /** @param labelId id of the caller's label whose events become read */
    suspend fun markAllRead(labelId: String) {
        store.write { markActivityRead(labelId) }
    }

    /**
     * Marks one event read; an unknown id or another label's event changes nothing.
     *
     * @param labelId id of the caller's label
     * @param eventId id of the event
     */
    suspend fun markRead(labelId: String, eventId: String) {
        store.write { markActivityRead(labelId, eventId) }
    }
}
