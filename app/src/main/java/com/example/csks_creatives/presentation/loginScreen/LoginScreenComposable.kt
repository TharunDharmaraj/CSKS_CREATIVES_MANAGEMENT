package com.example.csks_creatives.presentation.loginScreen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.csks_creatives.R
import com.example.csks_creatives.domain.model.utills.sealed.UserRole
import com.example.csks_creatives.presentation.components.Constants.LOGIN_SCREEN_FIELDS_SIZE
import com.example.csks_creatives.presentation.components.Constants.LOGIN_SCREEN_PADDING
import com.example.csks_creatives.presentation.components.ui.LoadingProgress
import com.example.csks_creatives.presentation.components.charCoal
import com.example.csks_creatives.presentation.loginScreen.components.BottomLoginButton
import com.example.csks_creatives.presentation.loginScreen.components.PasswordInputTextField
import com.example.csks_creatives.presentation.loginScreen.components.UserNameInputTextField
import com.example.csks_creatives.presentation.loginScreen.viewModel.LoginViewModel
import com.example.csks_creatives.presentation.loginScreen.viewModel.event.LoginEvent
import com.example.csks_creatives.presentation.loginScreen.viewModel.event.LoginUIEvent

@Composable
fun LoginScreen(viewModel: LoginViewModel = hiltViewModel(), navController: NavHostController) {
    val context = LocalContext.current
    val loginState = viewModel.loginScreenState.collectAsState()
    val focusManager = LocalFocusManager.current

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
            .background(charCoal)
            .padding(LOGIN_SCREEN_PADDING),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier
                .width(LOGIN_SCREEN_FIELDS_SIZE)
                .height(100.dp),
            painter = painterResource(R.drawable.logo),
            contentDescription = "CSKS CREATIVES"
        )
        UserNameInputTextField(
            loginState.value.userName,
            onEditTextFieldChanged = { viewModel.onEvent(LoginEvent.OnUserNameTextFieldChanged(it)) },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        Spacer(modifier = Modifier.height(30.dp))
        PasswordInputTextField(
            modifier = Modifier.width(LOGIN_SCREEN_FIELDS_SIZE),
            passwordTextField = loginState.value.password,
            onPasswordTextChanged = { viewModel.onEvent(LoginEvent.OnPasswordTextFieldChanged(it)) },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(
                onGo = { viewModel.onEvent(LoginEvent.LoginButtonClick) }
            )
        )
        Spacer(modifier = Modifier.height(45.dp))
        if (loginState.value.isLoading) {
            LoadingProgress()
        } else {
            BottomLoginButton(
                onClick = {
                    viewModel.onEvent(LoginEvent.LoginButtonClick)
                }
            )
        }
    }
}