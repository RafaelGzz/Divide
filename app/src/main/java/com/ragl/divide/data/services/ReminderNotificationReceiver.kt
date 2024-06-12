package com.ragl.divide.data.services

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.ragl.divide.MainActivity
import com.ragl.divide.R

class ReminderNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "reminder_channel"
        const val TITLE_EXTRA = "titleExtra"
        const val CONTENT_EXTRA = "contentExtra"
        const val NOTIFICATION_ID = 1
    }

    private fun isNotificationsPermissionGranted(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

    fun showNotification(
        context: Context,
        title: String = "",
        content: String = "",
        icon: Int = R.drawable.baseline_attach_money_24
    ) {
        if (!isNotificationsPermissionGranted(context)) {
            return
        }

        val activityIntent = Intent(context, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            context,
            1,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(activityPendingIntent)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(
            1, notification
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        showNotification(
            context,
            intent.getStringExtra(TITLE_EXTRA)!!,
            intent.getStringExtra(CONTENT_EXTRA)!!
        )
    }

}