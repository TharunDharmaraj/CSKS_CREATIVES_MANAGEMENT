package com.example.csks_creatives.presentation.employeeDetailsScreen.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csks_creatives.domain.model.employee.LeaveRequest
import com.example.csks_creatives.domain.model.task.ClientTaskOverview
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import com.example.csks_creatives.domain.useCase.factories.*
import com.example.csks_creatives.domain.utils.LogoutEvent
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING
import com.example.csks_creatives.domain.utils.Utils.formatTimeStamp
import com.example.csks_creatives.presentation.components.sealed.DateOrder
import com.example.csks_creatives.presentation.components.sealed.ToastUiEvent
import com.example.csks_creatives.presentation.employeeDetailsScreen.viewModel.event.EmployeeDetailsScreenEvent
import com.example.csks_creatives.presentation.employeeDetailsScreen.viewModel.state.EmployeeDetailsScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmployeeDetailsScreenViewModel @Inject constructor(
    private val adminUseCaseFactory: AdminUseCaseFactory,
    private val tasksUseCaseFactory: TasksUseCaseFactory,
    private val employeeUseCaseFactory: EmployeeUseCaseFactory
) : ViewModel() {
    init {
        adminUseCaseFactory.create()
        tasksUseCaseFactory.create()
    }

    private val _activeTasksIds = MutableStateFlow<List<String>>(emptyList())
    private val _completedTasksIds = MutableStateFlow<List<String>>(emptyList())

    private val _employeeDetailsScreenState = MutableStateFlow(EmployeeDetailsScreenState())
    val employeeDetailsScreenState = _employeeDetailsScreenState.asStateFlow()

    private val tasksCompletedFetchFromFirestore =
        MutableStateFlow<List<ClientTaskOverview>>(emptyList())
    private val activeTasksFetchFromFirestore =
        MutableStateFlow<List<ClientTaskOverview>>(emptyList())

    private val _employeeDetailScreenTitle = MutableStateFlow(EMPTY_STRING)
    val employeeDetailsScreenTitle = _employeeDetailScreenTitle.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ToastUiEvent>()
    val uiEvent: SharedFlow<ToastUiEvent> = _uiEvent.asSharedFlow()

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
            adminUseCaseFactory.getEmployeeDetails(employeeId).collect { result ->
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
                } else {
                    _employeeDetailsScreenState.value =
                        _employeeDetailsScreenState.value.copy(
                            isActiveTasksLoading = false,
                            tasksInProgress = emptyList()
                        )
                }
            }
        }

        viewModelScope.launch {
            _completedTasksIds.collect { taskIds ->
                if (taskIds.isNotEmpty()) {
                    getCompletedTasksList(taskIds)
                } else {
                    _employeeDetailsScreenState.value =
                        _employeeDetailsScreenState.value.copy(
                            isCompletedTasksLoading = false,
                            tasksCompleted = emptyList()
                        )
                }
            }
        }
    }

    private fun getCompletedTasksList(tasksCompleted: List<String>) {
        if (tasksCompleted.isNotEmpty()) {
            tasksCompleted.forEach { taskId ->
                viewModelScope.launch {
                    tasksUseCaseFactory.getTaskOverView(taskId).collect { taskOverViewResult ->
                        when (taskOverViewResult) {
                            is ResultState.Error -> {
                                _employeeDetailsScreenState.value =
                                    _employeeDetailsScreenState.value.copy(
                                        isCompletedTasksLoading = false
                                    )
                                _uiEvent.emit(ToastUiEvent.ShowToast("Error retrieving data ${taskOverViewResult.message}"))
                            }

                            ResultState.Loading -> {
                                _employeeDetailsScreenState.value =
                                    _employeeDetailsScreenState.value.copy(
                                        isCompletedTasksLoading = true
                                    )
                            }

                            is ResultState.Success<ClientTaskOverview> -> {
                                _employeeDetailsScreenState.value =
                                    _employeeDetailsScreenState.value.copy(
                                        isCompletedTasksLoading = true
                                    )
                                val taskOverViewData = taskOverViewResult.data
                                val completedTasksList =
                                    tasksUseCaseFactory.getUniqueTaskOverViewList(
                                        taskOverViewData,
                                        _employeeDetailsScreenState.value.tasksCompleted
                                    )
                                _employeeDetailsScreenState.value =
                                    _employeeDetailsScreenState.value.copy(
                                        isCompletedTasksLoading = false,
                                        tasksCompleted = completedTasksList.sortedByDescending { it.taskCreationTime }
                                    )
                                tasksCompletedFetchFromFirestore.value =
                                    _employeeDetailsScreenState.value.tasksCompleted
                            }
                        }
                    }
                }
            }
        } else {
            _employeeDetailsScreenState.value =
                _employeeDetailsScreenState.value.copy(
                    isCompletedTasksLoading = false,
                    tasksCompleted = emptyList()
                )
        }
    }

    private fun getActiveTasksList(activeTasks: List<String>) {
        if (activeTasks.isNotEmpty()) {
            activeTasks.forEach { taskId ->
                viewModelScope.launch {
                    tasksUseCaseFactory.getTaskOverView(taskId).collect { taskOverViewResult ->
                        when (taskOverViewResult) {
                            is ResultState.Error -> {
                                _employeeDetailsScreenState.update {
                                    it.copy(
                                        isActiveTasksLoading = false
                                    )
                                }
                                _uiEvent.emit(ToastUiEvent.ShowToast("Error retrieving data ${taskOverViewResult.message}"))
                            }

                            ResultState.Loading -> {
                                _employeeDetailsScreenState.update {
                                    it.copy(
                                        isActiveTasksLoading = true
                                    )
                                }
                            }

                            is ResultState.Success<ClientTaskOverview> -> {
                                _employeeDetailsScreenState.update {
                                    it.copy(
                                        isActiveTasksLoading = true
                                    )
                                }
                                val taskOverViewData = taskOverViewResult.data
                                var activeTasksList = tasksUseCaseFactory.getUniqueTaskOverViewList(
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
                                    activeTasksList =
                                        tasksUseCaseFactory.removeCompletedTaskFromActiveList(
                                            taskOverViewData,
                                            activeTasksList
                                        )
                                }
                                _employeeDetailsScreenState.value =
                                    _employeeDetailsScreenState.value.copy(
                                        isActiveTasksLoading = false,
                                        tasksInProgress = activeTasksList.sortedByDescending { it.taskCreationTime }
                                    )
                                activeTasksFetchFromFirestore.value =
                                    _employeeDetailsScreenState.value.tasksInProgress
                            }
                        }
                    }
                }
            }
        } else {
            _employeeDetailsScreenState.value =
                _employeeDetailsScreenState.value.copy(
                    isActiveTasksLoading = false,
                    tasksInProgress = emptyList()
                )
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
                            unApprovedLeavesList = groupedLeavesList.unapproved,
                            rejectedLeavesList = groupedLeavesList.rejected
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
        tasksUseCaseFactory.getTimeTakenForCompletedTask(
            taskId,
            _employeeDetailsScreenState.value.tasksCompleted
        )

    fun getTimeTakenForActiveTask(taskId: String) =
        tasksUseCaseFactory.getTimeTakenForActiveTask(
            taskId,
            _employeeDetailsScreenState.value.tasksInProgress
        )

    fun setEmployeeDetailsScreenToolbarTitle(title: String) {
        _employeeDetailScreenTitle.value = title
    }

    fun approveEmployeeLeave(leaveRequest: LeaveRequest) {
        viewModelScope.launch {
            adminUseCaseFactory.markLeaveRequestAsApproved(leaveRequest)
        }
    }

    fun rejectEmployeeLeave(leaveRequest: LeaveRequest) {
        viewModelScope.launch {
            adminUseCaseFactory.markLeaveRequestAsRejected(leaveRequest)
        }
    }
}