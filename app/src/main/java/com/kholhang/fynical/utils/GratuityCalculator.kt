package com.kholhang.fynical.utils

object GratuityCalculator {
    data class GratuityResult(
        val gratuityAmount: Double,
        val formula: String
    )
    
    /**
     * Calculate gratuity amount
     * Formula: Gratuity = (Last drawn salary × 15 × Years of service) / 26
     * For employees covered under Gratuity Act
     * Maximum gratuity limit: ₹20 Lakh
     */
    fun calculate(
        lastDrawnSalary: Double,
        yearsOfService: Double
    ): GratuityResult {
        if (lastDrawnSalary <= 0 || yearsOfService <= 0) {
            return GratuityResult(0.0, "Invalid input")
        }
        
        // Gratuity Formula: (Last drawn salary × 15 × Years of service) / 26
        val gratuity = (lastDrawnSalary * 15.0 * yearsOfService) / 26.0
        
        // Maximum gratuity limit is ₹20 Lakh
        val finalGratuity = minOf(gratuity, 2000000.0)
        
        val formula = if (finalGratuity >= 2000000.0) {
            "Gratuity = (₹${String.format("%.2f", lastDrawnSalary)} × 15 × ${String.format("%.1f", yearsOfService)}) / 26 = ₹${String.format("%.2f", gratuity)} (Capped at ₹20 Lakh)"
        } else {
            "Gratuity = (₹${String.format("%.2f", lastDrawnSalary)} × 15 × ${String.format("%.1f", yearsOfService)}) / 26"
        }
        
        return GratuityResult(
            gratuityAmount = finalGratuity,
            formula = formula
        )
    }
}

