package com.kholhang.fynical.utils

/**
 * Fixed Deposit Calculator
 * Formulas verified and current as per 2026 banking standards
 * Uses standard compound interest formula: A = P(1 + r/n)^(nt)
 */
object FDCalculator {
    enum class CompoundingFrequency(val periodsPerYear: Int) {
        YEARLY(1),
        HALF_YEARLY(2),
        QUARTERLY(4),
        MONTHLY(12)
    }
    
    data class FDResult(
        val maturityAmount: Double,
        val interestEarned: Double,
        val principal: Double
    )
    
    fun calculate(
        principal: Double,
        interestRate: Double,
        tenureYears: Double,
        compoundingFrequency: CompoundingFrequency = CompoundingFrequency.QUARTERLY
    ): FDResult {
        if (principal <= 0 || interestRate <= 0 || tenureYears <= 0) {
            return FDResult(0.0, 0.0, principal)
        }
        
        val n = compoundingFrequency.periodsPerYear.toDouble()
        val r = interestRate / 100.0
        val t = tenureYears
        
        // Compound Interest Formula: A = P(1 + r/n)^(nt)
        val maturityAmount = principal * Math.pow(1 + (r / n), n * t)
        val interestEarned = maturityAmount - principal
        
        // Round to 2 decimal places (banking standard)
        return FDResult(
            maturityAmount = Math.round(maturityAmount * 100.0) / 100.0,
            interestEarned = Math.round(interestEarned * 100.0) / 100.0,
            principal = principal
        )
    }
    
    fun calculateYearlyBreakdown(
        principal: Double,
        interestRate: Double,
        tenureYears: Int,
        compoundingFrequency: CompoundingFrequency = CompoundingFrequency.QUARTERLY
    ): List<YearlyBreakdown> {
        val breakdown = mutableListOf<YearlyBreakdown>()
        var currentPrincipal = principal
        
        for (year in 1..tenureYears) {
            val result = calculate(currentPrincipal, interestRate, 1.0, compoundingFrequency)
            breakdown.add(
                YearlyBreakdown(
                    year = year,
                    principalAtStart = currentPrincipal,
                    interestEarned = result.interestEarned,
                    amountAtEnd = result.maturityAmount
                )
            )
            currentPrincipal = result.maturityAmount
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


