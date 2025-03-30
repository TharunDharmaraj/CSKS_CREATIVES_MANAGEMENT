package com.example.csks_creatives.presentation.components.sealed

sealed class DateOrder {
    object Ascending : DateOrder()
    object Descending : DateOrder()
}