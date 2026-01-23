package com.example.todolist.data

import com.example.todolist.data.local.TaskEntity
import com.example.todolist.domain.model.Priority
import com.example.todolist.domain.model.Task

fun TaskEntity.toDomain(): Task =
    Task(
        id = id,
        title = title,
        description = description,
        priority = Priority.fromValue(priority),
        dueAtMillis = dueAtMillis,
        isDone = isDone,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )

fun Task.toEntity(): TaskEntity =
    TaskEntity(
        id = id,
        title = title,
        description = description,
        priority = priority.value,
        dueAtMillis = dueAtMillis,
        isDone = isDone,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )