package com.example.todolist.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.formatter.DateFormatter
import com.example.todolist.domain.model.Priority
import com.example.todolist.domain.model.Task
import com.example.todolist.domain.usecase.DeleteTaskUseCase
import com.example.todolist.domain.usecase.ObserveTaskUseCase
import com.example.todolist.domain.usecase.UpsertTaskUseCase
import com.example.todolist.presentation.model.TaskFormData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
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
    private val _events = MutableSharedFlow<TaskDetailEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<TaskDetailEvent> = _events

    private val _dueAtMillis = MutableStateFlow<Long?>(null)
    private val _initialTaskToBind = MutableStateFlow<Task?>(null)
    val initialTaskToBind: StateFlow<Task?> = _initialTaskToBind

    private val _selectedPriority = MutableStateFlow(Priority.MEDIUM)
    val selectedPriority: StateFlow<Priority> = _selectedPriority

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
                .onEach { loaded ->
                    _dueAtMillis.value = loaded?.dueAtMillis
                    if (loaded != null && _initialTaskToBind.value == null) {
                        _initialTaskToBind.value = loaded
                    }
                }
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5_000),
                    null
                )
        }

    fun markInitialDataBound() {
        _initialTaskToBind.value = null
    }

    fun setSelectedPriority(priority: Priority) {
        _selectedPriority.value = priority
    }

    fun setDueAt(value: Long?) {
        _dueAtMillis.value = value
    }

    fun save(formData: TaskFormData) {
        viewModelScope.launch {
            if (formData.title.isBlank()) {
                _events.tryEmit(TaskDetailEvent.EmptyTitleError)
                return@launch
            }
            val base = task.value
            val toSave = Task(
                id = base?.id ?: 0L,
                title = formData.title.trim(),
                description = formData.description.trim(),
                priority = _selectedPriority.value,
                dueAtMillis = _dueAtMillis.value,
                isDone = formData.isDone,
                createdAtMillis = base?.createdAtMillis ?: 0L,
                updatedAtMillis = base?.updatedAtMillis ?: 0L,
            )
            upsertTask(toSave)
            _events.tryEmit(TaskDetailEvent.Saved)
            _events.tryEmit(TaskDetailEvent.NavigateBack)
        }
    }

    fun delete() {
        val id = taskId ?: return
        viewModelScope.launch {
            deleteTask(id)
            _events.tryEmit(TaskDetailEvent.Deleted)
            _events.tryEmit(TaskDetailEvent.NavigateBack)
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