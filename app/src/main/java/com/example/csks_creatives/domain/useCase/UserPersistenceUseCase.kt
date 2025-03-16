package com.example.csks_creatives.domain.useCase

import com.example.csks_creatives.data.database.CurrentUser
import com.example.csks_creatives.domain.repository.database.CurrentUserRepository
import javax.inject.Inject

class UserPersistenceUseCase @Inject constructor(
    private val currentUserRepository: CurrentUserRepository
) {
    suspend fun insertCurrentUser(currentUser: CurrentUser){
        currentUserRepository.insertCurrentUser(currentUser)
    }

    suspend fun deleteCurrentUser(){
        currentUserRepository.deleteCurrentUser()
    }

    suspend fun getCurrentUser() = currentUserRepository.getCurrentUserDetails()
}