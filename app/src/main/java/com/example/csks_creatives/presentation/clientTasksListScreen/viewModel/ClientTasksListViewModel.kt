package com.example.csks_creatives.presentation.clientTasksListScreen.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskPaidStatus
import com.example.csks_creatives.domain.model.utills.sealed.ResultState
import com.example.csks_creatives.domain.useCase.factories.TasksUseCaseFactory
import com.example.csks_creatives.domain.utils.LogoutEvent
import com.example.csks_creatives.presentation.clientTasksListScreen.viewModel.event.ClientTasksListScreenEvent
import com.example.csks_creatives.presentation.clientTasksListScreen.viewModel.state.ClientTasksListState
import com.example.csks_creatives.presentation.components.sealed.DateOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ClientTasksListViewModel @Inject constructor(
    private val tasksUseCaseFactory: TasksUseCaseFactory
) : ViewModel() {
    init {
        tasksUseCaseFactory.create()
    }

    private val _allTasksForClientFromFireStore = MutableStateFlow<List<ClientTask>>(emptyList())

    private val _clientTasksListState = MutableStateFlow(ClientTasksListState())
    val clientsTasksListState = _clientTasksListState.asStateFlow()

    private var hasInitialized = false

    fun onEvent(clientTaskListScreenEvent: ClientTasksListScreenEvent) {
        when (clientTaskListScreenEvent) {
            is ClientTasksListScreenEvent.OnSearchTextChanged -> {
                _clientTasksListState.value = _clientTasksListState.value.copy(
                    searchText = clientTaskListScreenEvent.searchText
                )
                filterTasks()
            }

            is ClientTasksListScreenEvent.Order -> {
                val newOrder = clientTaskListScreenEvent.order
                // Thread Safer Way. TODO Update everything  that uses _state.value = _state.copy() into a thread safer way
                _clientTasksListState.update {
                    it.copy(tasksOrder = newOrder)
                }
                sortTasks(newOrder)
            }

            is ClientTasksListScreenEvent.ToggleStatusFilter -> {
                val currentSet = _clientTasksListState.value.selectedStatuses.toMutableSet()
                if (currentSet.contains(clientTaskListScreenEvent.status)) {
                    currentSet.remove(clientTaskListScreenEvent.status)
                } else {
                    currentSet.add(clientTaskListScreenEvent.status)
                }
                _clientTasksListState.value =
                    _clientTasksListState.value.copy(selectedStatuses = currentSet)
                filterTasks()
            }

            ClientTasksListScreenEvent.ToggleSearchBarClicked -> {
                _clientTasksListState.update {
                    it.copy(
                        isSearchBarVisible = !_clientTasksListState.value.isSearchBarVisible
                    )
                }
            }

            is ClientTasksListScreenEvent.ShowOnlyUnPaidTasksFilter -> {
                _clientTasksListState.update {
                    it.copy(
                        isUnpaidTasksVisible = !it.isUnpaidTasksVisible,
                        isPartiallyPaidTasksVisible = false,
                        isPaidTasksVisible = false,
                        isAllTasksVisible = false
                    )
                }
                filterTasks()
            }

            ClientTasksListScreenEvent.ShowOnlyPaidTasksFilter -> {
                _clientTasksListState.update {
                    it.copy(
                        isPaidTasksVisible = !it.isPaidTasksVisible,
                        isPartiallyPaidTasksVisible = false,
                        isUnpaidTasksVisible = false,
                        isAllTasksVisible = false
                    )
                }
                filterTasks()
            }

            ClientTasksListScreenEvent.ShowOnlyPartiallyPaidTasksFilter -> {
                _clientTasksListState.update {
                    it.copy(
                        isPartiallyPaidTasksVisible = !it.isPartiallyPaidTasksVisible,
                        isPaidTasksVisible = false,
                        isUnpaidTasksVisible = false,
                        isAllTasksVisible = false
                    )
                }
                filterTasks()
            }

            ClientTasksListScreenEvent.ToggleFilterTasksClicked -> {
                _clientTasksListState.update {
                    it.copy(
                        isSearchBarVisible = false,
                        canShowSearchIcon = !_clientTasksListState.value.canShowSearchIcon,
                        isFilterSectionVisible = !_clientTasksListState.value.isFilterSectionVisible
                    )
                }
            }

        }
    }

    private fun getClientTasks(order: DateOrder, clientId: String) {
        viewModelScope.launch {
            _clientTasksListState.update { it.copy(isLoading = true) }
            tasksUseCaseFactory.getTasksForClient(order, clientId).collect { result ->
                if (result is ResultState.Success) {
                    val clientTasksList = result.data
                    _allTasksForClientFromFireStore.value = clientTasksList
                    _clientTasksListState.value = _clientTasksListState.value.copy(
                        tasksList = clientTasksList,
                        isAllTasksVisible = true,
                        isUnpaidTasksVisible = false,
                        isLoading = false
                    )
                    if (clientTasksList.isNotEmpty()) {
                        _clientTasksListState.value = _clientTasksListState.value.copy(
                            isFilterTasksIconVisible = true
                        )
                        filterTasks()
                    } else {
                        _clientTasksListState.value = _clientTasksListState.value.copy(
                            isFilterTasksIconVisible = false
                        )
                    }
                }
            }
        }
    }

    private fun filterTasks() {
        val searchText = _clientTasksListState.value.searchText.lowercase()
        val selectedStatuses = _clientTasksListState.value.selectedStatuses

        var filteredList = _allTasksForClientFromFireStore.value

        if (searchText.isNotBlank()) {
            filteredList = filteredList.filter { task ->
                task.taskName.lowercase().contains(searchText) ||
                        task.currentStatus.name.lowercase().contains(searchText) ||
                        task.taskAttachment.lowercase().contains(searchText)
            }
        }

        if (selectedStatuses.isNotEmpty()) {
            filteredList = filteredList.filter { task -> task.currentStatus in selectedStatuses }
        }

        when {
            _clientTasksListState.value.isUnpaidTasksVisible -> {
                filteredList = filteredList.filter { it.taskPaidStatus == TaskPaidStatus.NOT_PAID }
            }

            _clientTasksListState.value.isPaidTasksVisible -> {
                filteredList =
                    filteredList.filter { it.taskPaidStatus == TaskPaidStatus.FULLY_PAID }
            }
        }

        _clientTasksListState.value = _clientTasksListState.value.copy(tasksList = filteredList)
    }

    private fun sortTasks(order: DateOrder) {
        val sortedList = if (order is DateOrder.Ascending) {
            _clientTasksListState.value.tasksList.sortedBy { it.taskCreationTime }
        } else {
            _clientTasksListState.value.tasksList.sortedByDescending { it.taskCreationTime }
        }

        _clientTasksListState.value = _clientTasksListState.value.copy(tasksList = sortedList)
    }

    fun initialize(clientId: String) {
        if (hasInitialized) return
        hasInitialized = true
        getClientTasks(DateOrder.Descending, clientId)
    }

    fun emitLogoutEvent(isUserLoggedOut: Boolean) {
        viewModelScope.launch {
            LogoutEvent.emitLogoutEvent(isUserLoggedOut)
        }
    }

    fun getTotalUnPaidCostForClient(): Pair<Int, Int> {
        var totalCostPaid = 0
        var totalCostUnPaid = 0
        _allTasksForClientFromFireStore.value.forEach { task ->
            if (task.taskPaidStatus == TaskPaidStatus.NOT_PAID) {
                totalCostUnPaid += task.taskCost
            } else {
                totalCostPaid += task.taskCost
            }
        }
        return Pair(totalCostPaid, totalCostUnPaid)
    }

    fun getYearlyAndMonthlyCostBreakdown(): Map<Int, Map<Int, Pair<Int, Int>>> {
        val costBreakdown = mutableMapOf<Int, MutableMap<Int, Pair<Int, Int>>>()

        _allTasksForClientFromFireStore.value.forEach { task ->
            val taskCreationTime = task.taskCreationTime.toLongOrNull()
            val taskCost = task.taskCost.toString().toIntOrNull()

            if (taskCreationTime == null || taskCost == null) return@forEach

            val calendar = Calendar.getInstance().apply { timeInMillis = taskCreationTime }
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1

            val currentYearData = costBreakdown.getOrPut(year) { mutableMapOf() }
            val currentMonthData = currentYearData.getOrPut(month) { Pair(0, 0) }

            val updatedMonthData = if (task.taskPaidStatus == TaskPaidStatus.FULLY_PAID) {
                currentMonthData.copy(first = currentMonthData.first + taskCost)
            } else {
                currentMonthData.copy(second = currentMonthData.second + taskCost)
            }

            currentYearData[month] = updatedMonthData
        }

        return costBreakdown
    }


    fun setFilterAndSearchIconVisibility(isVisible: Boolean) {
        _clientTasksListState.update {
            it.copy(
                isFilterTasksIconVisible = isVisible,
                isSearchBarVisible = isVisible
            )
        }
    }
}