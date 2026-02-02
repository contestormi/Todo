package com.example.todolist.domain.usecase

import com.example.todolist.domain.repository.TaskRepository
import javax.inject.Inject

class DeleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    suspend operator fun invoke(id: Long) = repository.deleteById(id)
}

