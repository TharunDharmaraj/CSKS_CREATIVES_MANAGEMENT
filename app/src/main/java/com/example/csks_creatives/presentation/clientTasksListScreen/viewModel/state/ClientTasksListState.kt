package com.example.csks_creatives.presentation.clientTasksListScreen.viewModel.state

import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING
import com.example.csks_creatives.presentation.components.sealed.DateOrder

data class ClientTasksListState(
    val tasksList: List<ClientTask> = emptyList(),
    val isLoading: Boolean = false,
    val tasksOrder: DateOrder = DateOrder.Ascending,
    val canShowSearchIcon: Boolean = false,
    val isSearchBarVisible: Boolean = false,
    val isFilterSectionVisible: Boolean = false,
    val isAllTasksVisible: Boolean = true,
    val isPaidTasksVisible: Boolean = false,
    val isUnpaidTasksVisible: Boolean = false,
    val isAmountVisible: Boolean = false,
    val searchText: String = EMPTY_STRING,
    val selectedStatuses: Set<TaskStatusType> = emptySet()
)
