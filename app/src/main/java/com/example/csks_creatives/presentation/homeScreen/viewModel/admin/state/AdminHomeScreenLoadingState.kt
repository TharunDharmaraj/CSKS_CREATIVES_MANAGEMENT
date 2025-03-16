package com.example.csks_creatives.presentation.homeScreen.viewModel.admin.state

data class AdminHomeScreenLoadingState(
    val isClientsLoading: Boolean = false,
    val isEmployeesLoading: Boolean = false,
    val isActiveTasksLoading: Boolean = false,
    val isBacklogTasksLoading: Boolean = false,
    val isCompletedTasksLoading: Boolean = false
)