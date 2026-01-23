package com.example.todolist.data.local

import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = TaskEntity::class)
@Entity(tableName = "tasks_fts")
data class TaskFtsEntity(
    val title: String,
    val description: String,
)
