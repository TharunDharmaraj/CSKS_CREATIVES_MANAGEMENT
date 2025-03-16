package com.example.csks_creatives.presentation.homeScreen.viewModel.admin.event

sealed class AdminHomeScreenEvent {
    object CreateEmployeeButtonClick : AdminHomeScreenEvent()
    object CreateClientButtonClick : AdminHomeScreenEvent()
    object CreateTaskButtonClick : AdminHomeScreenEvent()
    object ToggleEmployeeSection : AdminHomeScreenEvent()
    object ToggleClientSection : AdminHomeScreenEvent()
    object ToggleBacklogTaskSection : AdminHomeScreenEvent()
    object ToggleActiveTaskSection : AdminHomeScreenEvent()
    object ToggleCompletedTaskSection : AdminHomeScreenEvent()
    object EmployeeItemClick : AdminHomeScreenEvent()
    object ClientItemClick : AdminHomeScreenEvent()
    object TaskItemClick : AdminHomeScreenEvent()
}