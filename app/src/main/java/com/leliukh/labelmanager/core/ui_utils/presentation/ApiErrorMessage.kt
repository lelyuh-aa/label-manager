package com.leliukh.labelmanager.core.ui_utils.presentation

import androidx.annotation.StringRes
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.network.domain.ApiError

/** A short message for a failed request that has no more specific explanation. */
@get:StringRes
val ApiError.messageRes: Int
    get() = when (this) {
        ApiError.Network -> R.string.common_error_network

        else -> R.string.common_error_generic
    }
