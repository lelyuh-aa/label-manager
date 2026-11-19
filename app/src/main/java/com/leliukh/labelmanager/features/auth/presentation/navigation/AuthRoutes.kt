package com.leliukh.labelmanager.features.auth.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** First screen of a signed-out user: sign in or create an account. */
@Serializable
data object WelcomeRoute : NavKey

/** Sign-in form. */
@Serializable
data object SignInRoute : NavKey

/** Account creation form. */
@Serializable
data object SignUpRoute : NavKey
