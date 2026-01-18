package com.kholhang.fynical.utils

import java.text.NumberFormat
import java.util.*

object CurrencyFormatter {
    fun format(value: Double): String {
        // Keep .01 for 0.01, remove .00 for whole numbers
        return if (value == 0.01) {
            val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
            formatter.minimumFractionDigits = 2
            formatter.maximumFractionDigits = 2
            formatter.format(value)
        } else if (value % 1.0 == 0.0) {
            // Whole number - no decimals
            val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
            formatter.minimumFractionDigits = 0
            formatter.maximumFractionDigits = 0
            formatter.format(value)
        } else {
            // Has decimals - show them
            val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
            formatter.minimumFractionDigits = 2
            formatter.maximumFractionDigits = 2
            formatter.format(value)
        }
    }
}

