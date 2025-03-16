package com.example.csks_creatives.domain.model.utills.sealed

sealed class UserRole {
    object Admin : UserRole()
    object Employee : UserRole()
}