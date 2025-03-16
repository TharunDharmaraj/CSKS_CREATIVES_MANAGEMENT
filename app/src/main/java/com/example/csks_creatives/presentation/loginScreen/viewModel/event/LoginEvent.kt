package com.example.csks_creatives.presentation.loginScreen.viewModel.event

sealed class LoginEvent {
    data class OnUserNameTextFieldChanged(val userName: String) : LoginEvent()
    data class OnPasswordTextFieldChanged(val password: String) : LoginEvent()
    object LoginButtonClick : LoginEvent()
}