package com.example.csks_creatives.domain.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object LogoutEvent {
    private val _logoutEventFlow = MutableStateFlow<Boolean>(false)
    val logoutEventFlow = _logoutEventFlow.asStateFlow()

    suspend fun emitLogoutEvent(isUserLoggedOut: Boolean){
        _logoutEventFlow.emit(isUserLoggedOut)
    }
}