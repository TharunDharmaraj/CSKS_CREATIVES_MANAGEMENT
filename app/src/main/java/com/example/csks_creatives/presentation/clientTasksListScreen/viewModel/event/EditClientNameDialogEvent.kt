package com.example.csks_creatives.presentation.clientTasksListScreen.viewModel.event

sealed interface EditClientNameDialogEvent {
    object CancelClicked : EditClientNameDialogEvent
    object SaveClicked : EditClientNameDialogEvent
    data class OnClientNameTextEdit(val clientName: String) : EditClientNameDialogEvent
}