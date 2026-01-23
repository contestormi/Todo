package com.example.todolist.domain.usecase

import com.example.todolist.domain.repository.TaskRepository

class DeleteTaskUseCase(
    private val repository: TaskRepository,
) {
    suspend operator fun invoke(id: Long) = repository.deleteById(id)
}

