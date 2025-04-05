package com.example.csks_creatives.domain.utils

import com.example.csks_creatives.data.utils.Constants.BACKLOG
import com.example.csks_creatives.data.utils.Constants.COMPLETED
import com.example.csks_creatives.data.utils.Constants.FULLY_PAID
import com.example.csks_creatives.data.utils.Constants.IN_PROGRESS
import com.example.csks_creatives.data.utils.Constants.IN_REVIEW
import com.example.csks_creatives.data.utils.Constants.NOT_PAID
import com.example.csks_creatives.data.utils.Constants.REVISION_ONE
import com.example.csks_creatives.data.utils.Constants.REVISION_THREE
import com.example.csks_creatives.data.utils.Constants.REVISION_TWO
import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskPaidStatus
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
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

    fun getAvailableStatusOptions(currentStatus: TaskStatusType): List<String> {
        return when (currentStatus) {
            TaskStatusType.BACKLOG -> listOf(
                IN_PROGRESS,
                IN_REVIEW,
                REVISION_ONE,
                REVISION_TWO,
                REVISION_THREE,
                COMPLETED
            )

            TaskStatusType.BLOCKED -> listOf(
                BACKLOG,
                IN_PROGRESS,
                IN_REVIEW,
                REVISION_ONE,
                REVISION_TWO,
                REVISION_THREE,
                COMPLETED
            )

            TaskStatusType.IN_PROGRESS -> listOf(
                IN_REVIEW,
                REVISION_ONE,
                REVISION_TWO,
                REVISION_THREE,
                COMPLETED
            )

            TaskStatusType.IN_REVIEW -> listOf(
                REVISION_ONE,
                REVISION_TWO,
                REVISION_THREE,
                COMPLETED
            )

            TaskStatusType.REVISION_ONE -> listOf(REVISION_TWO, REVISION_THREE, COMPLETED)
            TaskStatusType.REVISION_TWO -> listOf(REVISION_THREE, COMPLETED)
            TaskStatusType.REVISION_THREE -> listOf(COMPLETED)
            TaskStatusType.COMPLETED -> listOf(COMPLETED)
        }
    }

    fun getTasksPaidStatusList(currentPaidStatus: TaskPaidStatus): List<String> {
        return when (currentPaidStatus) {
            TaskPaidStatus.FULLY_PAID -> listOf(
                FULLY_PAID
            )

            TaskPaidStatus.NOT_PAID -> listOf(
                NOT_PAID,
                FULLY_PAID
            )
        }
    }

    fun getCurrentTimeAsLong(): Long = System.currentTimeMillis()

    fun getCurrentTimeAsString(): String = System.currentTimeMillis().toString()

    fun formatTimeStamp(timeStampInMilliSeconds: String): String {
        if(timeStampInMilliSeconds.toString().isEmpty()) return "TimeStamp Empty"
        val timeFormat = SimpleDateFormat("HH:mm:ss MMM dd yyyy", Locale.getDefault())
        return timeFormat.format(Date(timeStampInMilliSeconds.toLong()))
    }

    fun formatTimeStampToGetJustDate(timeStampInMilliSeconds: String): String {
        if(timeStampInMilliSeconds.toString().isEmpty()) return "Date Empty"
        val timeFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return timeFormat.format(Date(timeStampInMilliSeconds.toLong()))
    }

    fun getFormattedDateTimeFormat(timeStampInMilliSeconds: String): String {
        if(timeStampInMilliSeconds.toString().isEmpty()) return "Task Creation Time Empty"
        val dateFormat = SimpleDateFormat("MMM dd yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(Date(timeStampInMilliSeconds.toLong()))
    }

    fun calculateFormattedTaskTakenTime(
        taskInProgressTime: String,
        taskCompletedTime: String
    ): String {
        val timeForTaskInProgress = taskInProgressTime.toLong()
        val timeForTaskCompleted = taskCompletedTime.toLong()
        val timeTakenMillis = timeForTaskCompleted - timeForTaskInProgress
        return getFormattedTaskTakenTime(timeTakenMillis)

    }

    fun getFormattedTaskTakenTime(timeDifference: Long): String {
        val seconds = timeDifference / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return if (days >= 1) {
            val remainingHours = hours % 24
            "$days day${if (days > 1) "s" else ""}, $remainingHours hour${if (remainingHours > 1) "s" else ""}"
        } else {
            val remainingMinutes = minutes % 60
            "$hours hour${if (hours > 1) "s" else ""}, $remainingMinutes minute${if (remainingMinutes > 1) "s" else ""}"
        }
    }

    fun getMonthName(month: Int): String {
        return DateFormatSymbols().months[month - 1]
    }

    fun Date.toTimestamp(): Timestamp {
        return Timestamp(this)
    }
}