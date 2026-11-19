package com.leliukh.labelmanager.features.market.presentation.navigation

import androidx.navigation3.runtime.NavKey
import com.leliukh.labelmanager.features.label.domain.models.Genre
import kotlinx.serialization.Serializable

/**
 * The moment after a purchase: the artist joins the label and the new market value is revealed.
 * Everything it shows travels in the route, so it needs no loading.
 *
 * @property artistId id of the bought artist
 * @property fullName the artist's first and last name
 * @property initials letters for the avatar
 * @property genre playing genre
 * @property countryCode the artist's nationality
 * @property age age in full years
 * @property labelName name of the user's label
 * @property price price paid
 * @property valueBefore market value before the deal
 * @property valueAfter market value after the deal
 * @property budget the user's budget after paying
 */
@Serializable
data class DealDoneRoute(
    val artistId: String,
    val fullName: String,
    val initials: String,
    val genre: Genre,
    val countryCode: String,
    val age: Int,
    val labelName: String,
    val price: Long,
    val valueBefore: Long,
    val valueAfter: Long,
    val budget: Long,
) : NavKey

/** Pick one of the user's artists to put on the market. */
@Serializable
data object SelectArtistRoute : NavKey

/**
 * Set the asking price and list one of the user's artists.
 *
 * @property artistId id of the artist
 */
@Serializable
data class CreateListingRoute(val artistId: String) : NavKey
