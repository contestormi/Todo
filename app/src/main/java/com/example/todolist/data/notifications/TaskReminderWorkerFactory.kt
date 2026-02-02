package com.example.todolist.data.notifications

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.todolist.data.formatter.DateFormatter
import javax.inject.Inject

class TaskReminderWorkerFactory @Inject constructor(
    private val dateFormatter: DateFormatter
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            TaskReminderWorker::class.java.name -> {
                TaskReminderWorker(appContext, workerParameters, dateFormatter)
            }
            else -> null
        }
    }
}

