package com.example.csks_creatives.presentation.components.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PaginationLoader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(50),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Please wait...", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

fun LazyListState.isAtBottom(): Boolean {
    val layoutInfo = this.layoutInfo
    val visibleItemsInfo = layoutInfo.visibleItemsInfo
    if (layoutInfo.totalItemsCount == 0) return false
    val lastVisibleItem = visibleItemsInfo.lastOrNull()
    return lastVisibleItem != null && lastVisibleItem.index >= layoutInfo.totalItemsCount - 1
}
