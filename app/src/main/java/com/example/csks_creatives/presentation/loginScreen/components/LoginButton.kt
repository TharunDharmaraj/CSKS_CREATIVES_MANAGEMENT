package com.example.csks_creatives.presentation.loginScreen.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.text.font.FontWeight
import com.example.csks_creatives.presentation.components.Constants.LOGIN_BUTTON_BORDER_STROKE
import com.example.csks_creatives.presentation.components.Constants.LOGIN_BUTTON_TEXT
import com.example.csks_creatives.presentation.components.Constants.LOGIN_SCREEN_FIELDS_SIZE
import com.example.csks_creatives.presentation.components.tealGreen

@Composable
fun BottomLoginButton(onClick: () -> Unit) {
    Button(
        modifier = Modifier.width(LOGIN_SCREEN_FIELDS_SIZE),
        border = BorderStroke(LOGIN_BUTTON_BORDER_STROKE, tealGreen),
        colors = ButtonColors(
            contentColor = tealGreen,
            containerColor = Transparent,
            disabledContainerColor = tealGreen,
            disabledContentColor = tealGreen,
        ),
        onClick = {
            onClick()
        }
    ) {
        Text(LOGIN_BUTTON_TEXT, fontWeight = FontWeight.Bold)
    }
}