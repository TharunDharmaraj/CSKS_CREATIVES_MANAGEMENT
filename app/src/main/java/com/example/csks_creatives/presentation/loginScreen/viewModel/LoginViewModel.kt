package com.example.csks_creatives.presentation.loginScreen.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csks_creatives.domain.useCase.UserLoginUseCase
import com.example.csks_creatives.presentation.loginScreen.viewModel.event.LoginEvent
import com.example.csks_creatives.presentation.loginScreen.viewModel.event.LoginUIEvent
import com.example.csks_creatives.presentation.loginScreen.viewModel.state.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: UserLoginUseCase
) : ViewModel() {
    private val _loginScreenState = MutableStateFlow(LoginState())
    val loginScreenState: StateFlow<LoginState> = _loginScreenState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<LoginUIEvent>()
    val uiEvent: SharedFlow<LoginUIEvent> = _uiEvent.asSharedFlow()

    fun onEvent(loginEvent: LoginEvent) {
        when (loginEvent) {
            is LoginEvent.OnUserNameTextFieldChanged -> {
                _loginScreenState.update { it.copy(userName = loginEvent.userName) }
            }

            is LoginEvent.OnPasswordTextFieldChanged -> {
                _loginScreenState.update { it.copy(password = loginEvent.password) }
            }

            LoginEvent.LoginButtonClick -> {
                onLoggingIn()
            }
        }
    }

    private fun onLoggingIn() {
        _loginScreenState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val loginResult = loginUseCase(
                _loginScreenState.value.userName, _loginScreenState.value.password
            )
            loginResult.onSuccess { user ->
                _loginScreenState.update {
                    it.copy(
                        isLoading = false, loginSuccess = true, userRole = user.userRole, employeeId = user.id
                    )
                }
                viewModelScope.launch {
                    _uiEvent.emit(LoginUIEvent.ShowToast("Login Successful!"))
                }
            }.onFailure { exception ->
                _loginScreenState.update {
                    it.copy(
                        isLoading = false,
                        loginSuccess = false,
                        errorMessage = "Login failed ${exception.message}"
                    )
                }
                viewModelScope.launch {
                    _uiEvent.emit(LoginUIEvent.ShowToast("Login failed: ${exception.message}"))
                }
            }
        }
    }
}