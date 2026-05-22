package com.example.csks_creatives.presentation.loginScreen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csks_creatives.R
import com.example.csks_creatives.domain.model.utills.sealed.UserRole
import com.example.csks_creatives.presentation.components.*
import com.example.csks_creatives.presentation.loginScreen.components.*
import com.example.csks_creatives.presentation.loginScreen.viewModel.LoginViewModel
import com.example.csks_creatives.presentation.loginScreen.viewModel.event.LoginEvent
import com.example.csks_creatives.presentation.loginScreen.viewModel.event.LoginUIEvent

@Composable
fun LoginScreen(viewModel: LoginViewModel = hiltViewModel(), navController: NavHostController) {
    val context = LocalContext.current
    val loginState = viewModel.loginScreenState.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is LoginUIEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(loginState.value.loginSuccess) {
        if (loginState.value.loginSuccess) {
            when (loginState.value.userRole) {
                UserRole.Employee -> {
                    val employeeId = loginState.value.employeeId
                    navController.navigate("employee_home/$employeeId") {
                        popUpTo("login") { inclusive = true }
                    }
                }

                UserRole.Admin -> navController.navigate("admin_home") {
                    popUpTo("login") { inclusive = true }
                }

                null -> {
                    // Ignore
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkSlateBlue)
            .verticalScroll(scrollState)
            .padding(24.dp)
            .imePadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo Section
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(white.copy(alpha = 0.05f), RoundedCornerShape(32.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.toolbar_logo),
                contentDescription = "CSKS CREATIVES",
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Welcome Text
        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineLarge,
            color = white,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Enter your credentials to access your account",
            style = MaterialTheme.typography.bodyMedium,
            color = silverGrey.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Login Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = charCoalPurple.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, white.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                UserNameInputTextField(
                    loginState.value.userName,
                    onEditTextFieldChanged = { viewModel.onEvent(LoginEvent.OnUserNameTextFieldChanged(it)) },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                PasswordInputTextField(
                    modifier = Modifier.fillMaxWidth(),
                    passwordTextField = loginState.value.password,
                    onPasswordTextChanged = { viewModel.onEvent(LoginEvent.OnPasswordTextFieldChanged(it)) },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(
                        onGo = { viewModel.onEvent(LoginEvent.LoginButtonClick) }
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (loginState.value.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = vividCerulean,
                        strokeWidth = 4.dp
                    )
                } else {
                    BottomLoginButton(
                        onClick = { viewModel.onEvent(LoginEvent.LoginButtonClick) }
                    )
                }
            }
        }
    }
}