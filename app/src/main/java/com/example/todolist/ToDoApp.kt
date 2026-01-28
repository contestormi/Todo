package com.example.todolist

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.todolist.data.notifications.NotificationHelper
import com.example.todolist.di.AppComponent
import com.example.todolist.di.DaggerAppComponent
import com.example.todolist.di.DatabaseModule

class ToDoApp : Application() {
    val appComponent: AppComponent by lazy {
        DaggerAppComponent.builder()
            .databaseModule(DatabaseModule(this))
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        val component = appComponent
        
        val workerFactory = component.taskReminderWorkerFactory()
        val configuration = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
        WorkManager.initialize(this, configuration)
        
        NotificationHelper.ensureChannelExists(this)
    }
}
