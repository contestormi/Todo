package com.example.todolist.data.notifications

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todolist.R
import com.example.todolist.data.formatter.DateFormatter

class TaskReminderWorker(
    appContext: Context,
    params: WorkerParameters,
    private val dateFormatter: DateFormatter,
) : CoroutineWorker(appContext, params) {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        val taskId = inputData.getLong(KEY_TASK_ID, -1L)
        val title = inputData.getString(KEY_TITLE).orEmpty()
        val dueAt = inputData.getLong(KEY_DUE_AT, -1L)

        if (taskId <= 0L || title.isBlank() || dueAt <= 0L) return Result.success()

        val dueText = dateFormatter.format(dueAt)

        NotificationHelper.showReminder(
            context = applicationContext,
            notificationId = taskId.toInt(),
            title = applicationContext.getString(R.string.dueSoon),
            content = "$title до $dueText"
        )

        return Result.success()
    }

    companion object {
        const val KEY_TASK_ID = "task_id"
        const val KEY_TITLE = "title"
        const val KEY_DUE_AT = "due_at"
    }
}