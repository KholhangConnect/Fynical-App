package com.kholhang.fynical.utils

/**
 * EMI (Equated Monthly Installment) Calculator
 * Formulas verified and current as per 2026 banking standards
 * Uses standard EMI formula: EMI = [P × R × (1+R)^N] / [(1+R)^N - 1]
 */
object EMICalculator {
    /**
     * Calculate EMI using the formula:
     * EMI = [P x R x (1+R)^N] / [(1+R)^N - 1]
     * where P = Principal, R = Monthly Interest Rate, N = Number of months
     */
    /**
     * Calculate EMI using banking standard formula with proper rounding
     * EMI = [P x R x (1+R)^N] / [(1+R)^N - 1]
     * Banks round EMI to nearest rupee (2 decimal places)
     */
    fun calculateEMI(principal: Double, annualRate: Double, tenureMonths: Int): Double {
        if (principal <= 0 || annualRate < 0 || tenureMonths <= 0) {
            return 0.0
        }
        
        val monthlyRate = annualRate / (12 * 100)
        if (monthlyRate == 0.0) {
            // Round to 2 decimal places for banking standards
            return Math.round((principal / tenureMonths) * 100.0) / 100.0
        }
        
        val factor = Math.pow(1 + monthlyRate, tenureMonths.toDouble())
        val emi = (principal * monthlyRate * factor) / (factor - 1)
        
        // Round to 2 decimal places (banking standard)
        return Math.round(emi * 100.0) / 100.0
    }
    
    /**
     * Calculate total amount paid (EMI * tenure)
     * Rounded to 2 decimal places
     */
    fun calculateTotalAmount(emi: Double, tenureMonths: Int): Double {
        val total = emi * tenureMonths
        return Math.round(total * 100.0) / 100.0
    }
    
    /**
     * Calculate total interest paid
     * Rounded to 2 decimal places
     */
    fun calculateTotalInterest(principal: Double, totalAmount: Double): Double {
        val interest = totalAmount - principal
        return Math.round(interest * 100.0) / 100.0
    }
}

