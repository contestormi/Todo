package com.example.todolist.presentation.model

import com.example.todolist.domain.model.Priority

data class TaskFormData(
    val title: String,
    val description: String,
    val priority: Priority,
    val isDone: Boolean,
)

