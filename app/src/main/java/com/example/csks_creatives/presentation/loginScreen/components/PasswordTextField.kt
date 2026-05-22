package com.example.csks_creatives.presentation.loginScreen.components


import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.csks_creatives.presentation.components.Constants.PASSWORD_TEXT
import com.example.csks_creatives.presentation.components.*

@Composable
fun PasswordInputTextField(
    passwordTextField: String,
    onPasswordTextChanged: (String) -> Unit,
    modifier: Modifier,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        modifier = modifier.padding(vertical = 8.dp),
        value = passwordTextField,
        onValueChange = { onPasswordTextChanged(it) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = vividCerulean
            )
        },
        trailingIcon = {
            val image = if (passwordVisible)
                Icons.Rounded.Visibility
            else Icons.Rounded.VisibilityOff

            val description = if (passwordVisible) "Hide password" else "Show password"

            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = image, contentDescription = description, tint = silverGrey.copy(alpha = 0.6f))
            }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
        label = { Text(PASSWORD_TEXT) }
    )
}