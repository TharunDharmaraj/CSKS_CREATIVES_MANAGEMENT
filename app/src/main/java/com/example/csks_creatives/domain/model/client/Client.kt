package com.example.csks_creatives.domain.model.client

import androidx.annotation.Keep
import com.example.csks_creatives.data.utils.Constants.CLIENT_ID
import com.example.csks_creatives.data.utils.Constants.CLIENT_NAME
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

// client / ClientId
@Keep
@IgnoreExtraProperties
data class Client(
    @PropertyName(CLIENT_ID) val clientId: String = EMPTY_STRING,
    @PropertyName(CLIENT_NAME) val clientName: String = EMPTY_STRING,
) {
    constructor() : this("", "", emptyList())
}
