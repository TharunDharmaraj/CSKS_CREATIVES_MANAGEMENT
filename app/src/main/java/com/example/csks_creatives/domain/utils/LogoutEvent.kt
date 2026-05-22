package com.example.csks_creatives.domain.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object LogoutEvent {
    private val _logoutEventFlow = MutableSharedFlow<Boolean>()
    val logoutEventFlow = _logoutEventFlow.asSharedFlow()

    suspend fun emitLogoutEvent(isUserLoggedOut: Boolean){
        _logoutEventFlow.emit(isUserLoggedOut)
    }
}