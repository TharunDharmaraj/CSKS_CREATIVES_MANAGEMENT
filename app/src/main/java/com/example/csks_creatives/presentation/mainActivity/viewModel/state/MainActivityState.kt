package com.example.csks_creatives.presentation.mainActivity.viewModel.state

import com.example.csks_creatives.domain.model.utills.sealed.UserRole
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING

data class MainActivityState(
    val userRole: UserRole = UserRole.Employee,
    val adminName: String = EMPTY_STRING,
    val employeeId: String = EMPTY_STRING
)