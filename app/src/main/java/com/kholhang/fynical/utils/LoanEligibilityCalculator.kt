package com.kholhang.fynical.utils

object LoanEligibilityCalculator {
    enum class LoanType {
        HOME_LOAN,
        CAR_LOAN,
        PERSONAL_LOAN,
        EDUCATION_LOAN
    }
    
    data class EligibilityResult(
        val eligibleLoanAmount: Double,
        val maxEMICapacity: Double,
        val recommendedLoanAmount: Double,
        val maxTenure: Int
    )
    
    fun calculate(
        monthlyIncome: Double,
        existingEMIs: Double,
        age: Int,
        loanType: LoanType = LoanType.HOME_LOAN,
        interestRate: Double = 8.5,
        tenureYears: Int = 20
    ): EligibilityResult {
        if (monthlyIncome <= 0) {
            return EligibilityResult(0.0, 0.0, 0.0, 0)
        }
        
        // Calculate maximum EMI capacity (typically 40-60% of income)
        val emiRatio = when (loanType) {
            LoanType.HOME_LOAN -> 0.60 // 60% for home loans
            LoanType.CAR_LOAN -> 0.40 // 40% for car loans
            LoanType.PERSONAL_LOAN -> 0.50 // 50% for personal loans
            LoanType.EDUCATION_LOAN -> 0.50 // 50% for education loans
        }
        
        val maxEMICapacity = (monthlyIncome * emiRatio) - existingEMIs
        val maxEMI = maxOf(0.0, maxEMICapacity)
        
        // Calculate eligible loan amount using reverse EMI calculation
        val r = interestRate / 100.0 / 12.0 // Monthly rate
        val n = tenureYears * 12.0 // Number of months
        
        val eligibleLoanAmount = if (r > 0 && n > 0) {
            maxEMI * ((Math.pow(1 + r, n) - 1) / (r * Math.pow(1 + r, n)))
        } else {
            0.0
        }
        
        // Recommended loan amount (80% of eligible)
        val recommendedLoanAmount = eligibleLoanAmount * 0.8
        
        // Maximum tenure based on age (typically up to retirement age - 5 years)
        val retirementAge = 60
        val maxTenure = maxOf(5, minOf(30, retirementAge - age - 5))
        
        return EligibilityResult(
            eligibleLoanAmount = eligibleLoanAmount,
            maxEMICapacity = maxEMI,
            recommendedLoanAmount = recommendedLoanAmount,
            maxTenure = maxTenure
        )
    }
}


