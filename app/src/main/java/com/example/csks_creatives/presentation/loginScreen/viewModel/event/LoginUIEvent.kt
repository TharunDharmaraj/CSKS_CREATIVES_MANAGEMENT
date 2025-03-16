package com.example.csks_creatives.presentation.loginScreen.viewModel.event

sealed class LoginUIEvent {
    data class ShowToast(val message: String) : LoginUIEvent()
}