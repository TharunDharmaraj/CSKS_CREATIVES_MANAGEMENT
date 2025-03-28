package com.example.csks_creatives.presentation.loginScreen.components


import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults.outlinedTextFieldColors
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.csks_creatives.presentation.components.Constants.PASSWORD_TEXT
import com.example.csks_creatives.presentation.components.tealGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordInputTextField(
    passwordTextField: String,
    onPasswordTextChanged: (String) -> Unit,
    modifier: Modifier,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions
) {
    OutlinedTextField(
        modifier = modifier,
        value = passwordTextField,
        onValueChange = { onPasswordTextChanged(it) },
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
        label = { Text(PASSWORD_TEXT, color = tealGreen) }
    )
}