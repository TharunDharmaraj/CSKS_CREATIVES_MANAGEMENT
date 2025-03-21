package com.example.csks_creatives.domain.model.login

import com.example.csks_creatives.domain.model.utills.sealed.UserRole
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING

data class CurrentLoginUser(
    val userRole: UserRole = UserRole.Employee,
    val adminName: String = EMPTY_STRING,
    val employeeId: String = EMPTY_STRING
)
