package com.example.csks_creatives.domain.repository.remote

import com.example.csks_creatives.domain.model.login.CurrentLoginUser
import com.example.csks_creatives.domain.model.user.User

interface LoginRepository {
    suspend fun login(username: String, password: String): Result<User>

    suspend fun saveFCMToken(employeeId: String)

    suspend fun saveAdminFMToken()

    suspend fun saveNewFcmToken(employeeId: String, newToken: String)

    fun getCurrentUser(): CurrentLoginUser?

    fun deleteCurrentUser()
}