package com.example.csks_creatives.presentation.homeScreen.components

import androidx.compose.foundation.background
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.csks_creatives.presentation.components.charCoalBlack
import com.example.csks_creatives.presentation.components.tealGreen

@Composable
fun AddEmployeeButton(buttonImage: ImageVector, buttonText: String, onClick: () -> Unit) {
    IconButton(
        modifier = Modifier
            .background(tealGreen),
        onClick = { onClick() }
    ) {
        Icon(
            imageVector = buttonImage,
            contentDescription = buttonText,
            tint = charCoalBlack
        )
    }
}