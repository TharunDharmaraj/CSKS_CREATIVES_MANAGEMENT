package com.example.csks_creatives.presentation.homeScreen.viewModel.admin.state

import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING
import java.util.UUID

data class AddEmployeeDialogState(
    val employeeId: String = UUID.randomUUID().toString(),
    val employeeName: String = EMPTY_STRING,
    val employeePassword: String = EMPTY_STRING
)
