package com.example.csks_creatives.presentation.toolbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppToolbar(
    title: String,
    canShowBackIcon: Boolean = false,
    canShowSearch: Boolean = false,
    canShowMenu: Boolean = false,
    canShowActionButton: Boolean = false,
    canShowAddTaskButton: Boolean = false,
    canShowTaskPaidStatusButton: Boolean = false,
    taskPaidStatus: Boolean = false, // Paid / Not
    isActionButtonEnabled: Boolean = false,
    actionButtonText: String = "Save",
    menuItems: List<ToolbarOverFlowMenuItem> = emptyList(),
    onBackClicked: () -> Unit = {},
    onSearchClicked: () -> Unit = {},
    onMenuItemClicked: (String) -> Unit = {},
    onActionButtonClicked: () -> Unit = {},
    onAddTaskIconClicked: () -> Unit = {}
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (canShowBackIcon) {
                IconButton(onClick = onBackClicked) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            if (canShowTaskPaidStatusButton) {
                if (taskPaidStatus) {
                    IconButton(
                        onClick = { /* Ignore */ }
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Paid",
                            tint = Color.Green
                        )
                    }
                } else {
                    IconButton(
                        onClick = { /* Ignore */ }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Paid", tint = Color.Red)
                    }
                }
            }
            if (canShowSearch) {
                IconButton(onClick = onSearchClicked) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
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
                var showMenu by remember { mutableStateOf(false) }
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

