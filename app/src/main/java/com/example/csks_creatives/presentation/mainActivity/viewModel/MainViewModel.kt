package com.example.csks_creatives.presentation.mainActivity.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csks_creatives.domain.useCase.UserPersistenceUseCase
import com.example.csks_creatives.domain.utils.LogoutEvent.logoutEventFlow
import com.example.csks_creatives.presentation.mainActivity.viewModel.state.MainActivityState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPersistenceUseCase: UserPersistenceUseCase
) : ViewModel() {

    private val _mainState = MutableStateFlow(MainActivityState())
    val mainState = _mainState.asStateFlow()

    init {
        getCurrentUser()
        listenForLogoutEventFlow()
    }

    private fun listenForLogoutEventFlow() {
        viewModelScope.launch {
            logoutEventFlow.collect { isUserLoggedOut ->
                if (isUserLoggedOut) {
                    userPersistenceUseCase.deleteCurrentUser()
                }
            }
        }
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            // First we check from repo-stored cache, if it is found, use that to login, if not, use suspend call, from RoomDB.
            // This is done to eliminate 1 second of delay in fetching from Room Db when app is opened after closing it via tapping home button
            val currentUserFromLocalCache = userPersistenceUseCase.getCurrentUserFromLocalCache()
            if (currentUserFromLocalCache != null) {
                _mainState.update {
                    it.copy(
                        userRole = currentUserFromLocalCache.userRole,
                        employeeId = currentUserFromLocalCache.employeeId,
                        adminName = currentUserFromLocalCache.adminName
                    )
                }
            } else {
                val currentUser = userPersistenceUseCase.getCurrentUser()
                if (currentUser != null) {
                    _mainState.update {
                        it.copy(
                            userRole = currentUser.userRole,
                            employeeId = currentUser.employeeId,
                            adminName = currentUser.adminName
                        )
                    }
                }
            }
        }
    }
}