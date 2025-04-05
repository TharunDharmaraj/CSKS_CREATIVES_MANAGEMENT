package com.example.csks_creatives.presentation.employeeDetailsScreen.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csks_creatives.domain.model.employee.LeaveRequest
import com.example.csks_creatives.domain.model.task.ClientTaskOverview
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import com.example.csks_creatives.domain.useCase.factories.*
import com.example.csks_creatives.domain.utils.LogoutEvent
import com.example.csks_creatives.domain.utils.Utils.formatTimeStamp
import com.example.csks_creatives.presentation.components.sealed.DateOrder
import com.example.csks_creatives.presentation.employeeDetailsScreen.viewModel.event.EmployeeDetailsScreenEvent
import com.example.csks_creatives.presentation.employeeDetailsScreen.viewModel.state.EmployeeDetailsScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmployeeDetailsScreenViewModel @Inject constructor(
    private val adminUseCase: AdminUseCaseFactory,
    private val tasksUseCase: TasksUseCaseFactory,
    private val employeeUseCaseFactory: EmployeeUseCaseFactory
) : ViewModel() {
    init {
        adminUseCase.create()
        tasksUseCase.create()
    }

    private val _activeTasksIds = MutableStateFlow<List<String>>(emptyList())
    private val _completedTasksIds = MutableStateFlow<List<String>>(emptyList())

    private val _employeeDetailsScreenState = MutableStateFlow(EmployeeDetailsScreenState())
    val employeeDetailsScreenState = _employeeDetailsScreenState.asStateFlow()

    private val tasksCompletedFetchFromFirestore =
        MutableStateFlow<List<ClientTaskOverview>>(emptyList())
    private val activeTasksFetchFromFirestore =
        MutableStateFlow<List<ClientTaskOverview>>(emptyList())

    private val _employeeDetailScreenTitle = MutableStateFlow("")
    val employeeDetailsScreenTitle = _employeeDetailScreenTitle.asStateFlow()

    private var hasInitialized = false

    fun onEvent(employeeDetailsScreenEvent: EmployeeDetailsScreenEvent) {
        when (employeeDetailsScreenEvent) {
            EmployeeDetailsScreenEvent.OnEmployeeTaskItemClicked -> {
                // TODO Go to Task Details Page
            }

            is EmployeeDetailsScreenEvent.OnSearchTextChangedForCompleted -> {
                _employeeDetailsScreenState.update {
                    it.copy(searchTextForCompleted = employeeDetailsScreenEvent.text)
                }
                filterCompletedTasks()
            }

            is EmployeeDetailsScreenEvent.OnSearchTextChangedForActive -> {
                _employeeDetailsScreenState.update {
                    it.copy(searchTextForActive = employeeDetailsScreenEvent.text)
                }
                filterActiveTasks()
            }

            is EmployeeDetailsScreenEvent.Order -> {
                val sortedCompletedTasks =
                    if (employeeDetailsScreenEvent.order == DateOrder.Ascending) {
                        _employeeDetailsScreenState.value.tasksCompleted.sortedBy { it.taskCreationTime }
                    } else {
                        _employeeDetailsScreenState.value.tasksCompleted.sortedByDescending { it.taskCreationTime }
                    }
                val sortedInProgressTasks =
                    if (employeeDetailsScreenEvent.order == DateOrder.Ascending) {
                        _employeeDetailsScreenState.value.tasksInProgress.sortedBy { it.taskCreationTime }
                    } else {
                        _employeeDetailsScreenState.value.tasksInProgress.sortedByDescending { it.taskCreationTime }
                    }
                _employeeDetailsScreenState.value = _employeeDetailsScreenState.value.copy(
                    completedTasksOrder = employeeDetailsScreenEvent.order,
                    tasksInProgress = sortedInProgressTasks,
                    tasksCompleted = sortedCompletedTasks
                )
            }

            EmployeeDetailsScreenEvent.ToggleCompletedTasksSectionVisibility -> {
                _employeeDetailsScreenState.value = _employeeDetailsScreenState.value.copy(
                    isCompletedTasksSectionVisible = !_employeeDetailsScreenState.value.isCompletedTasksSectionVisible
                )
            }

            EmployeeDetailsScreenEvent.ToggleActiveTasksSectionVisibility -> {
                _employeeDetailsScreenState.value = _employeeDetailsScreenState.value.copy(
                    isActiveTasksSectionVisible = !_employeeDetailsScreenState.value.isActiveTasksSectionVisible
                )
            }

            EmployeeDetailsScreenEvent.ToggleSearchBarVisibility -> {
                _employeeDetailsScreenState.value = _employeeDetailsScreenState.value.copy(
                    isSearchBarVisible = !_employeeDetailsScreenState.value.isSearchBarVisible
                )
            }
        }
    }

    private fun filterCompletedTasks() {
        val searchText = _employeeDetailsScreenState.value.searchTextForCompleted
        val filtered = if (searchText.isBlank()) {
            tasksCompletedFetchFromFirestore.value
        } else {
            tasksCompletedFetchFromFirestore.value.filter { task ->
                task.taskName.contains(searchText, ignoreCase = true) ||
                        task.clientId.contains(searchText, ignoreCase = true) ||
                        task.currentStatus.name.contains(searchText, ignoreCase = true)
            }
        }

        _employeeDetailsScreenState.update {
            it.copy(tasksCompleted = filtered)
        }
    }

    private fun filterActiveTasks() {
        val searchText = _employeeDetailsScreenState.value.searchTextForActive
        val filtered = if (searchText.isBlank()) {
            activeTasksFetchFromFirestore.value
        } else {
            activeTasksFetchFromFirestore.value.filter { task ->
                task.taskName.contains(searchText, ignoreCase = true) ||
                        task.clientId.contains(searchText, ignoreCase = true) ||
                        task.currentStatus.name.contains(searchText, ignoreCase = true)
            }
        }

        _employeeDetailsScreenState.update {
            it.copy(tasksInProgress = filtered)
        }
    }


    private fun getEmployeeDetails(employeeId: String) {
        viewModelScope.launch {
            adminUseCase.getEmployeeDetails(employeeId).collect { result ->
                if (result is ResultState.Success) {
                    val employeeDetails = result.data
                    observeTaskIds()
                    _activeTasksIds.value = employeeDetails.tasksInProgress
                    _completedTasksIds.value = employeeDetails.tasksCompleted
                    _employeeDetailsScreenState.value = _employeeDetailsScreenState.value.copy(
                        employeeName = employeeDetails.employeeName,
                        employeePassword = employeeDetails.employeePassword,
                        employeeJoinedTime = formatTimeStamp(employeeDetails.joinedTime),
                        totalNumberOfTasksCompleted = employeeDetails.numberOfTasksCompleted,
                    )
                }
            }
        }
    }

    private fun observeTaskIds() {
        viewModelScope.launch {
            _activeTasksIds.collect { taskIds ->
                if (taskIds.isNotEmpty()) {
                    getActiveTasksList(taskIds)
                }
            }
        }

        viewModelScope.launch {
            _completedTasksIds.collect { taskIds ->
                if (taskIds.isNotEmpty()) {
                    getCompletedTasksList(taskIds)
                }
            }
        }
    }

    private fun getCompletedTasksList(tasksCompleted: List<String>) {
        if (tasksCompleted.isNotEmpty()) {
            tasksCompleted.forEach { taskId ->
                viewModelScope.launch {
                    tasksUseCase.getTaskOverView(taskId).collect { taskOverViewResult ->
                        if (taskOverViewResult is ResultState.Success) {
                            val taskOverViewData = taskOverViewResult.data
                            val completedTasksList = tasksUseCase.getUniqueTaskOverViewList(
                                taskOverViewData, _employeeDetailsScreenState.value.tasksCompleted
                            )
                            _employeeDetailsScreenState.value =
                                _employeeDetailsScreenState.value.copy(
                                    tasksCompleted = completedTasksList.sortedByDescending { it.taskCreationTime }
                                )
                            tasksCompletedFetchFromFirestore.value =
                                _employeeDetailsScreenState.value.tasksCompleted
                        }
                    }
                }
            }
        }
    }

    private fun getActiveTasksList(activeTasks: List<String>) {
        if (activeTasks.isNotEmpty()) {
            activeTasks.forEach { taskId ->
                viewModelScope.launch {
                    tasksUseCase.getTaskOverView(taskId).collect { taskOverViewResult ->
                        if (taskOverViewResult is ResultState.Success) {
                            val taskOverViewData = taskOverViewResult.data
                            var activeTasksList = tasksUseCase.getUniqueTaskOverViewList(
                                taskOverViewData,
                                _employeeDetailsScreenState.value.tasksInProgress
                            )
                            // Handling When a task is moved from In_Progress to completed, we need to update the UI accordingly
                            if (taskOverViewData.currentStatus == TaskStatusType.COMPLETED) {
                                val newCompletedTasksList =
                                    (_employeeDetailsScreenState.value.tasksCompleted + taskOverViewData).sortedByDescending { it.taskCreationTime }
                                _employeeDetailsScreenState.update {
                                    it.copy(
                                        tasksCompleted = newCompletedTasksList
                                    )
                                }
                                activeTasksList = tasksUseCase.removeCompletedTaskFromActiveList(
                                    taskOverViewData,
                                    activeTasksList
                                )
                            }
                            _employeeDetailsScreenState.value =
                                _employeeDetailsScreenState.value.copy(
                                    tasksInProgress = activeTasksList.sortedByDescending { it.taskCreationTime }
                                )
                            activeTasksFetchFromFirestore.value =
                                _employeeDetailsScreenState.value.tasksInProgress
                        }
                    }
                }
            }
        }
    }

    fun initialize(employeeId: String) {
        if (hasInitialized) return
        hasInitialized = true
        _employeeDetailScreenTitle.value = employeeId
        getEmployeeDetails(employeeId)
        getAllLeavesTaken(employeeId)
    }

    private fun getAllLeavesTaken(employeeId: String) {
        viewModelScope.launch {
            employeeUseCaseFactory.getAllLeaveRequestsGrouped(employeeId).collect { result ->
                if (result is ResultState.Success) {
                    val groupedLeavesList = result.data
                    _employeeDetailsScreenState.update {
                        it.copy(
                            approvedLeavesList = groupedLeavesList.approved,
                            unApprovedLeavesList = groupedLeavesList.unapproved
                        )
                    }
                }
            }
        }
    }

    fun emitLogoutEvent(isUserLoggedOut: Boolean) {
        viewModelScope.launch {
            LogoutEvent.emitLogoutEvent(isUserLoggedOut)
        }
    }

    fun getTimeTakenForCompletion(taskId: String) =
        tasksUseCase.getTimeTakenForCompletedTask(
            taskId,
            _employeeDetailsScreenState.value.tasksCompleted
        )

    fun getTimeTakenForActiveTask(taskId: String) =
        tasksUseCase.getTimeTakenForActiveTask(
            taskId,
            _employeeDetailsScreenState.value.tasksInProgress
        )

    fun setEmployeeDetailsScreenToolbarTitle(title: String) {
        _employeeDetailScreenTitle.value = title
    }

    fun approveEmployeeLeave(leaveRequest: LeaveRequest) {
        viewModelScope.launch {
            adminUseCase.markLeaveRequestAsApproved(leaveRequest)
        }
    }
}