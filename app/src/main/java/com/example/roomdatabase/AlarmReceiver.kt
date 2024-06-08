package com.example.roomdatabase

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ID = "Alarm"
        const val TITLE_EXTRA = "title"
        const val ID = 1
    }

    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.d("AlarmManager23", "mssg")
        if (p0 != null) {
            playAlarmSound(p0)
        }
        if (p0 != null) {
            if (p1 != null) {
                createNotificationChannel(p0, p1)
            }
        }
        if (p0 != null) {
            if (p1 != null) {
                createNotification(p0, p1)
            }
        }
    }

    private fun createNotification(p0: Context, intent: Intent) {
        val title = intent.getStringExtra("title")
        Log.d("AlarmManager23", "title$title")
        val intent = Intent(p0, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            p0,
            0,
            intent,
             PendingIntent.FLAG_IMMUTABLE
        )
        val notificationManager =
            p0.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = title
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(p0, CHANNEL_ID)
            .setSmallIcon(R.drawable.pencil)
            .setContentText(title)
            .setLargeIcon(BitmapFactory.decodeResource(p0.resources, R.drawable.pencil))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        notificationManager.notify(ID, builder.build())
    }


    private fun createNotificationChannel(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            var title = intent.getStringExtra("title")
            var description = intent.getIntExtra("id", 0)
            var chanel =
                NotificationChannel(CHANNEL_ID, "Alarm", NotificationManager.IMPORTANCE_HIGH)
            chanel.description = title
            var nm = context.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(chanel)
        }
    }

    private fun playAlarmSound(context: Context) {
        var alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        if (alarmSound != null) {
            alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
        val ringtone = RingtoneManager.getRingtone(context, alarmSound)
        if (ringtone != null) {
            ringtone.play()
        }
    }
}