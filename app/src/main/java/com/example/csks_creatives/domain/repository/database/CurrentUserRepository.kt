package com.example.csks_creatives.domain.repository.database

import com.example.csks_creatives.data.database.CurrentUser

interface CurrentUserRepository  {
    suspend fun insertCurrentUser(currentUser: CurrentUser)

    suspend fun deleteCurrentUser()

    suspend fun getCurrentUserDetails() : CurrentUser?
}