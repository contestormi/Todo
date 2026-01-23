package com.example.todolist.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.todolist.domain.model.Priority
import com.example.todolist.domain.model.Task
import com.example.todolist.domain.repository.TaskRepository
import com.example.todolist.domain.usecase.DeleteTaskUseCase
import com.example.todolist.domain.usecase.ObserveTaskUseCase
import com.example.todolist.domain.usecase.UpsertTaskUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskDetailViewModel(
    private val taskId: Long,
    repository: TaskRepository,
) : ViewModel() {
    val dueAtMillis = MutableStateFlow<Long?>(null)

    private val observeTask = ObserveTaskUseCase(repository)
    private val upsertTask = UpsertTaskUseCase(repository)
    private val deleteTask = DeleteTaskUseCase(repository)

    val task: StateFlow<Task?> =
        if (taskId == 0L) {
            MutableStateFlow(null)
        } else {
            observeTask(taskId)
                .onEach { loaded -> dueAtMillis.value = loaded?.dueAtMillis }
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5_000),
                    null
                )
        }

    fun setDueAt(value: Long?) {
        dueAtMillis.value = value
    }

    fun save(
        title: String,
        description: String,
        priority: Priority,
        isDone: Boolean,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val base = task.value
            val toSave = Task(
                id = base?.id ?: 0L,
                title = title.trim(),
                description = description.trim(),
                priority = priority,
                dueAtMillis = dueAtMillis.value,
                isDone = isDone,
                createdAtMillis = base?.createdAtMillis ?: 0L,
                updatedAtMillis = base?.updatedAtMillis ?: 0L,
            )
            upsertTask(toSave)
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        if (taskId == 0L) return
        viewModelScope.launch {
            deleteTask(taskId)
            onDone()
        }
    }

    class Factory(
        private val taskId: Long,
        private val repository: TaskRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TaskDetailViewModel(taskId, repository) as T
        }
    }
}