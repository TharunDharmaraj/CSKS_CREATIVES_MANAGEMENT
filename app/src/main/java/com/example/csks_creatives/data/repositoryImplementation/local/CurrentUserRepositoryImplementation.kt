package com.example.csks_creatives.data.repositoryImplementation.local

import com.example.csks_creatives.data.database.CurrentUser
import com.example.csks_creatives.data.database.CurrentUserDao
import com.example.csks_creatives.domain.repository.database.CurrentUserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentUserRepositoryImplementation @Inject constructor(
    private val currentUserDao: CurrentUserDao
) : CurrentUserRepository {
    override suspend fun insertCurrentUser(currentUser: CurrentUser) {
        currentUserDao.insertUser(currentUser)
    }

    override suspend fun deleteCurrentUser() {
        currentUserDao.deleteCurrentUser()
    }

    override suspend fun getCurrentUserDetails() = currentUserDao.getCurrentUser()
}