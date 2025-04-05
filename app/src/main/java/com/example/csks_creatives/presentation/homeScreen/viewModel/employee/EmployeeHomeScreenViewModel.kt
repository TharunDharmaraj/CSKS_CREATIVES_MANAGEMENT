package com.example.csks_creatives.presentation.homeScreen.viewModel.employee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import com.example.csks_creatives.domain.useCase.factories.EmployeeUseCaseFactory
import com.example.csks_creatives.domain.useCase.factories.TasksUseCaseFactory
import com.example.csks_creatives.domain.utils.LogoutEvent
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING
import com.example.csks_creatives.presentation.components.sealed.DateOrder
import com.example.csks_creatives.presentation.components.sealed.ToastUiEvent
import com.example.csks_creatives.presentation.homeScreen.viewModel.employee.event.EmployeeHomeScreenEvent
import com.example.csks_creatives.presentation.homeScreen.viewModel.employee.event.LeaveRequestDialogEvent
import com.example.csks_creatives.presentation.homeScreen.viewModel.employee.state.EmployeeHomeScreenState
import com.example.csks_creatives.presentation.homeScreen.viewModel.employee.state.LeaveRequestDialogState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class EmployeeHomeScreenViewModel @Inject constructor(
    private val tasksUseCaseFactory: TasksUseCaseFactory,
    private val employeeUseCaseFactory: EmployeeUseCaseFactory
) : ViewModel() {
    init {
        tasksUseCaseFactory.create()
        employeeUseCaseFactory.create()
    }

    private val _employeeHomeScreenState = MutableStateFlow(EmployeeHomeScreenState())
    val employeeHomeScreenState = _employeeHomeScreenState.asStateFlow()

    private val _leaveRequestDialogState = MutableStateFlow(LeaveRequestDialogState())
    val leaveRequestDialogState = _leaveRequestDialogState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ToastUiEvent>()
    val uiEvent: SharedFlow<ToastUiEvent> = _uiEvent.asSharedFlow()

    private var employeeId = EMPTY_STRING
    private var hasInitialized = false
    private var hasCompletedTasksFethed = false

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

    private fun getEmployeeActiveTasks(employeeId: String, order: DateOrder) {
        tasksUseCaseFactory.getActiveTasksForEmployee(employeeId, order)
            .onEach { result ->
                when (result) {
                    is ResultState.Error -> {
                        _uiEvent.emit(ToastUiEvent.ShowToast("Error: ${result.message}"))
                    }

                    ResultState.Loading -> {
                        _employeeHomeScreenState.value =
                            _employeeHomeScreenState.value.copy(isActiveTasksLoading = true)
                    }

                    is ResultState.Success -> {
                        val activeTasks = result.data
                        _employeeHomeScreenState.value =
                            _employeeHomeScreenState.value.copy(
                                activeTasks = activeTasks,
                                isActiveTasksLoading = false
                            )
                    }
                }
            }.launchIn(viewModelScope)
    }

    fun getEmployeeCompletedTasks(employeeId: String) {
        if (hasCompletedTasksFethed) return
        hasCompletedTasksFethed = true
        tasksUseCaseFactory.getCompletedTasksForEmployee(
            employeeId,
            _employeeHomeScreenState.value.tasksOrder
        )
            .onEach { result ->
                when (result) {
                    ResultState.Loading -> {
                        _employeeHomeScreenState.value =
                            _employeeHomeScreenState.value.copy(isCompletedTasksLoading = true)
                    }

                    is ResultState.Error -> {
                        _uiEvent.emit(ToastUiEvent.ShowToast("Error: ${result.message}"))
                    }


                    is ResultState.Success -> {
                        val activeTasks = result.data
                        _employeeHomeScreenState.value =
                            _employeeHomeScreenState.value.copy(
                                completedTasks = activeTasks,
                                isCompletedTasksLoading = false
                            )
                    }
                }
            }.launchIn(viewModelScope)
    }

    private fun sortTasks(order: DateOrder) {
        val sortedActiveTasksList: List<ClientTask>
        val sortedCompletedTasksList: List<ClientTask>
        if (order is DateOrder.Ascending) {
            sortedActiveTasksList =
                _employeeHomeScreenState.value.activeTasks.sortedBy { it.taskCreationTime }
            sortedCompletedTasksList =
                _employeeHomeScreenState.value.completedTasks.sortedBy { it.taskCreationTime }
        } else {
            sortedActiveTasksList =
                _employeeHomeScreenState.value.activeTasks.sortedByDescending { it.taskCreationTime }
            sortedCompletedTasksList =
                _employeeHomeScreenState.value.completedTasks.sortedByDescending { it.taskCreationTime }
        }

        _employeeHomeScreenState.value = employeeHomeScreenState.value.copy(
            activeTasks = sortedActiveTasksList,
            completedTasks = sortedCompletedTasksList
        )
    }

    fun initialize(employeeId: String) {
        if (hasInitialized) return
        hasInitialized = true
        this.employeeId = employeeId
        getEmployeeActiveTasks(employeeId, DateOrder.Descending)
        fetchLeaveRequests(employeeId)
    }

    private fun fetchLeaveRequests(employeeId: String) {
        viewModelScope.launch {
            employeeUseCaseFactory.getAllLeavesTaken(employeeId).collectLatest { result ->
                when (result) {
                    is ResultState.Success -> {
                        val leaveRequests = result.data
                        val approved = leaveRequests.filter { it.approvedStatus }
                            .sortedByDescending { it.leaveDate }
                        val rejected =
                            leaveRequests.filter { !it.approvedStatus }.sortedBy { it.leaveDate }

                        _employeeHomeScreenState.update {
                            it.copy(
                                approvedLeaves = approved,
                                rejectedLeaves = rejected
                            )
                        }
                    }

                    is ResultState.Error -> {
                        _uiEvent.emit(ToastUiEvent.ShowToast(result.message))
                    }

                    ResultState.Loading -> {

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

    fun getCompletedTaskTime(clientTask: ClientTask) =
        tasksUseCaseFactory.getTimeTakenForCompletedTask(clientTask)
}