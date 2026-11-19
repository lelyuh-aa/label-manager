package com.leliukh.labelmanager.features.activity.presentation.notification

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Asks for the notification permission once the signed-in home is shown, so sale alerts can
 * be posted. Android 13 and newer only; older versions grant it at install.
 */
@Composable
fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    LaunchedEffect(Unit) {
        val permission = Manifest.permission.POST_NOTIFICATIONS
        val state = ContextCompat.checkSelfPermission(context, permission)
        if (state != PackageManager.PERMISSION_GRANTED) launcher.launch(permission)
    }
}
