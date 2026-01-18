package com.kholhang.fynical.utils

/**
 * Simple Interest Calculator
 * Formulas verified and current as per 2026 standards
 * Uses standard formula: SI = (P × R × T) / 100
 */
object SimpleInterestCalculator {
    enum class TimeUnit {
        YEARS, MONTHS, DAYS
    }
    
    data class SimpleInterestResult(
        val interestAmount: Double,
        val totalAmount: Double,
        val principal: Double
    )
    
    fun calculate(
        principal: Double,
        rate: Double,
        time: Double,
        timeUnit: TimeUnit = TimeUnit.YEARS
    ): SimpleInterestResult {
        if (principal <= 0 || rate <= 0 || time <= 0) {
            return SimpleInterestResult(0.0, principal, principal)
        }
        
        // Convert time to years
        val timeInYears = when (timeUnit) {
            TimeUnit.YEARS -> time
            TimeUnit.MONTHS -> time / 12.0
            TimeUnit.DAYS -> time / 365.0
        }
        
        // Simple Interest Formula: SI = (P × R × T) / 100
        val interestAmount = (principal * rate * timeInYears) / 100.0
        val totalAmount = principal + interestAmount
        
        // Round to 2 decimal places (banking standard)
        return SimpleInterestResult(
            interestAmount = Math.round(interestAmount * 100.0) / 100.0,
            totalAmount = Math.round(totalAmount * 100.0) / 100.0,
            principal = principal
        )
    }
    
    data class YearlyBreakdown(
        val year: Int,
        val principalAtStart: Double,
        val interestEarned: Double,
        val amountAtEnd: Double
    )
    
    fun calculateYearlyBreakdown(
        principal: Double,
        rate: Double,
        timeYears: Int,
        timeUnit: TimeUnit = TimeUnit.YEARS
    ): List<YearlyBreakdown> {
        val breakdown = mutableListOf<YearlyBreakdown>()
        var currentPrincipal = principal
        
        // Convert to years if needed
        val actualYears = when (timeUnit) {
            TimeUnit.YEARS -> timeYears
            TimeUnit.MONTHS -> (timeYears / 12.0).toInt()
            TimeUnit.DAYS -> (timeYears / 365.0).toInt()
        }
        
        for (year in 1..actualYears) {
            val result = calculate(currentPrincipal, rate, 1.0, TimeUnit.YEARS)
            breakdown.add(
                YearlyBreakdown(
                    year = year,
                    principalAtStart = Math.round(currentPrincipal * 100.0) / 100.0,
                    interestEarned = result.interestAmount,
                    amountAtEnd = result.totalAmount
                )
            )
            currentPrincipal = result.totalAmount
        }
        
        return breakdown
    }
}


