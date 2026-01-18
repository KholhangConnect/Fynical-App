package com.kholhang.fynical.utils

/**
 * PPF (Public Provident Fund) Calculator
 * Formulas verified and current as per 2026 standards
 * Uses annual compounding: M = P × [{(1 + r)^n - 1} / r] × (1 + r)
 */
object PPFCalculator {
    data class PPFResult(
        val maturityAmount: Double,
        val totalContribution: Double,
        val interestEarned: Double
    )
    
    /**
     * Calculate PPF maturity amount
     * PPF has a fixed interest rate and compounds annually
     * Formula: M = P × [{(1 + r)^n - 1} / r] × (1 + r)
     * where:
     * M = Maturity amount
     * P = Annual contribution
     * r = Annual interest rate
     * n = Number of years (minimum 15 years)
     */
    fun calculate(
        annualContribution: Double,
        interestRate: Double,
        years: Int
    ): PPFResult {
        if (annualContribution <= 0 || interestRate <= 0 || years < 15) {
            return PPFResult(0.0, 0.0, 0.0)
        }
        
        val totalContribution = annualContribution * years
        val r = interestRate / 100.0
        val n = years.toDouble()
        
        // PPF Formula: M = P × [{(1 + r)^n - 1} / r] × (1 + r)
        val maturityAmount = if (r > 0) {
            annualContribution * ((Math.pow(1 + r, n) - 1) / r) * (1 + r)
        } else {
            totalContribution
        }
        
        val interestEarned = maturityAmount - totalContribution
        
        // Round to 2 decimal places (banking standard)
        return PPFResult(
            maturityAmount = Math.round(maturityAmount * 100.0) / 100.0,
            totalContribution = Math.round(totalContribution * 100.0) / 100.0,
            interestEarned = Math.round(interestEarned * 100.0) / 100.0
        )
    }
    
    data class YearlyBreakdown(
        val year: Int,
        val contribution: Double,
        val interestEarned: Double,
        val amountAtEnd: Double
    )
    
    fun calculateYearlyBreakdown(
        annualContribution: Double,
        interestRate: Double,
        years: Int
    ): List<YearlyBreakdown> {
        val breakdown = mutableListOf<YearlyBreakdown>()
        var currentAmount = 0.0
        val r = interestRate / 100.0
        
        for (year in 1..years) {
            // Add annual contribution at the start of the year
            currentAmount += annualContribution
            
            // Calculate interest for the year
            val interestEarned = currentAmount * r
            currentAmount += interestEarned
            
            breakdown.add(
                YearlyBreakdown(
                    year = year,
                    contribution = annualContribution,
                    interestEarned = Math.round(interestEarned * 100.0) / 100.0,
                    amountAtEnd = Math.round(currentAmount * 100.0) / 100.0
                )
            )
        }
        
        return breakdown
    }
}
