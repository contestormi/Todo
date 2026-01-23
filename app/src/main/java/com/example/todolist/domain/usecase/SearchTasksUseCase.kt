package com.example.todolist.domain.usecase

import com.example.todolist.domain.model.Task
import com.example.todolist.domain.repository.TaskRepository
import com.example.todolist.domain.repository.TaskSort
import kotlinx.coroutines.flow.Flow

class SearchTasksUseCase(
    private val repository: TaskRepository,
) {
    operator fun invoke(query: String, sort: TaskSort): Flow<List<Task>> =
        repository.searchTasks(query, sort)
}

