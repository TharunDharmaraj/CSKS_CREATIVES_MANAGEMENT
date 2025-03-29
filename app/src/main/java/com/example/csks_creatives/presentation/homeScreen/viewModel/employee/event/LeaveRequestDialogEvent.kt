package com.example.csks_creatives.presentation.homeScreen.viewModel.employee.event

import java.util.Date

sealed class LeaveRequestDialogEvent {
    object OpenDialog : LeaveRequestDialogEvent()
    object CloseDialog : LeaveRequestDialogEvent()
    data class OnLeaveRequestDateChanged(val date: Date) : LeaveRequestDialogEvent()
    data class OnLeaveRequestReasonChanged(val leaveReason: String) : LeaveRequestDialogEvent()
    object SubmitLeaveRequest : LeaveRequestDialogEvent()
}
