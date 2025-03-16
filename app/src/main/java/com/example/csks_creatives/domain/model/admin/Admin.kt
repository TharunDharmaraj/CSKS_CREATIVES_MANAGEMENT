package com.example.csks_creatives.domain.model.admin

import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING

// TODO - Assign list of sub ordinates to a Admin / Employee
data class Admin(
    val userName: String = EMPTY_STRING,
    val password: String = EMPTY_STRING
)
