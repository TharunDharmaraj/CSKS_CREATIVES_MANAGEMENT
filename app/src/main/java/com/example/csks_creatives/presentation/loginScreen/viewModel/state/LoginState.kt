package com.example.csks_creatives.presentation.loginScreen.viewModel.state

import com.example.csks_creatives.domain.model.utills.sealed.UserRole
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING

data class LoginState(
    val userName: String = EMPTY_STRING,
    val password: String = EMPTY_STRING,
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val errorMessage: String? = null,
    val userRole: UserRole? = null,
    val employeeId: String = EMPTY_STRING
)
