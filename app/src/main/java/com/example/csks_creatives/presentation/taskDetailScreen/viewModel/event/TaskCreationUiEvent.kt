package com.example.csks_creatives.presentation.taskDetailScreen.viewModel.event

import com.example.csks_creatives.presentation.loginScreen.viewModel.event.LoginUIEvent

sealed class TaskCreationUiEvent {
    data class ShowToast(val message: String) : TaskCreationUiEvent()
    object NavigateBack : TaskCreationUiEvent() // To take the user back to Admin Home Screen
}