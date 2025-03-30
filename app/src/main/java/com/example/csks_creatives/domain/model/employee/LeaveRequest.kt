package com.example.csks_creatives.domain.model.employee

import com.example.csks_creatives.data.utils.Constants.LEAVE_REQUEST_APPROVAL_STATUS
import com.example.csks_creatives.data.utils.Constants.LEAVE_REQUEST_DATE
import com.example.csks_creatives.data.utils.Constants.LEAVE_REQUEST_ID
import com.example.csks_creatives.data.utils.Constants.LEAVE_REQUEST_POSTED_BY
import com.example.csks_creatives.data.utils.Constants.LEAVE_REQUEST_REASON
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class LeaveRequest(
    @PropertyName(LEAVE_REQUEST_ID) val leaveRequestId: String = EMPTY_STRING, // Unique id, Time the request is raised
    @PropertyName(LEAVE_REQUEST_DATE) val leaveDate: Timestamp = Timestamp.now(),
    @PropertyName(LEAVE_REQUEST_REASON) val leaveReason: String = EMPTY_STRING,
    @PropertyName(LEAVE_REQUEST_POSTED_BY) val postedBy: String = EMPTY_STRING, // EMPLOYEE_ID
    @PropertyName(LEAVE_REQUEST_APPROVAL_STATUS) val approvedStatus: Boolean = false // Accepted or Rejected // TODO Make Rejection Reason from admin & re-raise request
)