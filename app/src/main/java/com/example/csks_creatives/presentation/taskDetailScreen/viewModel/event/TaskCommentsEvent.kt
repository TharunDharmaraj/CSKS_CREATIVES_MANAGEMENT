package com.example.csks_creatives.presentation.taskDetailScreen.viewModel.event

sealed class TaskCommentsEvent {
    object CreateComment : TaskCommentsEvent() // On Adding Comment
    object CancelComment : TaskCommentsEvent() // On Cancelling Comment - Discarding
    data class commentStringChanged(val commentDescription: String) : TaskCommentsEvent()
}