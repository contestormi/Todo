package com.example.todolist.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.formatter.DateFormatter
import com.example.todolist.domain.model.Task
import com.example.todolist.domain.usecase.DeleteTaskUseCase
import com.example.todolist.domain.usecase.ObserveTaskUseCase
import com.example.todolist.domain.usecase.UpsertTaskUseCase
import com.example.todolist.presentation.model.TaskFormData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskDetailViewModel(
    private val taskId: Long?,
    observeTask: ObserveTaskUseCase,
    private val upsertTask: UpsertTaskUseCase,
    private val deleteTask: DeleteTaskUseCase,
    private val dateFormatter: DateFormatter,
) : ViewModel() {
    private val _dueAtMillis = MutableStateFlow<Long?>(null)

    val formattedDueDate: StateFlow<String?> = _dueAtMillis.map { millis ->
        millis?.let { dateFormatter.format(it) }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )

    val task: StateFlow<Task?> =
        if (taskId == null) {
            MutableStateFlow(null)
        } else {
            observeTask(taskId)
                .onEach { loaded -> _dueAtMillis.value = loaded?.dueAtMillis }
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5_000),
                    null
                )
        }

    fun setDueAt(value: Long?) {
        _dueAtMillis.value = value
    }

    fun save(
        formData: TaskFormData,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val base = task.value
            val toSave = Task(
                id = base?.id ?: 0L,
                title = formData.title.trim(),
                description = formData.description.trim(),
                priority = formData.priority,
                dueAtMillis = _dueAtMillis.value,
                isDone = formData.isDone,
                createdAtMillis = base?.createdAtMillis ?: 0L,
                updatedAtMillis = base?.updatedAtMillis ?: 0L,
            )
            upsertTask(toSave)
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        val id = taskId ?: return
        viewModelScope.launch {
            deleteTask(id)
            onDone()
        }
    }

    class Factory @Inject constructor(
        private val observeTask: ObserveTaskUseCase,
        private val upsertTask: UpsertTaskUseCase,
        private val deleteTask: DeleteTaskUseCase,
        private val dateFormatter: DateFormatter,
    ) {
        fun create(taskId: Long?): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(TaskDetailViewModel::class.java)) {
                        "Unknown ViewModel class: ${modelClass.name}"
                    }
                    val viewModel = TaskDetailViewModel(
                        taskId,
                        observeTask,
                        upsertTask,
                        deleteTask,
                        dateFormatter
                    )
                    return modelClass.cast(viewModel)
                        ?: throw IllegalStateException("Failed to cast ViewModel")
                }
            }
        }
    }
}