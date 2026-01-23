package com.example.todolist.data.notifications

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.time.Instant
import java.util.concurrent.TimeUnit

class TaskReminderScheduler(private val context: Context) {
    private val workManager = WorkManager.getInstance(context)

    @RequiresApi(Build.VERSION_CODES.O)
    fun schedule(taskId: Long, title: String, dueAtMillis: Long) {
        val now = Instant.now().toEpochMilli()
        val remindAt =
            (dueAtMillis - REMINDER_OFFSET_MILLIS).coerceAtLeast(now + 1000L)
        val delayMillis = remindAt - now

        val request = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    TaskReminderWorker.KEY_TASK_ID to taskId,
                    TaskReminderWorker.KEY_TITLE to title,
                    TaskReminderWorker.KEY_DUE_AT to dueAtMillis,
                )
            ).addTag(tag(taskId)).build()
    }

    fun cancel(taskId: Long) {
        workManager.cancelUniqueWork(uniqueName(taskId))
    }

    private fun uniqueName(taskId: Long): String = "task_reminder_$taskId"

    private fun tag(taskId: Long): String = "task_reminder_tag_$taskId"

    companion object {
        const val REMINDER_OFFSET_MILLIS: Long = 60L * 60L * 1000L
    }
}