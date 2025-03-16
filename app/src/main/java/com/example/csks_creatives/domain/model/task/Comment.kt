package com.example.csks_creatives.domain.model.task

import androidx.annotation.Keep
import com.example.csks_creatives.data.utils.Constants.COMMENT_COMMENTED_BY
import com.example.csks_creatives.data.utils.Constants.COMMENT_ID
import com.example.csks_creatives.data.utils.Constants.COMMENT_STRING
import com.example.csks_creatives.data.utils.Constants.COMMENT_TIME_STAMP
import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

// Comments are stored in firestore by the structure
// Tasks / taskId / comments
@Keep
@IgnoreExtraProperties
data class Comment(
    @PropertyName(COMMENT_ID) val commentId: String = EMPTY_STRING, // Auto Generated UUID
    @PropertyName(COMMENT_TIME_STAMP) val commentTimeStamp: String = EMPTY_STRING,
    @PropertyName(COMMENT_COMMENTED_BY) val commentedBy: String, // EmployeeId - For AdminID = 999
    @PropertyName(COMMENT_STRING) val commentString: String = EMPTY_STRING
) {
    constructor() : this("", "", "", "")
}
