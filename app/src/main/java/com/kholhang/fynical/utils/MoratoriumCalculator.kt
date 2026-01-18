package com.kholhang.fynical.utils

import com.kholhang.fynical.utils.EMICalculator

object MoratoriumCalculator {
    data class MoratoriumResult(
        val extendedTenureMonths: Int,
        val additionalInterest: Double,
        val newEMI: Double,
        val originalEMI: Double,
        val totalAdditionalAmount: Double
    )
    
    fun calculate(
        principal: Double,
        interestRate: Double,
        originalTenureMonths: Int,
        moratoriumMonths: Int
    ): MoratoriumResult {
        if (principal <= 0 || interestRate <= 0 || originalTenureMonths <= 0 || moratoriumMonths <= 0) {
            return MoratoriumResult(0, 0.0, 0.0, 0.0, 0.0)
        }
        
        val originalEMI = EMICalculator.calculateEMI(principal, interestRate, originalTenureMonths)
        val originalTotal = originalEMI * originalTenureMonths
        
        // During moratorium, only interest accrues
        val r = interestRate / 100.0 / 12.0
        val principalAfterMoratorium = principal * Math.pow(1 + r, moratoriumMonths.toDouble())
        val additionalInterest = principalAfterMoratorium - principal
        
        // New tenure to pay off the increased principal
        val newEMI = EMICalculator.calculateEMI(principalAfterMoratorium, interestRate, originalTenureMonths)
        val newTotal = (newEMI * originalTenureMonths) + (originalEMI * moratoriumMonths)
        
        // Calculate extended tenure if keeping same EMI
        val extendedTenure = calculateExtendedTenure(principalAfterMoratorium, interestRate, originalEMI)
        
        val totalAdditionalAmount = newTotal - originalTotal
        
        return MoratoriumResult(
            extendedTenureMonths = extendedTenure,
            additionalInterest = additionalInterest,
            newEMI = newEMI,
            originalEMI = originalEMI,
            totalAdditionalAmount = totalAdditionalAmount
        )
    }
    
    private fun calculateExtendedTenure(principal: Double, interestRate: Double, emi: Double): Int {
        val r = interestRate / 100.0 / 12.0
        if (r <= 0 || emi <= principal * r) {
            return 0
        }
        
        val tenure = Math.log(1 + (principal * r) / (emi - principal * r)) / Math.log(1 + r)
        return Math.ceil(tenure).toInt()
    }
}


