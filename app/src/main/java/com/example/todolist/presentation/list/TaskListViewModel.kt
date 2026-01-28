package com.example.todolist.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.todolist.domain.model.Task
import com.example.todolist.domain.repository.TaskSort
import com.example.todolist.domain.usecase.ObserveTasksUseCase
import com.example.todolist.domain.usecase.SearchTasksUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class TaskListViewModel(
    private val observeTasks: ObserveTasksUseCase,
    private val searchTasks: SearchTasksUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")

    private val _sort = MutableStateFlow(TaskSort.CREATED_AT_DESC)

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<List<Task>> =
        combine(_query.debounce(250), _sort) { q, s -> q to s }
            .flatMapLatest { (q, s) ->
                if (q.isBlank()) observeTasks(s) else searchTasks(q, s)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    fun setQuery(value: String) {
        _query.value = value
    }

    fun setSort(value: TaskSort) {
        _sort.value = value
    }

    class Factory @Inject constructor(
        private val observeTasks: ObserveTasksUseCase,
        private val searchTasks: SearchTasksUseCase,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(TaskListViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            val viewModel = TaskListViewModel(observeTasks, searchTasks)
            return modelClass.cast(viewModel)
                ?: throw IllegalStateException("Failed to cast ViewModel")
        }
    }
}
