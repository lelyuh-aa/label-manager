package com.leliukh.labelmanager.features.activity.presentation.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.leliukh.labelmanager.R
import com.leliukh.labelmanager.core.formatting.presentation.formatMoney
import com.leliukh.labelmanager.features.activity.domain.models.ActivityEvent
import com.leliukh.labelmanager.features.entry.presentation.view.activity.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val CHANNEL_ID = "sales"

/**
 * Posts a system notification when a rival label buys one of the user's artists. Tapping it
 * opens the app. Nothing is posted while notifications are off or not permitted.
 *
 * @param context the application context
 * @param notificationManager posts the notifications and registers the channel
 */
@Singleton
class SaleNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManagerCompat,
) {

    /** Registers the channel; safe to call on every start and a no-op below Android 8. */
    fun createChannel() {
        val channel = NotificationChannelCompat.Builder(
            CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_DEFAULT,
        )
            .setName(context.getString(R.string.notification_channel_sales))
            .setDescription(context.getString(R.string.notification_channel_sales_description))
            .build()
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * @param sale a sale of one of the user's artists
     */
    fun notify(sale: ActivityEvent) {
        if (!notificationManager.areNotificationsEnabled()) return
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val price = formatMoney(sale.amount ?: 0)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(
                context.getString(
                    R.string.notification_sale_title,
                    sale.counterparty?.name.orEmpty(),
                    sale.artistName,
                ),
            )
            .setContentText(context.getString(R.string.notification_sale_text, price))
            .setContentIntent(openAppIntent())
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .build()
        notificationManager.notify(sale.id.hashCode(), notification)
    }

    private fun openAppIntent(): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
