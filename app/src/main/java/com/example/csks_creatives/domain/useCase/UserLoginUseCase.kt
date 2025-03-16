package com.example.csks_creatives.domain.useCase

import android.util.Log
import com.example.csks_creatives.data.database.CurrentUser
import com.example.csks_creatives.domain.model.user.User
import com.example.csks_creatives.domain.model.utills.sealed.UserRole
import com.example.csks_creatives.domain.repository.remote.LoginRepository
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserLoginUseCase @Inject constructor(
    private val loginRepository: LoginRepository,
    private val userPersistenceUseCase: UserPersistenceUseCase
) {
    private val logTag = "UserLoginUseCase"
    suspend operator fun invoke(username: String, password: String): Result<User> {
        if (username.isBlank() || password.isBlank())
            return Result.failure(Exception("Username or password cannot be empty"))
        return loginRepository.login(username, password)
    }

    suspend fun insertCurrentUserDetails(user: User) {
        try {
            val userRole = user.userRole
            val userId = user.id
            var adminId = EMPTY_STRING
            var employeeId = EMPTY_STRING
            if (userRole == UserRole.Admin) {
                adminId = userId
            } else {
                employeeId = userId
            }
            val loginTime = System.currentTimeMillis().toString()
            withContext(Dispatchers.IO) {
                val currentUser = CurrentUser(
                    userRole = userRole,
                    loginTime = loginTime,
                    adminName = adminId,
                    employeeId = employeeId
                )
                userPersistenceUseCase.insertCurrentUser(
                    currentUser
                )
            }
        } catch (exception: Exception) {
            Log.d(logTag, "Exception $exception in inserting user $user in localDB")
        }
    }

    suspend fun saveFcmToken(employeeId: String) = loginRepository.saveFCMToken(employeeId)

    suspend fun saveNewFcmToken(employeeId: String, newToken: String) =
        loginRepository.saveNewFcmToken(employeeId, newToken)
}