package com.example.csks_creatives.domain.model.employee

import androidx.annotation.Keep
import com.example.csks_creatives.data.utils.Constants.LEAVE_REQUEST_APPROVAL_STATUS
import com.example.csks_creatives.data.utils.Constants.LEAVE_REQUEST_DATE
import com.example.csks_creatives.data.utils.Constants.LEAVE_REQUEST_ID
import com.example.csks_creatives.data.utils.Constants.LEAVE_REQUEST_POSTED_BY
import com.example.csks_creatives.data.utils.Constants.LEAVE_REQUEST_REASON
import com.example.csks_creatives.domain.model.utills.enums.employee.LeaveApprovalStatus
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@Keep
@IgnoreExtraProperties
data class LeaveRequest(
    @PropertyName(LEAVE_REQUEST_ID) val leaveRequestId: String = EMPTY_STRING, // Unique id, Time the request is raised
    @PropertyName(LEAVE_REQUEST_DATE) val leaveDate: Timestamp = Timestamp.now(),
    @PropertyName(LEAVE_REQUEST_REASON) val leaveReason: String = EMPTY_STRING,
    @PropertyName(LEAVE_REQUEST_POSTED_BY) val postedBy: String = EMPTY_STRING, // EMPLOYEE_ID
    @PropertyName(LEAVE_REQUEST_APPROVAL_STATUS) val approvedStatus: LeaveApprovalStatus = LeaveApprovalStatus.UN_APPROVED // Accepted or Rejected
) {
    constructor() : this("", Timestamp.now(), "", "", LeaveApprovalStatus.UN_APPROVED)
}