package com.example.csks_creatives.data.utils

import android.content.Context
import com.example.csks_creatives.data.utils.Constants.COMPLETED_TASKS_COUNT_PREFIX
import com.example.csks_creatives.data.utils.Constants.TASK_PREFERENCES
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskPreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(TASK_PREFERENCES, Context.MODE_PRIVATE)

    fun saveCompletedTasksCount(employeeId: String, count: String) {
        prefs.edit().putString(COMPLETED_TASKS_COUNT_PREFIX + employeeId, count).apply()
    }

    fun getCompletedTasksCount(employeeId: String): String {
        return prefs.getString(COMPLETED_TASKS_COUNT_PREFIX + employeeId, "0") ?: "0"
    }
}
