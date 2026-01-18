package com.kholhang.fynical.utils

/**
 * Lumpsum Investment Calculator
 * Formulas verified and current as per 2026 standards
 * Uses standard formula: FV = PV × (1 + r)^n
 */
object LumpsumCalculator {
    data class LumpsumResult(
        val futureValue: Double,
        val investmentAmount: Double,
        val returns: Double,
        val absoluteReturn: Double
    )
    
    /**
     * Calculate future value of lumpsum investment
     * Formula: FV = PV × (1 + r)^n
     * where:
     * FV = Future Value
     * PV = Present Value (Investment Amount)
     * r = Annual interest rate
     * n = Number of years
     */
    fun calculate(
        investmentAmount: Double,
        expectedReturnRate: Double,
        investmentPeriodYears: Double
    ): LumpsumResult {
        if (investmentAmount <= 0 || expectedReturnRate <= 0 || investmentPeriodYears <= 0) {
            return LumpsumResult(0.0, investmentAmount, 0.0, 0.0)
        }
        
        val r = expectedReturnRate / 100.0
        val n = investmentPeriodYears
        
        // Future Value Formula: FV = PV × (1 + r)^n
        val futureValue = investmentAmount * Math.pow(1 + r, n)
        val returns = futureValue - investmentAmount
        val absoluteReturn = if (investmentAmount > 0) {
            (returns / investmentAmount) * 100.0
        } else {
            0.0
        }
        
        // Round to 2 decimal places (banking standard)
        return LumpsumResult(
            futureValue = Math.round(futureValue * 100.0) / 100.0,
            investmentAmount = investmentAmount,
            returns = Math.round(returns * 100.0) / 100.0,
            absoluteReturn = Math.round(absoluteReturn * 100.0) / 100.0
        )
    }
    
    data class YearlyBreakdown(
        val year: Int,
        val amountAtStart: Double,
        val interestEarned: Double,
        val amountAtEnd: Double
    )
    
    fun calculateYearlyBreakdown(
        investmentAmount: Double,
        expectedReturnRate: Double,
        investmentPeriodYears: Int
    ): List<YearlyBreakdown> {
        val breakdown = mutableListOf<YearlyBreakdown>()
        var currentAmount = investmentAmount
        val r = expectedReturnRate / 100.0
        
        for (year in 1..investmentPeriodYears) {
            val amountAtStart = currentAmount
            val interestEarned = currentAmount * r
            currentAmount += interestEarned
            
            breakdown.add(
                YearlyBreakdown(
                    year = year,
                    amountAtStart = Math.round(amountAtStart * 100.0) / 100.0,
                    interestEarned = Math.round(interestEarned * 100.0) / 100.0,
                    amountAtEnd = Math.round(currentAmount * 100.0) / 100.0
                )
            )
        }
        
        return breakdown
    }
}
