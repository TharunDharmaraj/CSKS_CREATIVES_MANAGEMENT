package com.example.csks_creatives.presentation.loginScreen.components


import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.csks_creatives.presentation.components.Constants.LOGIN_SCREEN_FIELDS_SIZE
import com.example.csks_creatives.presentation.components.Constants.USERNAME_TEXT
import com.example.csks_creatives.presentation.components.*

@Composable
fun UserNameInputTextField(
    editTextField: String,
    onEditTextFieldChanged: (String) -> Unit,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions
) {
    OutlinedTextField(
        modifier = Modifier
            .width(LOGIN_SCREEN_FIELDS_SIZE)
            .padding(vertical = 8.dp),
        value = editTextField,
        onValueChange = { onEditTextFieldChanged(it) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = vividCerulean
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = white,
            unfocusedTextColor = white,
            focusedBorderColor = vividCerulean,
            unfocusedBorderColor = silverGrey.copy(alpha = 0.3f),
            focusedLabelColor = vividCerulean,
            unfocusedLabelColor = silverGrey,
            cursorColor = vividCerulean
        ),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        label = { Text(text = USERNAME_TEXT) }
    )
}