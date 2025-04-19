package com.example.csks_creatives.data.utils

object Constants {
    // Collection Names
    const val EMPLOYEE_COLLECTION = "Employee"
    const val ADMIN_COLLECTION = "Admin"
    const val CLIENT_COLLECTION = "Client"
    const val TASKS_COLLECTION = "Tasks"
    const val LEAVE_REQUESTS_COLLECTION = "LeaveRequests"

    // Employee Collection Fields
    const val EMPLOYEE_EMPLOYEE_ID = "employeeId"
    const val EMPLOYEE_EMPLOYEE_NAME = "employeeName"
    const val EMPLOYEE_EMPLOYEE_JOINED_TIME = "joinedTime"
    const val EMPLOYEE_EMPLOYEE_PASSWORD = "employeePassword"
    const val EMPLOYEE_EMPLOYEE_TASKS_COMPLETED = "tasksCompleted"
    const val EMPLOYEE_EMPLOYEE_TASKS_IN_PROGRESS = "tasksInProgress"
    const val TASKS_COMPLETED_SUB_COLLECTION = "TasksCompleted"
    const val TASKS_IN_PROGRESS_SUB_COLLECTION = "TasksInProgress"
    const val TASKS_IN_PROGRESS_OR_COMPLETED_SUB_COLLECTION_TASK_ID = "taskId"
    const val EMPLOYEE_EMPLOYEE_NUMBER_OF_TASKS_COMPLETED = "numberOfTasksCompleted"
    const val LEAVES_SUB_COLLECTION = "Leaves"

    // LeaveRequests
    const val LEAVE_REQUEST_ID = "leaveRequestId"
    const val LEAVE_REQUEST_DATE = "leaveDate"
    const val LEAVE_REQUEST_REASON = "leaveReason"
    const val LEAVE_REQUEST_POSTED_BY = "postedBy"
    const val LEAVE_REQUEST_APPROVAL_STATUS = "approvedStatus"

    // Admin Collection
    const val ADMIN_USERNAME = "UserName"
    const val ADMIN_PASSWORD = "Password"
    const val ADMIN_DOCUMENT_NAME = "kishor"

    // Clients Collection Fields
    const val CLIENT_ID = "ClientId"
    const val CLIENT_NAME = "ClientName"

    // Task Collection Fields
    const val TASK_ID = "taskId"
    const val TASK_CREATION_TIME = "taskCreationTime"
    const val TASK_CLIENT_ID = "clientId"
    const val TASK_EMPLOYEE_ID = "employeeId"
    const val TASK_TASK_NAME = "taskName"
    const val TASK_ATTACHMENT = "taskAttachment"
    const val TASK_ESTIMATE = "taskEstimate"
    const val TASK_TYPE = "taskType"
    const val TASK_COST = "taskCost"
    const val TASK_PAID_STATUS = "taskPaidStatus"
    const val TASK_FULLY_PAID_DATE = "taskFullyPaidDate"
    const val TASK_PRIORITY = "taskPriority"
    const val TASK_DIRECTION_APP = "taskDirectionApp"
    const val TASK_UPLOAD_OUTPUT = "taskUploadOutput"
    const val TASK_CURRENT_STATUS = "currentStatus"
    const val COMMENT_SUB_COLLECTION = "Comment"
    const val TASK_STATUS_HISTORY_SUB_COLLECTION = "StatusHistory"
    const val TASK_STATUS_HISTORY_START_TIME = "startTime"
    const val TASK_STATUS_HISTORY_END_TIME = "endTime"
    const val TASK_STATUS_HISTORY_END_TIME_DEFAULT_VALUE = 0L
    const val TASK_PAYMENTS_INFO_SUB_COLLECTION = "PaymentsInfo"
    const val TASK_PAYMENTS_INFO_AMOUNT = "amount"
    const val TASK_PAYMENTS_INFO_PAYMENT_DATE = "paymentDate"

    // Comments Collection Fields
    const val COMMENT_ID = "CommentId"
    const val COMMENT_STRING = "CommentString"
    const val COMMENT_COMMENTED_BY = "CommentedBy"
    const val COMMENT_TIME_STAMP = "CommentTimeStamp"
    const val ADMIN_COMMENT_OWNER = "kishor"

    // Status Types
    const val BACKLOG = "BACKLOG"
    const val IN_PROGRESS = "IN_PROGRESS"
    const val IN_REVIEW = "IN_REVIEW"
    const val REVISION_ONE = "REVISION_ONE"
    const val REVISION_TWO = "REVISION_TWO"
    const val REVISION_THREE = "REVISION_THREE"
    const val BLOCKED = "BLOCKED"
    const val COMPLETED = "COMPLETED"

    // Task Paid Status
    const val FULLY_PAID = "FULLY_PAID"
    const val PARTIALLY_PAID = "PARTIALLY_PAID"
    const val NOT_PAID = "NOT_PAID"

    // Admin Name
    const val ADMIN_NAME = "Kishor"

    //FCM
    const val FCM_TOKEN_FIELD = "fcmToken"
    const val FCM_TOKEN_LAST_UPDATED_FIELD = "fcmTokenLastUpdated" // Just Used for debugging purposes
}