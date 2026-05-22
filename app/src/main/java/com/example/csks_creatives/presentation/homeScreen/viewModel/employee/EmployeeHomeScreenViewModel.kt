package com.example.csks_creatives.presentation.homeScreen.viewModel.employee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csks_creatives.domain.model.employee.LeaveRequest
import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.domain.model.utills.enums.employee.LeaveApprovalStatus
import com.example.csks_creatives.domain.model.utills.enums.employee.LeaveDuration
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import com.example.csks_creatives.domain.useCase.UserPersistenceUseCase
import com.example.csks_creatives.domain.useCase.factories.AdminUseCaseFactory
import com.example.csks_creatives.domain.useCase.factories.EmployeeUseCaseFactory
import com.example.csks_creatives.domain.useCase.factories.TasksUseCaseFactory
import com.example.csks_creatives.domain.utils.LogoutEvent
import com.example.csks_creatives.domain.utils.SecurityUtils.decrypt
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING
import com.example.csks_creatives.data.utils.Constants.DEFAULT_TASK_FETCH_LIMIT
import com.example.csks_creatives.presentation.components.sealed.DateOrder
import com.example.csks_creatives.presentation.components.sealed.ToastUiEvent
import com.example.csks_creatives.presentation.homeScreen.viewModel.employee.event.EmployeeHomeScreenEvent
import com.example.csks_creatives.presentation.homeScreen.viewModel.employee.event.LeaveRequestDialogEvent
import com.example.csks_creatives.presentation.homeScreen.viewModel.employee.state.EmployeeHomeScreenState
import com.example.csks_creatives.presentation.homeScreen.viewModel.employee.state.LeaveRequestDialogState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class EmployeeHomeScreenViewModel @Inject constructor(
    private val tasksUseCaseFactory: TasksUseCaseFactory,
    private val employeeUseCaseFactory: EmployeeUseCaseFactory,
    private val adminUseCaseFactory: AdminUseCaseFactory,
    private val userPersistenceUseCase: UserPersistenceUseCase,
    private val taskPreferenceManager: com.example.csks_creatives.data.utils.TaskPreferenceManager
) : ViewModel() {

    private val _employeeHomeScreenState = MutableStateFlow(EmployeeHomeScreenState())
    val employeeHomeScreenState = _employeeHomeScreenState.asStateFlow()

    private val _leaveRequestDialogState = MutableStateFlow(LeaveRequestDialogState())
    val leaveRequestDialogState = _leaveRequestDialogState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ToastUiEvent>()
    val uiEvent: SharedFlow<ToastUiEvent> = _uiEvent.asSharedFlow()

    private var employeeId = EMPTY_STRING
    private var hasInitialized = false

    private var activeTasksJob: Job? = null
    private var completedTasksJob: Job? = null
    private var backlogTasksJob: Job? = null

    private var leavesJob: Job? = null

    fun onEvent(employeeHomeScreenEvent: EmployeeHomeScreenEvent) {
        when (employeeHomeScreenEvent) {
            is EmployeeHomeScreenEvent.Order -> {
                _employeeHomeScreenState.update {
                    it.copy(tasksOrder = employeeHomeScreenEvent.order)
                }
                sortTasks(employeeHomeScreenEvent.order)
            }

            EmployeeHomeScreenEvent.ToggleOrderSection -> {
                _employeeHomeScreenState.value = _employeeHomeScreenState.value.copy(
                    isOrderByToggleVisible = _employeeHomeScreenState.value.isOrderByToggleVisible.not()
                )
            }

            EmployeeHomeScreenEvent.ForceFetchTasks -> {
                if (_employeeHomeScreenState.value.isActiveTasksLoading || _employeeHomeScreenState.value.isCompletedTasksLoading || _employeeHomeScreenState.value.isBacklogTasksLoading) return
                _employeeHomeScreenState.update { 
                    it.copy(
                        activeTasks = emptyList(), 
                        completedTasks = emptyList(),
                        backlogTasks = emptyList(),
                        activeTasksLimit = DEFAULT_TASK_FETCH_LIMIT,
                        completedTasksLimit = DEFAULT_TASK_FETCH_LIMIT,
                        backlogTasksLimit = DEFAULT_TASK_FETCH_LIMIT
                    ) 
                }
                getEmployeeActiveTasks(employeeId, _employeeHomeScreenState.value.tasksOrder, true)
                getEmployeeCompletedTasks(employeeId, true)
                getEmployeeBacklogTasks(employeeId, true)
            }

            EmployeeHomeScreenEvent.ForceFetchLeaves -> {
                if (_employeeHomeScreenState.value.isPaginationLoading) return // Using pagination loading as a proxy for generic loading here
                _employeeHomeScreenState.update { 
                    it.copy(
                        approvedLeaves = emptyList(), 
                        unApprovedLeaves = emptyList(), 
                        rejectedLeaves = emptyList()
                    ) 
                }
                fetchLeaveRequests(employeeId, true)
            }

            EmployeeHomeScreenEvent.LoadMoreActiveTasks -> {
                if (_employeeHomeScreenState.value.isActiveTasksEndReached || _employeeHomeScreenState.value.isPaginationLoading) return
                _employeeHomeScreenState.update { it.copy(activeTasksLimit = it.activeTasksLimit + DEFAULT_TASK_FETCH_LIMIT) }
                getEmployeeActiveTasks(employeeId, _employeeHomeScreenState.value.tasksOrder)
            }

            EmployeeHomeScreenEvent.LoadMoreCompletedTasks -> {
                if (_employeeHomeScreenState.value.isCompletedTasksEndReached || _employeeHomeScreenState.value.isPaginationLoading) return
                _employeeHomeScreenState.update { it.copy(completedTasksLimit = it.completedTasksLimit + DEFAULT_TASK_FETCH_LIMIT) }
                getEmployeeCompletedTasks(employeeId)
            }

            EmployeeHomeScreenEvent.LoadMoreBacklogTasks -> {
                if (_employeeHomeScreenState.value.isBacklogTasksEndReached || _employeeHomeScreenState.value.isPaginationLoading) return
                _employeeHomeScreenState.update { it.copy(backlogTasksLimit = it.backlogTasksLimit + DEFAULT_TASK_FETCH_LIMIT) }
                getEmployeeBacklogTasks(employeeId)
            }

            EmployeeHomeScreenEvent.FetchCompletedTasksCount -> {
                viewModelScope.launch {
                    _employeeHomeScreenState.update { it.copy(isCompletedCountLoading = true) }
                    val result = adminUseCaseFactory.forceUpdateEmployeeCompletedTasksCount(employeeId)
                    when (result) {
                        is ResultState.Success -> {
                            val count = result.data.toString()
                            taskPreferenceManager.saveCompletedTasksCount(employeeId, count)
                            _employeeHomeScreenState.update {
                                it.copy(
                                    totalNumberOfTasksCompleted = count,
                                    isCompletedCountLoading = false
                                )
                            }
                        }
                        is ResultState.Error -> {
                            _employeeHomeScreenState.update { it.copy(isCompletedCountLoading = false) }
                            _uiEvent.emit(ToastUiEvent.ShowToast(result.message))
                        }
                        ResultState.Loading -> {}
                    }
                }
            }
        }
    }

    fun onAddLeaveDialogEvent(leaveRequestDialogEvent: LeaveRequestDialogEvent) {
        when (leaveRequestDialogEvent) {
            LeaveRequestDialogEvent.CloseDialog -> {
                _employeeHomeScreenState.update {
                    it.copy(
                        isAddLeaveDialogVisible = false
                    )
                }
            }

            is LeaveRequestDialogEvent.OnLeaveRequestDateChanged -> {
                _leaveRequestDialogState.update {
                    it.copy(
                        leaveRequestDate = leaveRequestDialogEvent.date
                    )
                }
            }

            is LeaveRequestDialogEvent.OnLeaveRequestReasonChanged -> {
                _leaveRequestDialogState.update {
                    it.copy(
                        leaveRequestReason = leaveRequestDialogEvent.leaveReason
                    )
                }
            }

            is LeaveRequestDialogEvent.OnLeaveDurationChanged -> {
                _leaveRequestDialogState.update {
                    it.copy(
                        leaveDuration = if(leaveRequestDialogEvent.isHalfDay) LeaveDuration.HALF_DAY else LeaveDuration.FULL_DAY
                    )
                }
            }

            LeaveRequestDialogEvent.OpenDialog -> {
                _employeeHomeScreenState.update {
                    it.copy(
                        isAddLeaveDialogVisible = true
                    )
                }
            }

            LeaveRequestDialogEvent.SubmitLeaveRequest -> {
                viewModelScope.launch {
                    val result = employeeUseCaseFactory.addLeaveRequest(
                        postedBy = employeeId,
                        leaveDate = _leaveRequestDialogState.value.leaveRequestDate,
                        leaveDuration = _leaveRequestDialogState.value.leaveDuration,
                        leaveReason = _leaveRequestDialogState.value.leaveRequestReason
                    )
                    when (result) {
                        is ResultState.Error -> {
                            _uiEvent.emit(ToastUiEvent.ShowToast(result.message))
                        }

                        ResultState.Loading -> {
                            // Ignore
                        }

                        is ResultState.Success -> {
                            _uiEvent.emit(ToastUiEvent.ShowToast(result.data))
                            _employeeHomeScreenState.update {
                                it.copy(
                                    isAddLeaveDialogVisible = false
                                )
                            }
                            // Reset the dialog state, to not copy over past values
                            _leaveRequestDialogState.update {
                                it.copy(
                                    leaveRequestReason = EMPTY_STRING,
                                    leaveRequestDate = Date()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getEmployeeActiveTasks(employeeId: String, order: DateOrder, isForceFetch: Boolean = false) {
        activeTasksJob?.cancel()
        val limit = if (isForceFetch) null else _employeeHomeScreenState.value.activeTasksLimit

        if (!isForceFetch && _employeeHomeScreenState.value.activeTasksLimit > DEFAULT_TASK_FETCH_LIMIT) {
            _employeeHomeScreenState.update { it.copy(isPaginationLoading = true) }
        } else {
            _employeeHomeScreenState.update { it.copy(isActiveTasksLoading = true) }
        }

        activeTasksJob = tasksUseCaseFactory.getActiveTasksForEmployee(employeeId, order, isForceFetch, limit)
            .onEach { result ->
                when (result) {
                    is ResultState.Error -> {
                        _uiEvent.emit(ToastUiEvent.ShowToast("Error: ${result.message}"))
                        _employeeHomeScreenState.update { it.copy(isActiveTasksLoading = false, isPaginationLoading = false) }
                    }

                    ResultState.Loading -> {
                    }

                    is ResultState.Success -> {
                        val activeTasks = result.data
                        val isEndReached = if (limit != null) activeTasks.size < limit else true
                        _employeeHomeScreenState.update {
                            it.copy(
                                activeTasks = activeTasks,
                                isActiveTasksLoading = false,
                                isPaginationLoading = false,
                                isActiveTasksEndReached = isEndReached
                            )
                        }
                    }
                }
            }.launchIn(viewModelScope)
    }

    fun getEmployeeCompletedTasks(employeeId: String, isForceFetch: Boolean = false) {
        val currentState = _employeeHomeScreenState.value
        // If not force fetching, only proceed if we don't have data yet and aren't already loading
        if (!isForceFetch && currentState.completedTasks.isNotEmpty() && !currentState.isCompletedTasksLoading && currentState.completedTasksLimit == DEFAULT_TASK_FETCH_LIMIT) {
            return
        }
        
        completedTasksJob?.cancel()
        
        val limit = if (isForceFetch) null else _employeeHomeScreenState.value.completedTasksLimit
        if (!isForceFetch && _employeeHomeScreenState.value.completedTasksLimit > DEFAULT_TASK_FETCH_LIMIT) {
            _employeeHomeScreenState.update { it.copy(isPaginationLoading = true) }
        } else {
            _employeeHomeScreenState.update { it.copy(isCompletedTasksLoading = true) }
        }

        completedTasksJob = tasksUseCaseFactory.getCompletedTasksForEmployee(
            employeeId,
            _employeeHomeScreenState.value.tasksOrder,
            isForceFetch,
            limit
        )
            .onEach { result ->
                when (result) {
                    ResultState.Loading -> {
                    }

                    is ResultState.Error -> {
                        _uiEvent.emit(ToastUiEvent.ShowToast("Error: ${result.message}"))
                        _employeeHomeScreenState.update { it.copy(isCompletedTasksLoading = false, isPaginationLoading = false) }
                    }


                    is ResultState.Success -> {
                        val activeTasks = result.data
                        val isEndReached = if (limit != null) activeTasks.size < limit else true
                        _employeeHomeScreenState.update {
                            it.copy(
                                completedTasks = activeTasks,
                                isCompletedTasksLoading = false,
                                isPaginationLoading = false,
                                isCompletedTasksEndReached = isEndReached
                            )
                        }
                    }
                }
            }.launchIn(viewModelScope)
    }

    fun getEmployeeBacklogTasks(employeeId: String, isForceFetch: Boolean = false) {
        val currentState = _employeeHomeScreenState.value
        if (!isForceFetch && currentState.backlogTasks.isNotEmpty() && !currentState.isBacklogTasksLoading && currentState.backlogTasksLimit == DEFAULT_TASK_FETCH_LIMIT) {
            return
        }

        backlogTasksJob?.cancel()

        val limit = if (isForceFetch) null else _employeeHomeScreenState.value.backlogTasksLimit
        if (!isForceFetch && _employeeHomeScreenState.value.backlogTasksLimit > DEFAULT_TASK_FETCH_LIMIT) {
            _employeeHomeScreenState.update { it.copy(isPaginationLoading = true) }
        } else {
            _employeeHomeScreenState.update { it.copy(isBacklogTasksLoading = true) }
        }

        backlogTasksJob = tasksUseCaseFactory.getBacklogTasksForEmployee(
            employeeId,
            _employeeHomeScreenState.value.tasksOrder,
            isForceFetch,
            limit
        ).onEach { result ->
            when (result) {
                ResultState.Loading -> {}
                is ResultState.Error -> {
                    _uiEvent.emit(ToastUiEvent.ShowToast("Error: ${result.message}"))
                    _employeeHomeScreenState.update { it.copy(isBacklogTasksLoading = false, isPaginationLoading = false) }
                }
                is ResultState.Success -> {
                    val tasks = result.data
                    val isEndReached = if (limit != null) tasks.size < limit else true
                    _employeeHomeScreenState.update {
                        it.copy(
                            backlogTasks = tasks,
                            isBacklogTasksLoading = false,
                            isPaginationLoading = false,
                            isBacklogTasksEndReached = isEndReached
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun sortTasks(order: DateOrder) {
        val sortedActiveTasksList: List<ClientTask>
        val sortedCompletedTasksList: List<ClientTask>
        val sortedBacklogTasksList: List<ClientTask>
        if (order is DateOrder.Ascending) {
            sortedActiveTasksList =
                _employeeHomeScreenState.value.activeTasks.sortedBy { it.taskCreationTime }
            sortedCompletedTasksList =
                _employeeHomeScreenState.value.completedTasks.sortedBy { it.taskCreationTime }
            sortedBacklogTasksList =
                _employeeHomeScreenState.value.backlogTasks.sortedBy { it.taskCreationTime }
        } else {
            sortedActiveTasksList =
                _employeeHomeScreenState.value.activeTasks.sortedByDescending { it.taskCreationTime }
            sortedCompletedTasksList =
                _employeeHomeScreenState.value.completedTasks.sortedByDescending { it.taskCreationTime }
            sortedBacklogTasksList =
                _employeeHomeScreenState.value.backlogTasks.sortedByDescending { it.taskCreationTime }
        }

        _employeeHomeScreenState.value = employeeHomeScreenState.value.copy(
            activeTasks = sortedActiveTasksList,
            completedTasks = sortedCompletedTasksList,
            backlogTasks = sortedBacklogTasksList
        )
    }

    fun initialize(employeeId: String) {
        if (hasInitialized) return
        hasInitialized = true
        this.employeeId = employeeId
        
        // Load from preferences initially
        val storedCount = taskPreferenceManager.getCompletedTasksCount(employeeId)
        _employeeHomeScreenState.update { it.copy(totalNumberOfTasksCompleted = storedCount) }

        getEmployeeActiveTasks(employeeId, DateOrder.Descending)
        getEmployeeBacklogTasks(employeeId)
        fetchLeaveRequests(employeeId)
        fetchEmployeeDetails(employeeId)
    }

    private fun fetchEmployeeDetails(employeeId: String) {
        viewModelScope.launch {
            adminUseCaseFactory.getEmployeeDetails(employeeId).collect { result ->
                if (result is ResultState.Success) {
                    val employee = result.data
                    _employeeHomeScreenState.update {
                        it.copy(
                            employeeName = employee.employeeName,
                            employeeJoinedTime = employee.joinedTime,
                            employeePassword = decrypt(employee.employeePassword),
                            // Use stored value from preferences as per requirements
                            totalNumberOfTasksCompleted = taskPreferenceManager.getCompletedTasksCount(employeeId)
                        )
                    }
                }
            }
        }
    }

    private fun fetchLeaveRequests(employeeId: String, isForceFetch: Boolean = false) {
        leavesJob?.cancel()

        leavesJob = viewModelScope.launch {
            employeeUseCaseFactory.getAllLeavesTaken(employeeId, isForceFetch).collectLatest { result ->
                when (result) {
                    is ResultState.Success -> {
                        val leaveRequests = result.data
                        val approved =
                            leaveRequests.filter { it.approvedStatus == LeaveApprovalStatus.APPROVED }
                                .sortedByDescending { it.leaveDate }
                        val unApproved =
                            leaveRequests.filter { it.approvedStatus == LeaveApprovalStatus.UN_APPROVED }
                                .sortedBy { it.leaveDate }
                        val rejected =
                            leaveRequests.filter { it.approvedStatus == LeaveApprovalStatus.REJECTED }
                                .sortedBy { it.leaveDate }

                        _employeeHomeScreenState.update {
                            it.copy(
                                approvedLeaves = approved,
                                unApprovedLeaves = unApproved,
                                rejectedLeaves = rejected,
                                isPaginationLoading = false
                            )
                        }
                    }

                    is ResultState.Error -> {
                        _uiEvent.emit(ToastUiEvent.ShowToast(result.message))
                        _employeeHomeScreenState.update { it.copy(isPaginationLoading = false) }
                    }

                    ResultState.Loading -> {

                    }
                }
            }
        }
    }

    fun withDrawLeaveRequest(leaveRequest: LeaveRequest) {
        viewModelScope.launch {
            employeeUseCaseFactory.withDrawLeaveRequest(leaveRequest)
        }
    }

    fun reRequestLeaveRequest(leaveRequest: LeaveRequest) {
        viewModelScope.launch {
            employeeUseCaseFactory.reRequestLeaveRequest(
                leaveRequest.copy(
                    approvedStatus = LeaveApprovalStatus.UN_APPROVED
                )
            )
        }
    }

    fun emitLogoutEvent(isUserLoggedOut: Boolean) {
        viewModelScope.launch {
            if (isUserLoggedOut) {
                userPersistenceUseCase.deleteCurrentUser()
            }
            LogoutEvent.emitLogoutEvent(isUserLoggedOut)
        }
    }

    fun getCompletedTaskTime(clientTask: ClientTask) =
        tasksUseCaseFactory.getTimeTakenForCompletedTask(clientTask)

    init {
        tasksUseCaseFactory.create()
        employeeUseCaseFactory.create()
    }
}