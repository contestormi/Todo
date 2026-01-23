package com.example.todolist.domain.repository

import kotlinx.coroutines.flow.Flow
import com.example.todolist.domain.model.Task

enum class TaskSort {
    CREATED_AT_DESC,
    PRIORITY_DESC,
}

interface TaskRepository {
    fun observeTasks(sort: TaskSort): Flow<List<Task>>
    fun searchTasks(query: String, sort: TaskSort): Flow<List<Task>>
    fun observeTask(id: Long): Flow<Task?>

    suspend fun upsert(task: Task): Long
    suspend fun deleteById(id: Long)
}
