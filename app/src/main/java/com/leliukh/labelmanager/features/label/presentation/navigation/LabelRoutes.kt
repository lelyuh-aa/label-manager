package com.leliukh.labelmanager.features.label.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** "Your label is ready" moment shown once after the account is created. */
@Serializable
data object LabelReadyRoute : NavKey

/** Rename the label or change its country. */
@Serializable
data object EditLabelRoute : NavKey
