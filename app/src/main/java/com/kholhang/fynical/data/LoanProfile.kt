package com.kholhang.fynical.data

data class LoanProfile(
    val id: String,
    val bankName: String,
    val principal: Double,
    val interestRate: Double,
    val tenureMonths: Int,
    val startDate: String,
    val loanType: String,
    val emi: Double,
    val totalAmount: Double,
    val totalInterest: Double
)


