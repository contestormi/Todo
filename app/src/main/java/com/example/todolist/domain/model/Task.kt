package com.example.todolist.domain.model

data class Task(
    val id: Long,
    val title: String,
    val description: String,
    val priority: Priority,
    val dueAtMillis: Long?,
    val isDone: Boolean,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
