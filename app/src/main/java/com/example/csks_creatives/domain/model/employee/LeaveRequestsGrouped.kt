package com.example.csks_creatives.domain.model.employee

data class LeaveRequestsGrouped(
    val approved: List<LeaveRequest>,
    val unapproved: List<LeaveRequest>,
    val rejected: List<LeaveRequest>
)