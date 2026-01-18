package com.kholhang.fynical.utils

/**
 * SIP (Systematic Investment Plan) Calculator
 * Formulas verified and current as per 2026 standards
 * Uses standard SIP formula: M = P × [{(1 + i)^n - 1} / i] × (1 + i)
 */
object SIPCalculator {
    data class SIPResult(
        val maturityAmount: Double,
        val totalInvestment: Double,
        val returns: Double
    )
    
    /**
     * Calculate SIP maturity amount
     * Formula: M = P × [{(1 + i)^n - 1} / i] × (1 + i)
     * where:
     * M = Maturity amount
     * P = Monthly investment
     * i = Monthly interest rate (annual rate / 12 / 100)
     * n = Number of months
     */
    fun calculateSIPMaturity(
        monthlyInvestment: Double,
        annualRate: Double,
        tenureMonths: Int
    ): SIPResult {
        if (monthlyInvestment <= 0 || annualRate <= 0 || tenureMonths <= 0) {
            return SIPResult(0.0, 0.0, 0.0)
        }
        
        val monthlyRate = annualRate / 12 / 100
        val totalMonths = tenureMonths.toDouble()
        
        // Calculate maturity amount
        val maturityAmount = if (monthlyRate > 0) {
            monthlyInvestment * (((Math.pow(1 + monthlyRate, totalMonths) - 1) / monthlyRate) * (1 + monthlyRate))
        } else {
            monthlyInvestment * totalMonths
        }
        
        val totalInvestment = monthlyInvestment * totalMonths
        val returns = maturityAmount - totalInvestment
        
        // Round to 2 decimal places (banking standard)
        return SIPResult(
            maturityAmount = Math.round(maturityAmount * 100.0) / 100.0,
            totalInvestment = Math.round(totalInvestment * 100.0) / 100.0,
            returns = Math.round(returns * 100.0) / 100.0
        )
    }
    
    /**
     * Calculate required monthly SIP to reach a target amount
     */
    fun calculateRequiredSIP(
        targetAmount: Double,
        annualRate: Double,
        tenureMonths: Int
    ): Double {
        if (targetAmount <= 0 || annualRate <= 0 || tenureMonths <= 0) {
            return 0.0
        }
        
        val monthlyRate = annualRate / 12 / 100
        val totalMonths = tenureMonths.toDouble()
        
        if (monthlyRate > 0) {
            val denominator = ((Math.pow(1 + monthlyRate, totalMonths) - 1) / monthlyRate) * (1 + monthlyRate)
            val requiredSIP = targetAmount / denominator
            // Round to 2 decimal places
            return Math.round(requiredSIP * 100.0) / 100.0
        } else {
            val requiredSIP = targetAmount / totalMonths
            return Math.round(requiredSIP * 100.0) / 100.0
        }
    }
    
    data class YearlyBreakdown(
        val year: Int,
        val investment: Double,
        val interestEarned: Double,
        val amountAtEnd: Double
    )
    
    fun calculateYearlyBreakdown(
        monthlyInvestment: Double,
        annualRate: Double,
        tenureMonths: Int
    ): List<YearlyBreakdown> {
        val breakdown = mutableListOf<YearlyBreakdown>()
        val monthlyRate = annualRate / 12 / 100
        var cumulativeAmount = 0.0
        
        var month = 1
        var year = 1
        
        while (month <= tenureMonths) {
            val monthsInYear = minOf(12, tenureMonths - month + 1)
            var yearInvestment = 0.0
            var yearStartAmount = cumulativeAmount
            
            // Calculate investment and growth for this year
            for (m in month until (month + monthsInYear)) {
                if (m > tenureMonths) break
                yearInvestment += monthlyInvestment
                
                // Add monthly investment
                cumulativeAmount += monthlyInvestment
                
                // Apply monthly interest
                if (monthlyRate > 0) {
                    cumulativeAmount *= (1 + monthlyRate)
                }
            }
            
            val yearEndAmount = cumulativeAmount
            val interestEarned = yearEndAmount - yearStartAmount - yearInvestment
            
            breakdown.add(
                YearlyBreakdown(
                    year = year,
                    investment = Math.round(yearInvestment * 100.0) / 100.0,
                    interestEarned = Math.round(interestEarned * 100.0) / 100.0,
                    amountAtEnd = Math.round(yearEndAmount * 100.0) / 100.0
                )
            )
            
            month += monthsInYear
            year++
        }
        
        return breakdown
    }
}
