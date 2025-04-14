package com.example.csks_creatives.domain.model.task

import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING

data class PaymentInfo(
    val amount : Int = 0,
    val paymentDate: String = EMPTY_STRING
)
