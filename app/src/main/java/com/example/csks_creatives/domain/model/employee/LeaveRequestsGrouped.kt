package com.example.csks_creatives.domain.model.employee

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class LeaveRequestsGrouped(
    val approved: List<LeaveRequest>,
    val unapproved: List<LeaveRequest>,
    val rejected: List<LeaveRequest>
)