package com.example.csks_creatives.presentation.loginScreen.components


import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults.outlinedTextFieldColors
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.csks_creatives.presentation.components.Constants.LOGIN_SCREEN_FIELDS_SIZE
import com.example.csks_creatives.presentation.components.Constants.USERNAME_TEXT
import com.example.csks_creatives.presentation.components.tealGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserNameInputTextField(
    editTextField: String,
    onEditTextFieldChanged: (String) -> Unit,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions
) {
    OutlinedTextField(
        modifier = Modifier.width(LOGIN_SCREEN_FIELDS_SIZE),
        value = editTextField,
        onValueChange = { onEditTextFieldChanged(it) },
        singleLine = true,
        colors = outlinedTextFieldColors(
            unfocusedTextColor = tealGreen,
            focusedTextColor = tealGreen,
            focusedBorderColor = tealGreen,
            unfocusedBorderColor = tealGreen,
            focusedLabelColor = tealGreen,
            cursorColor = tealGreen
        ),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        label = { Text(text = USERNAME_TEXT, color = tealGreen) }
    )
}