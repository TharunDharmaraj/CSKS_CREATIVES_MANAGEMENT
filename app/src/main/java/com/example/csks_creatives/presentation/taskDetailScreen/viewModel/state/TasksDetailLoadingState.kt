package com.example.csks_creatives.presentation.taskDetailScreen.viewModel.state

data class TasksDetailLoadingState(
    val isTaskCommentsLoading: Boolean = false,
    val isTaskStatusHistoryLoading: Boolean = false,
    val isEmployeeListLoadingForAssigningTask: Boolean = false,
    val isClientListLoadingForAssigningTask: Boolean = false,
    val isTaskDescriptionLoading: Boolean = false
)
