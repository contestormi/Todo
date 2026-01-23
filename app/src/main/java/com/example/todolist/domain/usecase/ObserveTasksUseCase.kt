package com.example.todolist.domain.usecase


import com.example.todolist.domain.model.Task
import com.example.todolist.domain.repository.TaskRepository
import com.example.todolist.domain.repository.TaskSort
import kotlinx.coroutines.flow.Flow

class ObserveTasksUseCase(
    private val repository: TaskRepository,
) {
    operator fun invoke(sort: TaskSort): Flow<List<Task>> =
        repository.observeTasks(sort)
}

