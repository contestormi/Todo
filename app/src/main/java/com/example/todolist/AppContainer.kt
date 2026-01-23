package com.example.todolist

import android.content.Context
import com.example.todolist.data.local.DbProvider
import com.example.todolist.data.notifications.TaskReminderScheduler
import com.example.todolist.data.repository.TaskRepositoryImpl
import com.example.todolist.domain.repository.TaskRepository

class AppContainer(context: Context) {
    private val db = DbProvider.create(context)
    private val dao = db.taskDao()
    private val reminderScheduler = TaskReminderScheduler(context.applicationContext)

    val taskRepository: TaskRepository = TaskRepositoryImpl(dao, reminderScheduler)
}