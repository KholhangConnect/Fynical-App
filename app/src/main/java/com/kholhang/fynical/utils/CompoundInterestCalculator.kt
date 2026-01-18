package com.kholhang.fynical.utils

/**
 * Compound Interest Calculator
 * Formulas verified and current as per 2026 standards
 * Uses standard formula: A = P(1 + r/n)^(nt)
 */
object CompoundInterestCalculator {
    enum class CompoundingFrequency(val periodsPerYear: Int) {
        YEARLY(1),
        HALF_YEARLY(2),
        QUARTERLY(4),
        MONTHLY(12)
    }
    
    data class CompoundInterestResult(
        val compoundInterest: Double,
        val totalAmount: Double,
        val principal: Double
    )
    
    fun calculate(
        principal: Double,
        rate: Double,
        timeYears: Double,
        compoundingFrequency: CompoundingFrequency = CompoundingFrequency.QUARTERLY
    ): CompoundInterestResult {
        if (principal <= 0 || rate <= 0 || timeYears <= 0) {
            return CompoundInterestResult(0.0, principal, principal)
        }
        
        val n = compoundingFrequency.periodsPerYear.toDouble()
        val r = rate / 100.0
        val t = timeYears
        
        // Compound Interest Formula: A = P(1 + r/n)^(nt)
        val totalAmount = principal * Math.pow(1 + (r / n), n * t)
        val compoundInterest = totalAmount - principal
        
        // Round to 2 decimal places (banking standard)
        return CompoundInterestResult(
            compoundInterest = Math.round(compoundInterest * 100.0) / 100.0,
            totalAmount = Math.round(totalAmount * 100.0) / 100.0,
            principal = principal
        )
    }
    
    fun calculateYearlyBreakdown(
        principal: Double,
        rate: Double,
        timeYears: Int,
        compoundingFrequency: CompoundingFrequency = CompoundingFrequency.QUARTERLY
    ): List<YearlyBreakdown> {
        val breakdown = mutableListOf<YearlyBreakdown>()
        var currentPrincipal = principal
        
        for (year in 1..timeYears) {
            val result = calculate(currentPrincipal, rate, 1.0, compoundingFrequency)
            breakdown.add(
                YearlyBreakdown(
                    year = year,
                    principalAtStart = currentPrincipal,
                    interestEarned = result.compoundInterest,
                    amountAtEnd = result.totalAmount
                )
            )
            currentPrincipal = result.totalAmount
        }
        
        return breakdown
    }
    
    data class YearlyBreakdown(
        val year: Int,
        val principalAtStart: Double,
        val interestEarned: Double,
        val amountAtEnd: Double
    )
}


