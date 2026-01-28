package com.example.todolist.di

import com.example.todolist.data.formatter.DateFormatter
import com.example.todolist.data.notifications.TaskReminderWorkerFactory
import com.example.todolist.domain.usecase.DeleteTaskUseCase
import com.example.todolist.domain.usecase.ObserveTaskUseCase
import com.example.todolist.domain.usecase.ObserveTasksUseCase
import com.example.todolist.domain.usecase.SearchTasksUseCase
import com.example.todolist.domain.usecase.UpsertTaskUseCase
import com.example.todolist.presentation.detail.TaskDetailViewModel
import com.example.todolist.presentation.list.TaskListViewModel
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
    fun observeTasksUseCase(): ObserveTasksUseCase
    fun searchTasksUseCase(): SearchTasksUseCase
    fun observeTaskUseCase(): ObserveTaskUseCase
    fun upsertTaskUseCase(): UpsertTaskUseCase
    fun deleteTaskUseCase(): DeleteTaskUseCase
    fun dateFormatter(): DateFormatter
    fun taskReminderWorkerFactory(): TaskReminderWorkerFactory
    fun taskListViewModelFactory(): TaskListViewModel.Factory
    fun taskDetailViewModelFactory(): TaskDetailViewModel.Factory
}
