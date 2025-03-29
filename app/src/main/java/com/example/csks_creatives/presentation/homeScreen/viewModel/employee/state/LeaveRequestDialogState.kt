package com.example.csks_creatives.presentation.homeScreen.viewModel.employee.state

import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING
import java.util.Date

data class LeaveRequestDialogState(
    val leaveRequestDate: Date = Date(),
    val leaveRequestReason: String = EMPTY_STRING,
)
