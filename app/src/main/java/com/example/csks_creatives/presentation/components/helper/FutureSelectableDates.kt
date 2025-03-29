package com.example.csks_creatives.presentation.components.helper

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates

@OptIn(ExperimentalMaterial3Api::class)
object FutureSelectableDates: SelectableDates {
    @ExperimentalMaterial3Api
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        return utcTimeMillis >= System.currentTimeMillis()
    }
}