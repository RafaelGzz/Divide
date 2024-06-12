package com.ragl.divide.data.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.ragl.divide.data.models.Frequency
import com.ragl.divide.data.models.getInMillis

class ScheduleNotificationService(
    private val context: Context
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun canScheduleExactAlarms() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }

    fun requestScheduleExactAlarmPermission() {
        Intent().also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                it.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                context.startActivity(it)
            }
        }
    }

    fun scheduleNotification(
        id: Int,
        title: String,
        content: String,
        time: Long,
        frequency: Frequency
    ) {
        val intent = Intent(context, ReminderNotificationReceiver::class.java)
        intent.putExtra(ReminderNotificationReceiver.TITLE_EXTRA, title)
        intent.putExtra(ReminderNotificationReceiver.CONTENT_EXTRA, content)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms())
                if (frequency == Frequency.DAILY)
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        time,
                        pendingIntent
                    )
                else
                    alarmManager.setInexactRepeating(
                        AlarmManager.RTC_WAKEUP,
                        time,
                        frequency.getInMillis(),
                        pendingIntent
                    )
        } else {
            if (frequency == Frequency.DAILY)
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    time,
                    pendingIntent
                )
            else
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    time,
                    frequency.getInMillis(),
                    pendingIntent
                )
        }
    }

    fun cancelNotification(id: Int) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                id,
                Intent(context, ReminderNotificationReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

}