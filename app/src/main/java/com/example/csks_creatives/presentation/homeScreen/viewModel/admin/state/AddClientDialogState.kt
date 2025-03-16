package com.example.csks_creatives.presentation.homeScreen.viewModel.admin.state

import com.example.csks_creatives.domain.utils.Utils.EMPTY_STRING
import java.util.UUID

data class AddClientDialogState(
    val clientId: String = UUID.randomUUID().toString(),
    val clientName: String = EMPTY_STRING
)
