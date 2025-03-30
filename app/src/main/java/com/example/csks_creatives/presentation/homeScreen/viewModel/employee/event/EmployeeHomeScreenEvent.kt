package com.example.csks_creatives.presentation.homeScreen.viewModel.employee.event

import com.example.csks_creatives.presentation.components.sealed.DateOrder

sealed class EmployeeHomeScreenEvent {
    data class Order(val order: DateOrder) : EmployeeHomeScreenEvent()
    object ToggleOrderSection : EmployeeHomeScreenEvent()
    object ToggleActiveTasksSection : EmployeeHomeScreenEvent()
    object ToggleCompletedTasksSection : EmployeeHomeScreenEvent()
}