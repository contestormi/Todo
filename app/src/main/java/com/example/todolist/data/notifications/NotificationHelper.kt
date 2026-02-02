package com.example.todolist.data.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.todolist.R

object NotificationHelper {
    const val CHANNEL_ID = "task_reminders"

    fun ensureChannelExists(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val applicationContext = context.applicationContext
        val notificationManager =
            applicationContext.getSystemService(NotificationManager::class.java) ?: return

        if (notificationManager.getNotificationChannel(CHANNEL_ID) != null) {
            return
        }
        
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Напоминания о задачах",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Уведомления о приближении сркоа выполнения"
        }
        notificationManager.createNotificationChannel(channel)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showReminder(
        context: Context,
        notificationId: Int,
        title: String,
        content: String
    ) {
        val applicationContext = context.applicationContext
        ensureChannelExists(applicationContext)
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(notificationId, notification)
    }
}