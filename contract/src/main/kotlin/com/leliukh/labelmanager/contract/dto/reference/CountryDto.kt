package com.leliukh.labelmanager.contract.dto.reference

import kotlinx.serialization.Serializable

/**
 * A country a label or an artist can belong to, as returned by
 * [com.leliukh.labelmanager.contract.routes.ApiRoutes.COUNTRIES].
 *
 * @property code ISO 3166-1 alpha-2 code
 * @property name English name
 */
@Serializable
public data class CountryDto(
    val code: String,
    val name: String,
)
