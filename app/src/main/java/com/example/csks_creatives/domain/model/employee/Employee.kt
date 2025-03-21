package com.example.csks_creatives.domain.model.employee

import androidx.annotation.Keep
import com.example.csks_creatives.data.utils.Constants.EMPLOYEE_EMPLOYEE_ID
import com.example.csks_creatives.data.utils.Constants.EMPLOYEE_EMPLOYEE_JOINED_TIME
import com.example.csks_creatives.data.utils.Constants.EMPLOYEE_EMPLOYEE_NAME
import com.example.csks_creatives.data.utils.Constants.EMPLOYEE_EMPLOYEE_NUMBER_OF_TASKS_COMPLETED
import com.example.csks_creatives.data.utils.Constants.EMPLOYEE_EMPLOYEE_PASSWORD
import com.example.csks_creatives.data.utils.Constants.EMPLOYEE_EMPLOYEE_TASKS_COMPLETED
import com.example.csks_creatives.data.utils.Constants.EMPLOYEE_EMPLOYEE_TASKS_IN_PROGRESS
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

// Employees / EmployeeId
@Keep
@IgnoreExtraProperties
data class Employee(
    @PropertyName(EMPLOYEE_EMPLOYEE_ID) val employeeId: String = EMPTY_STRING,
    @PropertyName(EMPLOYEE_EMPLOYEE_NAME) val employeeName: String = EMPTY_STRING,
    @PropertyName(EMPLOYEE_EMPLOYEE_PASSWORD) val employeePassword: String = EMPTY_STRING,
    @PropertyName(EMPLOYEE_EMPLOYEE_JOINED_TIME) val joinedTime: String = EMPTY_STRING,
    @PropertyName(EMPLOYEE_EMPLOYEE_TASKS_IN_PROGRESS) val tasksInProgress: List<String> = emptyList(),
    @PropertyName(EMPLOYEE_EMPLOYEE_TASKS_COMPLETED) val tasksCompleted: List<String> = emptyList(),
    @PropertyName(EMPLOYEE_EMPLOYEE_NUMBER_OF_TASKS_COMPLETED) val numberOfTasksCompleted: String = EMPTY_STRING
) {
    constructor() : this("", "", "", "", emptyList(), emptyList(), "")
}
