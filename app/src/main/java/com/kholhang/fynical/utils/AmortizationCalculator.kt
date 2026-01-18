package com.kholhang.fynical.utils

object AmortizationCalculator {
    /**
     * Represents a single month's payment breakdown in the amortization schedule
     */
    data class AmortizationEntry(
        val month: Int,
        val beginningBalance: Double,
        val emi: Double,
        val principalPayment: Double,
        val interestPayment: Double,
        val endingBalance: Double
    )
    
    /**
     * Complete amortization schedule result
     */
    data class AmortizationSchedule(
        val principal: Double,
        val annualRate: Double,
        val tenureMonths: Int,
        val emi: Double,
        val totalAmount: Double,
        val totalInterest: Double,
        val entries: List<AmortizationEntry>
    )
    
    /**
     * Calculate complete amortization schedule for a loan
     * @param principal Loan principal amount
     * @param annualRate Annual interest rate (as percentage)
     * @param tenureMonths Loan tenure in months
     * @return Complete amortization schedule with monthly breakdown
     */
    fun calculateSchedule(
        principal: Double,
        annualRate: Double,
        tenureMonths: Int
    ): AmortizationSchedule {
        if (principal <= 0 || annualRate < 0 || tenureMonths <= 0) {
            return AmortizationSchedule(
                principal = principal,
                annualRate = annualRate,
                tenureMonths = tenureMonths,
                emi = 0.0,
                totalAmount = 0.0,
                totalInterest = 0.0,
                entries = emptyList()
            )
        }
        
        val emi = EMICalculator.calculateEMI(principal, annualRate, tenureMonths)
        val monthlyRate = annualRate / (12 * 100)
        val schedule = mutableListOf<AmortizationEntry>()
        
        var remainingBalance = principal
        
        for (month in 1..tenureMonths) {
            val beginningBalance = remainingBalance
            val interestPayment = Math.round((beginningBalance * monthlyRate) * 100.0) / 100.0
            val principalPayment = Math.round((emi - interestPayment) * 100.0) / 100.0
            
            // For the last month, adjust to ensure balance becomes zero
            val adjustedPrincipal = if (month == tenureMonths) {
                beginningBalance
            } else {
                principalPayment
            }
            
            val adjustedEMI = if (month == tenureMonths) {
                beginningBalance + interestPayment
            } else {
                emi
            }
            
            remainingBalance = Math.round((beginningBalance - adjustedPrincipal) * 100.0) / 100.0
            if (remainingBalance < 0) remainingBalance = 0.0
            
            schedule.add(
                AmortizationEntry(
                    month = month,
                    beginningBalance = Math.round(beginningBalance * 100.0) / 100.0,
                    emi = Math.round(adjustedEMI * 100.0) / 100.0,
                    principalPayment = Math.round(adjustedPrincipal * 100.0) / 100.0,
                    interestPayment = Math.round(interestPayment * 100.0) / 100.0,
                    endingBalance = Math.round(remainingBalance * 100.0) / 100.0
                )
            )
        }
        
        // Calculate actual total amount from schedule entries (accounts for last month adjustment)
        val actualTotalAmount = schedule.sumByDouble { it.emi }
        val totalAmount = Math.round(actualTotalAmount * 100.0) / 100.0
        val totalInterest = Math.round((totalAmount - principal) * 100.0) / 100.0
        
        return AmortizationSchedule(
            principal = principal,
            annualRate = annualRate,
            tenureMonths = tenureMonths,
            emi = emi,
            totalAmount = totalAmount,
            totalInterest = totalInterest,
            entries = schedule
        )
    }
    
    /**
     * Get yearly summary from amortization schedule
     */
    data class YearlySummary(
        val year: Int,
        val totalPrincipalPaid: Double,
        val totalInterestPaid: Double,
        val totalPaid: Double,
        val remainingBalance: Double
    )
    
    fun getYearlySummary(schedule: AmortizationSchedule): List<YearlySummary> {
        val yearlySummaries = mutableListOf<YearlySummary>()
        val entries = schedule.entries
        
        var currentYear = 1
        var yearStartMonth = 1
        var yearEndMonth = 12
        
        while (yearStartMonth <= entries.size) {
            if (yearEndMonth > entries.size) {
                yearEndMonth = entries.size
            }
            
            var totalPrincipal = 0.0
            var totalInterest = 0.0
            
            for (i in (yearStartMonth - 1) until yearEndMonth) {
                if (i < entries.size) {
                    totalPrincipal += entries[i].principalPayment
                    totalInterest += entries[i].interestPayment
                }
            }
            
            val remainingBalance = if (yearEndMonth <= entries.size) {
                entries[yearEndMonth - 1].endingBalance
            } else {
                0.0
            }
            
            yearlySummaries.add(
                YearlySummary(
                    year = currentYear,
                    totalPrincipalPaid = Math.round(totalPrincipal * 100.0) / 100.0,
                    totalInterestPaid = Math.round(totalInterest * 100.0) / 100.0,
                    totalPaid = Math.round((totalPrincipal + totalInterest) * 100.0) / 100.0,
                    remainingBalance = Math.round(remainingBalance * 100.0) / 100.0
                )
            )
            
            currentYear++
            yearStartMonth += 12
            yearEndMonth += 12
        }
        
        return yearlySummaries
    }
}

