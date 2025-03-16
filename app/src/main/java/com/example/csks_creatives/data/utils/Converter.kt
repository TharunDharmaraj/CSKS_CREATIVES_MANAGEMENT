package com.example.csks_creatives.data.utils

import androidx.room.TypeConverter
import com.example.csks_creatives.domain.model.utills.sealed.UserRole

class Converter {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }

    @TypeConverter
    fun fromUserRole(userRole: UserRole): String {
        return when (userRole) {
            is UserRole.Admin -> "Admin"
            is UserRole.Employee -> "Employee"
        }
    }

    @TypeConverter
    fun toUserRole(value: String): UserRole {
        return when (value) {
            "Admin" -> UserRole.Admin
            "Employee" -> UserRole.Employee
            else -> throw IllegalArgumentException("Unknown user role: $value")
        }
    }
}