package com.example.csks_creatives.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Movie
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.csks_creatives.domain.model.utills.enums.tasks.TaskType

val TaskType.displayName: String
    get() = when (this) {
        TaskType.LONG_VIDEO -> "Long"
        TaskType.SHORTS_VIDEO -> "short"
        TaskType.GRAPHIC_DESIGN -> "graphic"
    }

val TaskType.icon: ImageVector
    get() = when (this) {
        TaskType.LONG_VIDEO -> Icons.Default.Movie
        TaskType.SHORTS_VIDEO -> Icons.Default.Bolt
        TaskType.GRAPHIC_DESIGN -> Icons.Default.Brush
    }
