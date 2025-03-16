package com.example.csks_creatives.domain.useCase

import com.example.csks_creatives.domain.model.utills.enums.UserPrivilege
import com.example.csks_creatives.domain.model.utills.sealed.UserRole

class PrivilegeProviderUserCase {
    fun getUserPrivileges(role: UserRole): UserPrivilege {
        return when (role) {
            is UserRole.Admin -> UserPrivilege.FULL_ACCESS
            is UserRole.Employee -> UserPrivilege.EDIT_TASKS
        }
    }
}