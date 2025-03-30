package com.example.csks_creatives.presentation.components.sealed

sealed class ToastUiEvent {
    data class ShowToast(val message: String) : ToastUiEvent()
}