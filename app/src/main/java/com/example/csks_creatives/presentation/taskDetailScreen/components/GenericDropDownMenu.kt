package com.example.csks_creatives.presentation.taskDetailScreen.components

import androidx.compose.runtime.Composable

@Composable
fun GenericDropdownMenu(
    label: String,
    selectedItem: String,
    items: List<String>,
    onItemSelected: (String) -> Unit,
    enabled: Boolean = true,
    isVisible: Boolean = true
) {
    if (isVisible) {
        DropdownMenuWithSelection(
            label = label,
            selectedItem = selectedItem,
            items = items,
            onItemSelected = onItemSelected,
            enabled = enabled
        )
    }
}
