package com.example.csks_creatives.presentation.homeScreen.viewModel.admin.state

data class AdminHomeScreenLoadingState(
    val isClientsLoading: Boolean = false,
    val isEmployeesLoading: Boolean = true,
    val isActiveTasksLoading: Boolean = true,
    val isBacklogTasksLoading: Boolean = false,
    val isCompletedTasksLoading: Boolean = false,
    val isLeaveRequestsLoading: Boolean = true
)