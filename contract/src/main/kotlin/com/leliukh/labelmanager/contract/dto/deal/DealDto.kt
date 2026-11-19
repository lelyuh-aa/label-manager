package com.leliukh.labelmanager.contract.dto.deal

import com.leliukh.labelmanager.contract.dto.label.LabelSummaryDto
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * A completed deal of an artist between two labels.
 *
 * After every deal the artist's market value grows by a random 10–100 %.
 *
 * @property id unique deal id
 * @property artistId id of the signed artist
 * @property artistName artist's full name at the moment of the deal
 * @property from the selling label
 * @property to the buying label
 * @property price price the buyer paid, in whole US dollars
 * @property valueBefore artist's market value before the deal
 * @property valueAfter artist's market value after the deal
 * @property signedAt moment of the deal
 */
@Serializable
public data class DealDto(
    val id: String,
    val artistId: String,
    val artistName: String,
    val from: LabelSummaryDto,
    val to: LabelSummaryDto,
    val price: Long,
    val valueBefore: Long,
    val valueAfter: Long,
    val signedAt: Instant,
)
