package com.example.csks_creatives.application.service

import com.example.csks_creatives.domain.useCase.UserLoginUseCase
import com.example.csks_creatives.domain.useCase.UserPersistenceUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MessagingServiceEntryPoint {
    fun loginUseCase(): UserLoginUseCase
    fun userPersistenceUseCase(): UserPersistenceUseCase
}
