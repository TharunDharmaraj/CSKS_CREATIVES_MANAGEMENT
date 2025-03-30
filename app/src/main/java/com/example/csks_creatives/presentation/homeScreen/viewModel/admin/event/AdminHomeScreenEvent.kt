package com.example.csks_creatives.presentation.homeScreen.viewModel.admin.event

sealed class AdminHomeScreenEvent {
    object CreateEmployeeButtonClick : AdminHomeScreenEvent()
    object CreateClientButtonClick : AdminHomeScreenEvent()
    object ToggleEmployeeSection : AdminHomeScreenEvent()
    object ToggleClientSection : AdminHomeScreenEvent()
    object ToggleBacklogTaskSection : AdminHomeScreenEvent()
    object ToggleActiveTaskSection : AdminHomeScreenEvent()
    object ToggleCompletedTaskSection : AdminHomeScreenEvent()
    object ToggleActiveLeavesSection : AdminHomeScreenEvent()
}