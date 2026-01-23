package com.example.todolist.domain.usecase

import com.example.todolist.domain.model.Task
import com.example.todolist.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class ObserveTaskUseCase(
    private val repository: TaskRepository,
) {
    operator fun invoke(id: Long): Flow<Task?> = repository.observeTask(id)
}

