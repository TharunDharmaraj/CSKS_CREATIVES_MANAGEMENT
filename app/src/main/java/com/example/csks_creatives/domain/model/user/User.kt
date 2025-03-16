package com.example.csks_creatives.domain.model.user

import com.example.csks_creatives.domain.model.utills.sealed.UserRole
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING

data class User(
    val id: String = EMPTY_STRING,
    val userName: String = EMPTY_STRING,
    val userRole: UserRole
)
