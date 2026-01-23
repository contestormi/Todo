package com.example.todolist.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.todolist.data.local.TaskDao
import com.example.todolist.data.notifications.TaskReminderScheduler
import com.example.todolist.data.toDomain
import com.example.todolist.data.toEntity
import com.example.todolist.domain.model.Task
import com.example.todolist.domain.repository.TaskRepository
import com.example.todolist.domain.repository.TaskSort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

class TaskRepositoryImpl(
    private val dao: TaskDao,
    private val reminderScheduler: TaskReminderScheduler,
) : TaskRepository {

    override fun observeTasks(sort: TaskSort): Flow<List<Task>> =
        when (sort) {
            TaskSort.CREATED_AT_DESC -> dao.observeAllOrderByCreatedAtDesc()
            TaskSort.PRIORITY_DESC -> dao.observeAllOrderByPriorityDesc()
        }.map { list -> list.map { it.toDomain() } }

    override fun searchTasks(query: String, sort: TaskSort): Flow<List<Task>> {
        val ftsQuery = buildFtsQuery(query)
        return when (sort) {
            TaskSort.CREATED_AT_DESC -> dao.searchOrderByCreatedAtDesc(ftsQuery)
            TaskSort.PRIORITY_DESC -> dao.searchOrderByPriorityDesc(ftsQuery)
        }.map { list -> list.map { it.toDomain() } }
    }

    override fun observeTask(id: Long): Flow<Task?> =
        dao.observeById(id).map { it?.toDomain() }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun upsert(task: Task): Long {
        val now = Instant.now().toEpochMilli()
        val toSave = task.copy(
            createdAtMillis = if (task.id == 0L) now else task.createdAtMillis,
            updatedAtMillis = now,
        )
        val id = dao.upsert(toSave.toEntity())

        val finalId = if (toSave.id == 0L) id else toSave.id
        val due = toSave.dueAtMillis

        if (due != null && !toSave.isDone) {
            reminderScheduler.schedule(finalId, toSave.title, due)
        } else {
            reminderScheduler.cancel(finalId)
        }

        return finalId
    }

    override suspend fun deleteById(id: Long) {
        dao.deleteById(id)
        reminderScheduler.cancel(id)
    }

    private fun buildFtsQuery(query: String): String {
        val trimmed = query.trim()
        return trimmed
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .joinToString(" ") { token -> "${escapeForFts(token)}*" }
            .ifBlank { "*" }
    }

    private fun escapeForFts(token: String): String {
        return token.replace("\"", "")
    }
}

