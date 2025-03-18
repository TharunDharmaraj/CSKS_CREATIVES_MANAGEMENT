package com.example.csks_creatives.presentation.homeScreen.viewModel.admin.state

data class AdminHomeScreenVisibilityState(
    val isEmployeeSectionVisible: Boolean = false,
    val isClientSectionVisible: Boolean = false,
    val isActiveTaskSectionVisible: Boolean = false,
    val isBacklogTaskSectionVisible: Boolean = false,
    val isCompletedTaskSectionVisible: Boolean = false,
    val isAddEmployeeDialogVisible: Boolean = false,
    val isAddClientDialogVisible: Boolean = false,
)