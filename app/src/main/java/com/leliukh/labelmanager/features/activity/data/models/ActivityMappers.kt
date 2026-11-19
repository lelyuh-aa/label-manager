package com.leliukh.labelmanager.features.activity.data.models

import com.leliukh.labelmanager.contract.dto.activity.ActivityEventDto
import com.leliukh.labelmanager.contract.dto.activity.ActivityTypeDto
import com.leliukh.labelmanager.features.activity.domain.models.ActivityEvent
import com.leliukh.labelmanager.features.activity.domain.models.ActivityType
import com.leliukh.labelmanager.features.artist.data.models.toDomain
import com.leliukh.labelmanager.features.label.data.models.toDomain

fun ActivityEventDto.toDomain(): ActivityEvent = ActivityEvent(
    id = id,
    type = type.toDomain(),
    occurredAt = occurredAt,
    artistId = artist.id,
    artistName = "${artist.firstName} ${artist.lastName}",
    genre = artist.genre.toDomain(),
    counterparty = counterparty?.toDomain(),
    amount = amount,
    valueBefore = valueBefore,
    valueAfter = valueAfter,
    isRead = isRead,
)

private fun ActivityTypeDto.toDomain(): ActivityType = when (this) {
    ActivityTypeDto.ARTIST_BOUGHT -> ActivityType.ArtistBought

    ActivityTypeDto.ARTIST_SOLD -> ActivityType.ArtistSold

    ActivityTypeDto.ARTIST_LISTED -> ActivityType.ArtistListed

    ActivityTypeDto.ARTIST_UNLISTED -> ActivityType.ArtistUnlisted
}
