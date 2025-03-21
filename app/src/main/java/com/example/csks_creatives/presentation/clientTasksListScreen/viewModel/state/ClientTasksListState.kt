package com.example.csks_creatives.presentation.clientTasksListScreen.viewModel.state

import com.example.csks_creatives.domain.model.task.ClientTask
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskStatusType
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING
import com.example.csks_creatives.presentation.components.DateOrder

data class ClientTasksListState(
    val tasksList: List<ClientTask> = emptyList(),
    val tasksOrder: DateOrder = DateOrder.Ascending,
    val isSearchBarVisible: Boolean = false,
    val searchText: String = EMPTY_STRING,
    val selectedStatuses: Set<TaskStatusType> = emptySet()
)
