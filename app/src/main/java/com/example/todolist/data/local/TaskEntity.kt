package com.example.todolist.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val description: String,
    val priority: Int,
    val dueAtMillis: Long?,
    val isDone: Boolean,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
