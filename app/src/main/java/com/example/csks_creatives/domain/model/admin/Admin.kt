package com.example.csks_creatives.domain.model.admin

import androidx.annotation.Keep
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING
import com.google.firebase.firestore.IgnoreExtraProperties

// TODO - Assign list of sub ordinates to a Admin / Employee
@Keep
@IgnoreExtraProperties
data class Admin(
    val userName: String = EMPTY_STRING,
    val password: String = EMPTY_STRING
)
