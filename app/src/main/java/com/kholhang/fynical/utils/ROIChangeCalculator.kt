package com.kholhang.fynical.utils

import com.kholhang.fynical.utils.EMICalculator

object ROIChangeCalculator {
    data class ROIChangeResult(
        val newEMI: Double,
        val emiDifference: Double,
        val totalInterestDifference: Double,
        val newTotalAmount: Double,
        val originalTotalAmount: Double
    )
    
    fun calculate(
        principal: Double,
        originalRate: Double,
        newRate: Double,
        tenureMonths: Int
    ): ROIChangeResult {
        if (principal <= 0 || tenureMonths <= 0) {
            return ROIChangeResult(0.0, 0.0, 0.0, 0.0, 0.0)
        }
        
        val originalEMI = EMICalculator.calculateEMI(principal, originalRate, tenureMonths)
        val newEMI = EMICalculator.calculateEMI(principal, newRate, tenureMonths)
        
        val originalTotal = originalEMI * tenureMonths
        val newTotal = newEMI * tenureMonths
        
        val emiDifference = newEMI - originalEMI
        val totalInterestDifference = (newTotal - principal) - (originalTotal - principal)
        
        return ROIChangeResult(
            newEMI = newEMI,
            emiDifference = emiDifference,
            totalInterestDifference = totalInterestDifference,
            newTotalAmount = newTotal,
            originalTotalAmount = originalTotal
        )
    }
}


