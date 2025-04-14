package com.example.csks_creatives.presentation.taskDetailScreen.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.event.TaskDetailEvent
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.state.DropDownListState
import com.example.csks_creatives.presentation.taskDetailScreen.viewModel.state.TaskDetailState

@Composable
fun AmountSection(
    taskState: TaskDetailState,
    dropDownListState: DropDownListState,
    onEvent: (TaskDetailEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = taskState.taskCost.toString(),
            onValueChange = { value ->
                onEvent(TaskDetailEvent.TaskCostChanged(value.toIntOrNull() ?: 0))
            },
            label = { Text("Task Cost") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(12.dp))

        DropdownMenuWithSelection(
            label = "Paid Status",
            selectedItem = dropDownListState.taskPaidStatusList
                .find { it == taskState.taskPaidStatus }
                ?.name ?: "Select",
            items = dropDownListState.taskPaidStatusList.map { it.name },
            onItemSelected = { selected ->
                dropDownListState.taskPaidStatusList.find { it.name == selected }
                    ?.let { paidStatus ->
                        onEvent(TaskDetailEvent.TaskPaidStatusChanged(paidStatus))
                    }
            },
            enabled = true
        )
    }
}