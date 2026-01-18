package com.kholhang.fynical.utils

object IncomeTaxCalculator {
    data class TaxResult(
        val taxPayable: Double,
        val effectiveTaxRate: Double,
        val netIncome: Double,
        val taxSlab: String
    )
    
    /**
     * Calculate income tax for Indian tax slabs (2024-25)
     * Old Tax Regime:
     * - Up to ₹2.5 Lakh: Nil
     * - ₹2.5 Lakh to ₹5 Lakh: 5%
     * - ₹5 Lakh to ₹10 Lakh: 20%
     * - Above ₹10 Lakh: 30%
     * 
     * New Tax Regime (Default):
     * - Up to ₹3 Lakh: Nil
     * - ₹3 Lakh to ₹7 Lakh: 5%
     * - ₹7 Lakh to ₹10 Lakh: 10%
     * - ₹10 Lakh to ₹12 Lakh: 15%
     * - ₹12 Lakh to ₹15 Lakh: 20%
     * - Above ₹15 Lakh: 30%
     */
    fun calculate(
        annualIncome: Double,
        useNewTaxRegime: Boolean = true
    ): TaxResult {
        if (annualIncome <= 0) {
            return TaxResult(0.0, 0.0, annualIncome, "Nil")
        }
        
        val income = annualIncome
        var tax = 0.0
        val slab: String
        
        if (useNewTaxRegime) {
            // New Tax Regime (2024-25)
            when {
                income <= 300000 -> {
                    tax = 0.0
                    slab = "Up to ₹3 Lakh (Nil)"
                }
                income <= 700000 -> {
                    tax = (income - 300000) * 0.05
                    slab = "₹3 Lakh - ₹7 Lakh (5%)"
                }
                income <= 1000000 -> {
                    tax = 20000 + (income - 700000) * 0.10
                    slab = "₹7 Lakh - ₹10 Lakh (10%)"
                }
                income <= 1200000 -> {
                    tax = 50000 + (income - 1000000) * 0.15
                    slab = "₹10 Lakh - ₹12 Lakh (15%)"
                }
                income <= 1500000 -> {
                    tax = 80000 + (income - 1200000) * 0.20
                    slab = "₹12 Lakh - ₹15 Lakh (20%)"
                }
                else -> {
                    tax = 140000 + (income - 1500000) * 0.30
                    slab = "Above ₹15 Lakh (30%)"
                }
            }
        } else {
            // Old Tax Regime
            when {
                income <= 250000 -> {
                    tax = 0.0
                    slab = "Up to ₹2.5 Lakh (Nil)"
                }
                income <= 500000 -> {
                    tax = (income - 250000) * 0.05
                    slab = "₹2.5 Lakh - ₹5 Lakh (5%)"
                }
                income <= 1000000 -> {
                    tax = 12500 + (income - 500000) * 0.20
                    slab = "₹5 Lakh - ₹10 Lakh (20%)"
                }
                else -> {
                    tax = 112500 + (income - 1000000) * 0.30
                    slab = "Above ₹10 Lakh (30%)"
                }
            }
        }
        
        // Add 4% Health and Education Cess
        val cess = tax * 0.04
        val totalTax = tax + cess
        
        val effectiveTaxRate = if (annualIncome > 0) {
            (totalTax / annualIncome) * 100.0
        } else {
            0.0
        }
        
        val netIncome = annualIncome - totalTax
        
        return TaxResult(
            taxPayable = totalTax,
            effectiveTaxRate = effectiveTaxRate,
            netIncome = netIncome,
            taxSlab = slab
        )
    }
}
