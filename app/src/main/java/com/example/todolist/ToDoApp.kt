package com.example.todolist

import android.app.Application
import com.example.todolist.data.notifications.NotificationHelper

class ToDoApp : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
        NotificationHelper.ensureChannelExists(this)
    }
}