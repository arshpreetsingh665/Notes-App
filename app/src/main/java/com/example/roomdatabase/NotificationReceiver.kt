package com.example.roomdatabase

import android.R
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title")
        val taskId = intent.getStringExtra("id")

        if (title != null && taskId != null) {
            showNotification(context, title, taskId)
        }
    }

    private fun showNotification(context: Context, title: String, taskId: String) {
        val channelId = "Your_Channel_ID" // Ensure this matches your notification channel ID
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.sym_def_app_icon)
            .setContentTitle("Notes")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationId = taskId.toIntOrNull() ?: 0 // Use task ID as notification ID, fallback to 0 if null or invalid
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}
