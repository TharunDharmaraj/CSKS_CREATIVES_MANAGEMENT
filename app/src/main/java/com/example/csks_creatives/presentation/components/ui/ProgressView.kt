package com.example.csks_creatives.presentation.components.ui

import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.csks_creatives.presentation.components.Constants.PROGRESS_INDICATOR_SIZE
import com.example.csks_creatives.presentation.components.Constants.PROGRESS_INDICATOR_STROKE
import com.example.csks_creatives.presentation.components.tealGreen

@Composable
fun LoadingProgress() {
    CircularProgressIndicator(
        modifier = Modifier.width(PROGRESS_INDICATOR_SIZE),
        color = tealGreen,
        strokeWidth = PROGRESS_INDICATOR_STROKE
    )
}