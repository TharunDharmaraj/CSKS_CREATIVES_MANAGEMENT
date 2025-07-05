package com.example.csks_creatives.presentation.taskDetailScreen.viewModel.event

sealed class TaskDeletionEvent {
    object DeleteTask : TaskDeletionEvent()
    object CancelDeleteTask : TaskDeletionEvent()
}