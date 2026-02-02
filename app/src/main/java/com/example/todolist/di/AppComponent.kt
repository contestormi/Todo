package com.example.todolist.di

import android.app.Application
import android.content.Context
import com.example.todolist.data.formatter.DateFormatter
import com.example.todolist.data.notifications.TaskReminderWorkerFactory
import com.example.todolist.presentation.detail.TaskDetailViewModel
import com.example.todolist.presentation.list.TaskListViewModel
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        DatabaseModule::class,
        RepositoryModule::class
    ]
)
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
            @BindsInstance context: Context
        ): AppComponent
    }

    fun dateFormatter(): DateFormatter
    fun taskReminderWorkerFactory(): TaskReminderWorkerFactory
    fun taskListViewModelFactory(): TaskListViewModel.Factory
    fun taskDetailViewModelFactory(): TaskDetailViewModel.Factory
}
