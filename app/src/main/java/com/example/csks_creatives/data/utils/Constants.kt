package com.example.csks_creatives.data.utils

object Constants {
    // Collection Names
    const val EMPLOYEE_COLLECTION = "Employee"
    const val ADMIN_COLLECTION = "Admin"
    const val CLIENT_COLLECTION = "Client"
    const val COMMENT_COLLECTION = "Comment"
    const val TASKS_COLLECTION = "Tasks"

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
    const val EMPLOYEES_COUNT = "Count"
    const val EMPLOYEES_COUNT_FIELD = "EmployeeCount"
    const val EMPLOYEE_COUNT_DEFAULT = 0

    // Admin Collection
    const val ADMIN_USERNAME = "UserName"
    const val ADMIN_PASSWORD = "Password"

    // Clients Collection Fields
    const val CLIENT_ID = "ClientId"
    const val CLIENT_NAME = "ClientName"
    const val CLIENT_TASKS = "ClientTasks"

    // Comments Collection Fields
    const val COMMENT_ID = "CommentId"
    const val COMMENT_STRING = "CommentString"
    const val COMMENT_COMMENTED_BY = "CommentedBy"
    const val COMMENT_TIME_STAMP = "CommentTimeStamp"
    const val ADMIN_COMMENT_OWNER = "csks"

    // Task Collection Fields
    const val TASK_ID = "taskId"
    const val TASK_CREATION_TIME = "taskCreationTime"
    const val TASK_CLIENT_ID = "clientId"
    const val TASK_EMPLOYEE_ID = "employeeId"
    const val TASK_TASK_NAME = "taskName"
    const val TASK_ATTACHMENT = "taskAttachment"
    const val TASK_POINT = "taskPoint"
    const val TASK_CURRENT_STATUS = "currentStatus"
    const val TASK_STATUS_HISTORY_SUB_COLLECTION = "StatusHistory"
    const val TASK_STATUS_HISTORY_START_TIME = "startTime"
    const val TASK_STATUS_HISTORY_END_TIME = "endTime"
    const val TASK_STATUS_HISTORY_END_TIME_DEFAULT_VALUE = 0.toLong()

    // Status Types
    const val BACKLOG = "BACKLOG"
    const val IN_PROGRESS = "IN_PROGRESS"
    const val IN_REVIEW = "IN_REVIEW"
    const val REVISION_ONE = "REVISION_ONE"
    const val REVISION_TWO = "REVISION_TWO"
    const val REVISION_THREE = "REVISION_THREE"
    const val BLOCKED = "BLOCKED"
    const val COMPLETED = "COMPLETED"

    // Admin Name
    const val ADMIN_NAME = "Kishor"
}