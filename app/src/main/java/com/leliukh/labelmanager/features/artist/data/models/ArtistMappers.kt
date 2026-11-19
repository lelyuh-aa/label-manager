package com.leliukh.labelmanager.features.artist.data.models

import com.leliukh.labelmanager.contract.dto.artist.ArtistDetailsDto
import com.leliukh.labelmanager.contract.dto.label.LabelSummaryDto
import com.leliukh.labelmanager.contract.dto.deal.DealDto
import com.leliukh.labelmanager.features.artist.domain.models.ArtistDetails
import com.leliukh.labelmanager.features.artist.domain.models.LabelSummary
import com.leliukh.labelmanager.features.artist.domain.models.Deal
import com.leliukh.labelmanager.features.label.data.models.toDomain

/**
 * @param myLabelId id of the signed-in user's label, to tell own artists from others
 */
fun ArtistDetailsDto.toDomain(myLabelId: String?): ArtistDetails = ArtistDetails(
    artist = artist.toDomain(),
    label = label.toDomain(),
    deals = deals.map { it.toDomain() },
    isMine = label.id == myLabelId,
)

fun LabelSummaryDto.toDomain(): LabelSummary = LabelSummary(
    id = id,
    name = name,
    countryCode = countryCode,
)

fun DealDto.toDomain(): Deal = Deal(
    id = id,
    from = from.toDomain(),
    to = to.toDomain(),
    price = price,
    valueBefore = valueBefore,
    valueAfter = valueAfter,
    signedAt = signedAt,
)
