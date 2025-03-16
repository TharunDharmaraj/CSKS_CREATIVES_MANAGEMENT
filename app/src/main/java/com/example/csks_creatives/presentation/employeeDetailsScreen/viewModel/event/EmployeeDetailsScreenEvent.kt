package com.example.csks_creatives.presentation.employeeDetailsScreen.viewModel.event

import com.example.csks_creatives.presentation.components.DateOrder

sealed class EmployeeDetailsScreenEvent {
    data class OnSearchTextChanged(val searchText: String) : EmployeeDetailsScreenEvent()
    data class Order(val order: DateOrder) : EmployeeDetailsScreenEvent()
    object ToggleSearchBarVisibility : EmployeeDetailsScreenEvent()
    object ToggleCompletedTasksSectionVisibility : EmployeeDetailsScreenEvent()
    object ToggleActiveTasksSectionVisibility : EmployeeDetailsScreenEvent()
    object OnEmployeeTaskItemClicked : EmployeeDetailsScreenEvent()
}