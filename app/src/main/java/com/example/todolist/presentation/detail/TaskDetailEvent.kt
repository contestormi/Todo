package com.example.todolist.presentation.detail

sealed class TaskDetailEvent {
    data object EmptyTitleError : TaskDetailEvent()
    data object Saved : TaskDetailEvent()
    data object Deleted : TaskDetailEvent()
    data object NavigateBack : TaskDetailEvent()
}
