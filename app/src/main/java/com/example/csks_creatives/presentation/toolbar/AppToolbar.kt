package com.example.csks_creatives.presentation.toolbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppToolbar(
    title: String,
    actionButtonText: String = "Save",
    canShowBackIcon: Boolean = false,
    canShowSearch: Boolean = false,
    canShowMenu: Boolean = true,
    canShowActionButton: Boolean = false,
    canShowAddTaskButton: Boolean = false,
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
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            if (canShowSearch) {
                IconButton(onClick = onSearchClicked) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }

            if (canShowActionButton) {
                Button(
                    onClick = onActionButtonClicked
                ) {
                    Text(actionButtonText)
                }
            }

            if(canShowAddTaskButton){
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

