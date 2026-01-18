package com.kholhang.fynical.utils

object EPFCalculator {
    data class EPFResult(
        val employeeContribution: Double,
        val employerContribution: Double,
        val totalContribution: Double,
        val epfBalance: Double,
        val epsContribution: Double
    )
    
    /**
     * Calculate EPF contributions and balance
     * Employee Contribution: 12% of Basic Salary (capped at ₹15,000 basic)
     * Employer Contribution: 12% of Basic Salary
     *   - 8.33% goes to EPS (Employee Pension Scheme)
     *   - 3.67% goes to EPF
     * EPF Interest Rate: 8.15% (2024-25, can be updated)
     */
    fun calculate(
        basicSalary: Double,
        interestRate: Double = 8.15,
        years: Int
    ): EPFResult {
        if (basicSalary <= 0 || years <= 0) {
            return EPFResult(0.0, 0.0, 0.0, 0.0, 0.0)
        }
        
        // EPF contribution is capped at ₹15,000 basic salary
        val cappedBasic = minOf(basicSalary, 15000.0)
        
        // Employee contributes 12% of basic salary
        val monthlyEmployeeContribution = cappedBasic * 0.12
        
        // Employer contributes 12% of basic salary
        // 8.33% goes to EPS, 3.67% goes to EPF
        val monthlyEPSContribution = cappedBasic * 0.0833
        val monthlyEPFContribution = cappedBasic * 0.0367
        val monthlyEmployerContribution = monthlyEPSContribution + monthlyEPFContribution
        
        // Total monthly contribution to EPF
        val monthlyTotalEPF = monthlyEmployeeContribution + monthlyEPFContribution
        
        // Calculate EPF balance with compound interest
        val months = years * 12
        val monthlyRate = interestRate / 100.0 / 12.0
        
        // EPF balance calculation using SIP formula
        val epfBalance = if (monthlyRate > 0) {
            monthlyTotalEPF * ((Math.pow(1 + monthlyRate, months.toDouble()) - 1) / monthlyRate) * (1 + monthlyRate)
        } else {
            monthlyTotalEPF * months
        }
        
        val totalEmployeeContribution = monthlyEmployeeContribution * months
        val totalEmployerContribution = monthlyEmployerContribution * months
        val totalEPSContribution = monthlyEPSContribution * months
        
        return EPFResult(
            employeeContribution = totalEmployeeContribution,
            employerContribution = totalEmployerContribution,
            totalContribution = totalEmployeeContribution + totalEmployerContribution,
            epfBalance = epfBalance,
            epsContribution = totalEPSContribution
        )
    }
}

