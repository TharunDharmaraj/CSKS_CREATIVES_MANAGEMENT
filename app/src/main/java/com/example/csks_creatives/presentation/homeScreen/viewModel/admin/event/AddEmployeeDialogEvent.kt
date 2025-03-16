package com.example.csks_creatives.presentation.homeScreen.viewModel.admin.event

sealed class AddEmployeeDialogEvent {
    object AddEmployeeButtonClicked : AddEmployeeDialogEvent()
    object CloseDialogButtonClicked : AddEmployeeDialogEvent()
    data class EmployeeNameTextFieldChanged(val employeeName: String) : AddEmployeeDialogEvent()
    data class EmployeeNamePasswordFieldChanged(val employeePassword: String) :
        AddEmployeeDialogEvent()
}