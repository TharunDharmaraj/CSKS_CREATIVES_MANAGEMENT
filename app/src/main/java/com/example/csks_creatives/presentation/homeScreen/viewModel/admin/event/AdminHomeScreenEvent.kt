package com.example.csks_creatives.presentation.homeScreen.viewModel.admin.event

import com.example.csks_creatives.presentation.components.sealed.DateOrder

sealed class AdminHomeScreenEvent {
    object CreateEmployeeButtonClick : AdminHomeScreenEvent()
    object CreateClientButtonClick : AdminHomeScreenEvent()
    object ToggleEmployeeSection : AdminHomeScreenEvent()
    object ToggleClientSection : AdminHomeScreenEvent()
    data class ToggleOrderDate(val order: DateOrder) : AdminHomeScreenEvent()
    object ToggleBacklogTaskSection : AdminHomeScreenEvent()
    object ToggleActiveTaskSection : AdminHomeScreenEvent()
    object ToggleCompletedTaskSection : AdminHomeScreenEvent()
    object ToggleActiveLeavesSection : AdminHomeScreenEvent()
}