package com.leliukh.labelmanager.features.artist.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Profile of any artist: own or another label's.
 *
 * @property artistId id of the artist
 */
@Serializable
data class ArtistRoute(val artistId: String) : NavKey

/**
 * Rename one of the user's artists or change their nationality.
 *
 * @property artistId id of the artist
 */
@Serializable
data class EditArtistRoute(val artistId: String) : NavKey
