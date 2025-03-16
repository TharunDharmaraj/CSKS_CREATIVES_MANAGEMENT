package com.example.csks_creatives.domain.useCase

import com.example.csks_creatives.domain.model.user.User
import com.example.csks_creatives.domain.repository.remote.LoginRepository
import javax.inject.Inject

class UserLoginUseCase @Inject constructor(
    private val loginRepository: LoginRepository
) {
    suspend operator fun invoke(username: String, password: String): Result<User> {
        if (username.isBlank() || password.isBlank())
            return Result.failure(Exception("Username or password cannot be empty"))
        return loginRepository.login(username, password)
    }
}