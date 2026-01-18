package com.kholhang.fynical.utils

import com.kholhang.fynical.utils.EMICalculator

object PrepaymentCalculator {
    data class PrepaymentResult(
        val newEMI: Double?,
        val newTenureMonths: Int?,
        val interestSavings: Double,
        val totalSavings: Double,
        val option: PrepaymentOption
    )
    
    enum class PrepaymentOption {
        REDUCE_EMI,
        REDUCE_TENURE
    }
    
    fun calculateWithEMIReduction(
        principal: Double,
        interestRate: Double,
        tenureMonths: Int,
        prepaymentAmount: Double,
        prepaymentMonth: Int
    ): PrepaymentResult {
        if (principal <= 0 || interestRate <= 0 || tenureMonths <= 0 || prepaymentAmount <= 0) {
            return PrepaymentResult(null, null, 0.0, 0.0, PrepaymentOption.REDUCE_EMI)
        }
        
        val originalEMI = EMICalculator.calculateEMI(principal, interestRate, tenureMonths)
        val originalTotal = originalEMI * tenureMonths
        
        // Calculate outstanding principal at prepayment month
        val r = interestRate / 100.0 / 12.0
        val outstandingPrincipal = principal * Math.pow(1 + r, prepaymentMonth.toDouble()) - 
            originalEMI * ((Math.pow(1 + r, prepaymentMonth.toDouble()) - 1) / r)
        
        val newPrincipal = outstandingPrincipal - prepaymentAmount
        val remainingMonths = tenureMonths - prepaymentMonth
        
        if (newPrincipal <= 0 || remainingMonths <= 0) {
            return PrepaymentResult(null, null, 0.0, 0.0, PrepaymentOption.REDUCE_EMI)
        }
        
        val newEMI = EMICalculator.calculateEMI(newPrincipal, interestRate, remainingMonths)
        val newTotal = (originalEMI * prepaymentMonth) + (newEMI * remainingMonths) + prepaymentAmount
        
        val interestSavings = originalTotal - newTotal
        val totalSavings = interestSavings
        
        return PrepaymentResult(
            newEMI = newEMI,
            newTenureMonths = null,
            interestSavings = interestSavings,
            totalSavings = totalSavings,
            option = PrepaymentOption.REDUCE_EMI
        )
    }
    
    fun calculateWithTenureReduction(
        principal: Double,
        interestRate: Double,
        tenureMonths: Int,
        prepaymentAmount: Double,
        prepaymentMonth: Int
    ): PrepaymentResult {
        if (principal <= 0 || interestRate <= 0 || tenureMonths <= 0 || prepaymentAmount <= 0) {
            return PrepaymentResult(null, null, 0.0, 0.0, PrepaymentOption.REDUCE_TENURE)
        }
        
        val originalEMI = EMICalculator.calculateEMI(principal, interestRate, tenureMonths)
        val originalTotal = originalEMI * tenureMonths
        
        // Calculate outstanding principal at prepayment month
        val r = interestRate / 100.0 / 12.0
        val outstandingPrincipal = principal * Math.pow(1 + r, prepaymentMonth.toDouble()) - 
            originalEMI * ((Math.pow(1 + r, prepaymentMonth.toDouble()) - 1) / r)
        
        val newPrincipal = outstandingPrincipal - prepaymentAmount
        
        if (newPrincipal <= 0) {
            return PrepaymentResult(null, null, 0.0, 0.0, PrepaymentOption.REDUCE_TENURE)
        }
        
        // Calculate new tenure keeping EMI same
        val newTenureMonths = calculateTenureFromEMI(newPrincipal, interestRate, originalEMI)
        val newTotal = (originalEMI * prepaymentMonth) + (originalEMI * newTenureMonths) + prepaymentAmount
        
        val interestSavings = originalTotal - newTotal
        val totalSavings = interestSavings
        
        return PrepaymentResult(
            newEMI = null,
            newTenureMonths = newTenureMonths,
            interestSavings = interestSavings,
            totalSavings = totalSavings,
            option = PrepaymentOption.REDUCE_TENURE
        )
    }
    
    private fun calculateTenureFromEMI(principal: Double, interestRate: Double, emi: Double): Int {
        val r = interestRate / 100.0 / 12.0
        if (r <= 0 || emi <= principal * r) {
            return 0
        }
        
        val tenure = Math.log(1 + (principal * r) / (emi - principal * r)) / Math.log(1 + r)
        return Math.ceil(tenure).toInt()
    }
}


