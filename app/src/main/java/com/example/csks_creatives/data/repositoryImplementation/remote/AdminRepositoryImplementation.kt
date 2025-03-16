package com.example.csks_creatives.data.repositoryImplementation.remote

import android.util.Log
import com.example.csks_creatives.data.utils.Constants.EMPLOYEES_COUNT
import com.example.csks_creatives.data.utils.Constants.EMPLOYEES_COUNT_FIELD
import com.example.csks_creatives.data.utils.Constants.EMPLOYEE_COLLECTION
import com.example.csks_creatives.data.utils.Constants.EMPLOYEE_COUNT_DEFAULT
import com.example.csks_creatives.data.utils.Constants.EMPLOYEE_EMPLOYEE_ID
import com.example.csks_creatives.data.utils.Constants.EMPLOYEE_EMPLOYEE_JOINED_TIME
import com.example.csks_creatives.data.utils.Constants.EMPLOYEE_EMPLOYEE_NAME
import com.example.csks_creatives.data.utils.Constants.EMPLOYEE_EMPLOYEE_NUMBER_OF_TASKS_COMPLETED
import com.example.csks_creatives.data.utils.Constants.EMPLOYEE_EMPLOYEE_PASSWORD
import com.example.csks_creatives.data.utils.Constants.EMPLOYEE_EMPLOYEE_TASKS_COMPLETED
import com.example.csks_creatives.data.utils.Constants.EMPLOYEE_EMPLOYEE_TASKS_IN_PROGRESS
import com.example.csks_creatives.data.utils.Constants.TASKS_COMPLETED_SUB_COLLECTION
import com.example.csks_creatives.data.utils.Constants.TASKS_IN_PROGRESS_OR_COMPLETED_SUB_COLLECTION_TASK_ID
import com.example.csks_creatives.data.utils.Constants.TASKS_IN_PROGRESS_SUB_COLLECTION
import com.example.csks_creatives.domain.model.employee.Employee
import com.example.csks_creatives.domain.repository.remote.AdminRepository
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepositoryImplementation @Inject constructor(
    private val firestore: FirebaseFirestore
) : AdminRepository {
    private val logTag = "AdminRepository"
    private val adminRepoCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override suspend fun createEmployee(employee: Employee) {
        val employeeId = employee.employeeId
        try {
            getEmployeePathForId(employeeId).set(
                hashMapOf(
                    EMPLOYEE_EMPLOYEE_ID to employeeId,
                    EMPLOYEE_EMPLOYEE_NAME to employee.employeeName,
                    EMPLOYEE_EMPLOYEE_JOINED_TIME to employee.joinedTime,
                    EMPLOYEE_EMPLOYEE_PASSWORD to employee.employeePassword,
                    EMPLOYEE_EMPLOYEE_TASKS_COMPLETED to employee.tasksCompleted,
                    EMPLOYEE_EMPLOYEE_TASKS_IN_PROGRESS to employee.tasksInProgress,
                    EMPLOYEE_EMPLOYEE_NUMBER_OF_TASKS_COMPLETED to employee.numberOfTasksCompleted
                ),
                SetOptions.merge()
            ).await()
            Log.d(
                logTag + "Create",
                "Successfully Created Employee on Firestore Employee : $employee"
            )
            adminRepoCoroutineScope.launch {
                incrementEmployeeCount()
            }
        } catch (error: Exception) {
            Log.d(logTag + "Create", "Failure $error Creation Employee : $employee")
        }
    }

    override suspend fun deleteEmployee(employeeId: String) {
        try {
            getEmployeePathForId(employeeId).delete().await()
            Log.d(
                logTag + "Delete",
                "Successfully Deleted Employee on Firestore EmployeeId $employeeId"
            )
            adminRepoCoroutineScope.launch {
                decrementEmployeeCount()
            }
        } catch (error: Exception) {
            Log.d(logTag + "Delete", "Failure $error in Deletion EmployeeId $employeeId")
        }
    }

    override suspend fun getEmployeeDetails(employeeId: String): Flow<Employee> = callbackFlow {
        val documentPath = getEmployeePathForId(employeeId)
        val listener = documentPath.addSnapshotListener { documentSnapShot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            var employeeDetails: Employee
            if (documentSnapShot != null) {
                var updatedEmployeeDetails = Employee()
                val employeeDetailsFetchJob = adminRepoCoroutineScope.launch {
                    employeeDetails = documentSnapShot.toObject<Employee>() ?: Employee()
                    val listOfTasksInProgress = getTasksInProgress(documentPath)
                    val listOfTasksCompleted = getTasksCompleted(documentPath)
                    updatedEmployeeDetails = employeeDetails.copy(
                        tasksCompleted = listOfTasksCompleted,
                        tasksInProgress = listOfTasksInProgress,
                        numberOfTasksCompleted = listOfTasksCompleted.size.toString()
                    )
                    Log.d(
                        logTag + "GetDetails",
                        "Employee Details for employeeId Exists $employeeDetails"
                    )
                }
                employeeDetailsFetchJob.invokeOnCompletion {
                    trySend(updatedEmployeeDetails)
                }
            }
        }
        awaitClose { listener.remove() }
    }

    private suspend fun getTasksInProgress(documentPath: DocumentReference): List<String> {
        val tasksInProgressReference =
            documentPath.collection(TASKS_IN_PROGRESS_SUB_COLLECTION).get().await()
        val tasksInProgressList = mutableListOf<String>()
        tasksInProgressReference.documents.forEach { documentSnapshot ->
            val taskId =
                documentSnapshot.getString(TASKS_IN_PROGRESS_OR_COMPLETED_SUB_COLLECTION_TASK_ID)
                    ?: EMPTY_STRING
            if (taskId.isNotEmpty()) {
                tasksInProgressList.add(taskId)
            }
        }
        return tasksInProgressList
    }

    private suspend fun getTasksCompleted(documentPath: DocumentReference): List<String> {
        val completedTasksReference =
            documentPath.collection(TASKS_COMPLETED_SUB_COLLECTION).get().await()
        val completedTasksList = mutableListOf<String>()
        completedTasksReference.documents.forEach { documentSnapshot ->
            val taskId =
                documentSnapshot.getString(TASKS_IN_PROGRESS_OR_COMPLETED_SUB_COLLECTION_TASK_ID)
                    ?: EMPTY_STRING
            if (taskId.isNotEmpty()) {
                completedTasksList.add(taskId)
            }
        }
        return completedTasksList
    }

    override suspend fun getEmployees(): List<Employee> {
        return try {
            val snapshot = firestore.collection(EMPLOYEE_COLLECTION).get().await()
            Log.d(
                logTag + "Get",
                "Successfully fetched Employees list size: ${snapshot.documents.size}"
            )
            return snapshot.documents.mapNotNull { it.toObject(Employee::class.java) }
        } catch (exception: Exception) {
            Log.d(logTag + "Get", "Error $exception in fetching Employees")
            emptyList()
        }
    }

    override suspend fun getEmployeeCount(): Int {
        return try {
            val employeeCountDocument = getPathForEmployeeCount().get().await()
            if (employeeCountDocument.exists()) {
                employeeCountDocument.getLong(EMPLOYEES_COUNT)?.toInt() ?: EMPLOYEE_COUNT_DEFAULT
            } else {
                EMPLOYEE_COUNT_DEFAULT
            }
        } catch (error: Exception) {
            Log.d(logTag + "GetCount", "Error fetching Employee Count: $error")
            EMPLOYEE_COUNT_DEFAULT
        }
    }

    override suspend fun checkEmployeeIdExists(employeeId: String): Boolean {
        try {
            val employeePath = getEmployeePathForId(employeeId).get().await()
            if (employeePath.exists()) {
                Log.d(logTag + "check", "EmployeeId already found: $employeeId")
                return true
            }
            Log.d(logTag + "check", "EmployeeId not found: $employeeId")
            return false
        } catch (exception: Exception) {
            Log.d(logTag + "check", "Error fetching Employee exists: $employeeId")
            return true
        }
    }

    override suspend fun addActiveTaskIntoEmployeeDetails(employeeId: String, taskId: String) {
        try {
            val employeeDetailsActiveTasksPath =
                getEmployeePathForId(employeeId).collection(TASKS_IN_PROGRESS_SUB_COLLECTION)
                    .document(taskId)
            employeeDetailsActiveTasksPath.set(
                hashMapOf(
                    TASKS_IN_PROGRESS_OR_COMPLETED_SUB_COLLECTION_TASK_ID to taskId
                ),
                SetOptions.merge()
            ).await()
            Log.d(
                logTag + "addActiveTask",
                "Successfully Added Active TaskId $taskId into EmployeeId $employeeId"
            )
        } catch (exception: Exception) {
            Log.d(
                logTag + "addActiveTask",
                "Error $exception in Adding Active TaskId $taskId into EmployeeId $employeeId"
            )
        }
    }

    override suspend fun removeActiveTaskFromEmployeeDetails(employeeId: String, taskId: String) {
        try {
            val employeeDetailsActiveTasksPath =
                getEmployeePathForId(employeeId).collection(TASKS_IN_PROGRESS_SUB_COLLECTION)
                    .document(taskId)
            employeeDetailsActiveTasksPath.delete().await()
            Log.d(
                logTag + "DeleteActiveTask",
                "Successfully deleted active taskId $taskId for EmployeeId $employeeId"
            )
        } catch (exception: Exception) {
            Log.d(
                logTag + "DeleteActiveTask",
                "Error $exception in deleting active taskId $taskId for EmployeeId $employeeId"
            )
        }
    }

    override suspend fun addCompletedTaskIntoEmployeeDetails(employeeId: String, taskId: String) {
        try {
            val employeeDetailsActiveTasksPath =
                getEmployeePathForId(employeeId).collection(TASKS_COMPLETED_SUB_COLLECTION)
                    .document(taskId)
            employeeDetailsActiveTasksPath.set(
                hashMapOf(
                    TASKS_IN_PROGRESS_OR_COMPLETED_SUB_COLLECTION_TASK_ID to taskId
                ),
                SetOptions.merge()
            ).await()
            Log.d(
                logTag + "addCompletedTask",
                "Successfully Added completed TaskId $taskId into EmployeeId $employeeId"
            )
        } catch (exception: Exception) {
            Log.d(
                logTag + "addCompletedTask",
                "Error $exception in Adding completed TaskId $taskId into EmployeeId $employeeId"
            )
        }
    }

    override suspend fun removeCompletedTaskFromEmployeeDetails(
        employeeId: String,
        taskId: String
    ) {
        try {
            val employeeDetailsActiveTasksPath =
                getEmployeePathForId(employeeId).collection(TASKS_COMPLETED_SUB_COLLECTION)
                    .document(taskId)
            employeeDetailsActiveTasksPath.delete().await()
            Log.d(
                logTag + "DeleteCompletedTask",
                "Successfully deleted Completed taskId $taskId for EmployeeId $employeeId"
            )
        } catch (exception: Exception) {
            Log.d(
                logTag + "DeleteCompletedTask",
                "Error $exception in deleting completed taskId $taskId for EmployeeId $employeeId"
            )
        }
    }

    private fun getEmployeePathForId(employeeId: String) =
        firestore.collection(EMPLOYEE_COLLECTION).document(employeeId)

    private fun getPathForEmployeeCount() =
        firestore.collection(EMPLOYEE_COLLECTION).document(EMPLOYEES_COUNT)

    private suspend fun incrementEmployeeCount() {
        try {
            val employeeCountDocument = getPathForEmployeeCount().get().await()
            val employeeCount =
                (employeeCountDocument.getLong(EMPLOYEES_COUNT_FIELD) ?: EMPLOYEE_COUNT_DEFAULT)

            getPathForEmployeeCount().set(
                hashMapOf(EMPLOYEES_COUNT_FIELD to employeeCount.toInt() + 1),
                SetOptions.merge()
            ).await()

            Log.d(logTag + "AddCount", "Successfully Updated Employee Count")
        } catch (error: Exception) {
            Log.d(logTag + "AddCount", "Failure $error On Updating Employee Count")
        }
    }

    private suspend fun decrementEmployeeCount() {
        try {
            val employeeCountDocument = getPathForEmployeeCount().get().await()
            val employeeCount =
                (employeeCountDocument.getLong(EMPLOYEES_COUNT_FIELD) ?: EMPLOYEE_COUNT_DEFAULT)

            getPathForEmployeeCount().set(
                hashMapOf(EMPLOYEES_COUNT_FIELD to employeeCount.toInt() - 1),
                SetOptions.merge()
            ).await()

            Log.d(logTag + "RemoveCount", "Successfully Updated Employee Count")
        } catch (error: Exception) {
            Log.d(logTag + "RemoveCount", "Failure $error On Updating Employee Count")
        }
    }
}