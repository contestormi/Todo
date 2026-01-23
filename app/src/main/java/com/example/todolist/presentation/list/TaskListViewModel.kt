package com.example.todolist.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.todolist.domain.model.Task
import com.example.todolist.domain.repository.TaskRepository
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

class TaskListViewModel(repository: TaskRepository) : ViewModel() {
    private val observeTasks = ObserveTasksUseCase(repository)
    private val searchTasks = SearchTasksUseCase(repository)

    val query = MutableStateFlow("")
    val sort = MutableStateFlow(TaskSort.CREATED_AT_DESC)

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<List<Task>> =
        combine(query.debounce(250), sort) { q, s -> q to s }
            .flatMapLatest { (q, s) ->
                if (q.isBlank()) observeTasks(s) else searchTasks(q, s)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    fun setQuery(value: String) {
        query.value = value
    }

    fun setSort(value: TaskSort) {
        sort.value = value
    }

    class Factory(
        private val repository: TaskRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TaskListViewModel(repository) as T
        }
    }
}