package com.example.csks_creatives.domain.model.utills.enums.finance

enum class FinanceMonths(val monthId: Int) {
    JANUARY(1),
    FEBRUARY(2),
    MARCH(3),
    APRIL(4),
    MAY(5),
    JUNE(6),
    JULY(7),
    AUGUST(8),
    SEPTEMBER(9),
    OCTOBER(10),
    NOVEMBER(11),
    DECEMBER(12);

    companion object {
        fun fromInt(month: Int): FinanceMonths {
            return entries.find { it.monthId == month }
                ?: throw IllegalArgumentException("Invalid month number: $month")
        }
    }
}