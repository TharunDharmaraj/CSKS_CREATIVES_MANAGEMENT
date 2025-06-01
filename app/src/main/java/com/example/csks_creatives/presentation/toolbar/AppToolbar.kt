package com.example.csks_creatives.presentation.toolbar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.csks_creatives.R
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskPaidStatus
import com.example.csks_creatives.presentation.components.darkSlateBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppToolbar(
    title: String,
    canShowLogo: Boolean = false,
    canShowBackIcon: Boolean = false,
    canShowSearch: Boolean = false,
    canShowMenu: Boolean = false,
    canShowFilterTasks: Boolean = false,
    canShowActionButton: Boolean = false,
    canShowAddTaskButton: Boolean = false,
    canShowTaskPaidStatusButton: Boolean = false,
    taskPaidStatus: TaskPaidStatus = TaskPaidStatus.NOT_PAID,
    isActionButtonEnabled: Boolean = false,
    actionButtonText: String = "Save",
    menuItems: List<ToolbarOverFlowMenuItem> = emptyList(),
    onFilterTasksIconClicked: () -> Unit = {},
    onBackClicked: () -> Unit = {},
    onSearchClicked: () -> Unit = {},
    onMenuItemClicked: (String) -> Unit = {},
    onActionButtonClicked: () -> Unit = {},
    onAddTaskIconClicked: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    TopAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .systemBarsPadding(),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = darkSlateBlue,
        ),
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
            if (canShowBackIcon) {
                IconButton(onClick = onBackClicked) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
            if(canShowLogo){
                Image(
                    painter = painterResource(R.drawable.toolbar_logo),
                    contentDescription = "CSKS CREATIVES"
                )
            }
        },
        actions = {
            if (canShowTaskPaidStatusButton) {
                IconButton(onClick = { /* Ignore */ }) {
                    Icon(
                        imageVector = when (taskPaidStatus) {
                            TaskPaidStatus.FULLY_PAID -> Icons.Default.CheckCircle
                            TaskPaidStatus.PARTIALLY_PAID -> Icons.Default.Lock
                            TaskPaidStatus.NOT_PAID -> Icons.Default.Info
                        },
                        contentDescription = "Paid Status",
                        tint = if (taskPaidStatus == TaskPaidStatus.FULLY_PAID) Color.Green else Color.Red
                    )
                }
            }

            if (canShowSearch) {
                IconButton(onClick = onSearchClicked) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }

            if (canShowFilterTasks) {
                IconButton(onClick = onFilterTasksIconClicked) {
                    Icon(Icons.Default.Build, contentDescription = "Filter")
                }
            }

            if (canShowActionButton) {
                Button(
                    onClick = onActionButtonClicked,
                    enabled = isActionButtonEnabled
                ) {
                    Text(actionButtonText)
                }
            }

            if (canShowAddTaskButton) {
                IconButton(onClick = onAddTaskIconClicked) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }

            if (canShowMenu) {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    menuItems.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(text = item.title) },
                            onClick = {
                                showMenu = false
                                onMenuItemClicked(item.id)
                            }
                        )
                    }
                }
            }
        }
    )
}