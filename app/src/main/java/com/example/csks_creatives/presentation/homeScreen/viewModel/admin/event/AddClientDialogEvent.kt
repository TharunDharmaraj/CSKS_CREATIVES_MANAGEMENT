package com.example.csks_creatives.presentation.homeScreen.viewModel.admin.event

sealed class AddClientDialogEvent {
    object AddClientButtonClicked : AddClientDialogEvent()
    object CloseDialogButtonClicked : AddClientDialogEvent()
    data class ClientNameTextFieldChanged(val clientName: String) : AddClientDialogEvent()
}