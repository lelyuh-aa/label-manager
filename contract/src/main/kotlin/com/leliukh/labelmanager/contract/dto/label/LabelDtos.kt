package com.leliukh.labelmanager.contract.dto.label

import com.leliukh.labelmanager.contract.dto.artist.ArtistDto
import kotlinx.serialization.Serializable

/**
 * Response of a `GET` or `PATCH` to [com.leliukh.labelmanager.contract.routes.ApiRoutes.LABEL]:
 * the signed-in user's own label.
 *
 * @property id unique label id
 * @property name label name, editable
 * @property countryCode ISO 3166-1 alpha-2 country, editable
 * @property budget money available for deals, in whole US dollars
 * @property value sum of the market values of all [artists]
 * @property artists the roster
 */
@Serializable
public data class LabelDto(
    val id: String,
    val name: String,
    val countryCode: String,
    val budget: Long,
    val value: Long,
    val artists: List<ArtistDto>,
)

/**
 * Public short form of a label, used wherever another label is referenced.
 *
 * @property id unique label id
 * @property name label name
 * @property countryCode ISO 3166-1 alpha-2 country
 */
@Serializable
public data class LabelSummaryDto(
    val id: String,
    val name: String,
    val countryCode: String,
)

/**
 * Body of a `PATCH` to [com.leliukh.labelmanager.contract.routes.ApiRoutes.LABEL]; absent
 * fields stay as they are.
 *
 * @property name new label name
 * @property countryCode new ISO 3166-1 alpha-2 country
 */
@Serializable
public data class UpdateLabelRequestDto(
    val name: String? = null,
    val countryCode: String? = null,
)
