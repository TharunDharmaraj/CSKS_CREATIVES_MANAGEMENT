package com.example.csks_creatives.domain.utils

import com.example.csks_creatives.data.utils.Constants.BACKLOG
import com.example.csks_creatives.data.utils.Constants.COMPLETED
import com.example.csks_creatives.data.utils.Constants.IN_PROGRESS
import com.example.csks_creatives.data.utils.Constants.IN_REVIEW
import com.example.csks_creatives.data.utils.Constants.IN_REVISION
import com.example.csks_creatives.data.utils.Constants.PAUSED
import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import com.example.csks_creatives.domain.model.utills.sealed.UserRole
import com.google.firebase.Timestamp
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {
    const val EMPTY_STRING = ""

    fun List<ClientTask>.getActiveTasks(): List<ClientTask> =
        this.filter { it.currentStatus != TaskStatusType.COMPLETED }

    fun List<ClientTask>.getCompletedTasks(): List<ClientTask> =
        this.filter { it.currentStatus == TaskStatusType.COMPLETED }

    fun getAvailableStatusOptions(currentStatus: TaskStatusType, userRole: UserRole): List<String> {
        return when (currentStatus) {
            TaskStatusType.BACKLOG -> listOf(
                IN_PROGRESS,
                IN_REVIEW,
                IN_REVISION,
                PAUSED,
                COMPLETED
            )

            TaskStatusType.IN_PROGRESS -> listOf(
                IN_REVIEW,
                IN_REVISION,
                PAUSED,
                COMPLETED
            )

            TaskStatusType.IN_REVIEW -> listOf(
                IN_REVISION,
                PAUSED,
                COMPLETED
            )

            TaskStatusType.IN_REVISION -> listOf(
                PAUSED,
                COMPLETED
            )

            TaskStatusType.COMPLETED -> if (userRole is UserRole.Admin) {
                listOf(
                    BACKLOG,
                    COMPLETED
                )
            } else {
                listOf(
                    COMPLETED
                )
            }

            TaskStatusType.PAUSED -> listOf(
                IN_PROGRESS,
                IN_REVIEW,
                IN_REVISION,
                COMPLETED
            )

            // Added for backward compatibility with version 1.0
            TaskStatusType.REVISION_1, TaskStatusType.REVISION_2, TaskStatusType.REVISION_3,
            TaskStatusType.REVISION_4, TaskStatusType.REVISION_5, TaskStatusType.REVISION_6,
            TaskStatusType.REVISION_7 -> listOf(
                IN_PROGRESS,
                IN_REVIEW,
                IN_REVISION,
                PAUSED,
                COMPLETED
            )
        }
    }

    fun getCurrentTimeAsLong(): Long = System.currentTimeMillis()

    fun getCurrentTimeAsString(): String = System.currentTimeMillis().toString()

    fun formatTimeStamp(timeStampInMilliSeconds: String): String {
        if (timeStampInMilliSeconds.isEmpty()) return "TimeStamp Empty"
        val timeFormat = SimpleDateFormat("HH:mm:ss MMM dd yyyy", Locale.getDefault())
        return timeFormat.format(Date(timeStampInMilliSeconds.toLong()))
    }

    fun formatTimeStampToGetJustDate(timeStampInMilliSeconds: String): String {
        if (timeStampInMilliSeconds.isEmpty()) return "Date Empty"
        val timeFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return timeFormat.format(Date(timeStampInMilliSeconds.toLong()))
    }

    fun getFormattedDateTimeFormat(timeStampInMilliSeconds: String): String {
        if (timeStampInMilliSeconds.isEmpty()) return "Task Creation Time Empty"
        val dateFormat = SimpleDateFormat("MMM dd yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(Date(timeStampInMilliSeconds.toLong()))
    }

    fun getMonthName(month: Int): String {
        return DateFormatSymbols().months[month - 1]
    }

    fun Date.toTimestamp(): Timestamp {
        return Timestamp(this)
    }
}