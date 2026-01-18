package com.kholhang.fynical.utils

object APYCalculator {
    /**
     * Atal Pension Yojana (APY) - Government Pension Scheme
     * Eligibility: Age 18-40 years
     * Retirement Age: 60 years
     * Pension Slabs: ₹1000, ₹2000, ₹3000, ₹4000, ₹5000 per month
     * 
     * Latest Official APY Contribution Chart (Updated as of 2026)
     * Based on official government rates from PFRDA
     * 
     * Note: As of December 2025, Government confirmed no changes to pension slabs
     * or contribution requirements. These rates remain current for 2026.
     * 
     * Form Update: Effective October 1, 2025, new registration forms required
     * with updated questions (e.g., foreign citizenship details).
     */
    
    /**
     * APY Contribution Chart
     * Maps [age] -> [pension amount] -> monthly contribution
     */
    private val apyContributionChart = mapOf(
        18 to mapOf(1000.0 to 42.0, 2000.0 to 84.0, 3000.0 to 126.0, 4000.0 to 168.0, 5000.0 to 210.0),
        19 to mapOf(1000.0 to 46.0, 2000.0 to 92.0, 3000.0 to 138.0, 4000.0 to 183.0, 5000.0 to 228.0),
        20 to mapOf(1000.0 to 50.0, 2000.0 to 100.0, 3000.0 to 150.0, 4000.0 to 198.0, 5000.0 to 248.0),
        21 to mapOf(1000.0 to 54.0, 2000.0 to 108.0, 3000.0 to 162.0, 4000.0 to 215.0, 5000.0 to 269.0),
        22 to mapOf(1000.0 to 59.0, 2000.0 to 117.0, 3000.0 to 177.0, 4000.0 to 234.0, 5000.0 to 292.0),
        23 to mapOf(1000.0 to 64.0, 2000.0 to 127.0, 3000.0 to 192.0, 4000.0 to 254.0, 5000.0 to 318.0),
        24 to mapOf(1000.0 to 70.0, 2000.0 to 139.0, 3000.0 to 208.0, 4000.0 to 277.0, 5000.0 to 346.0),
        25 to mapOf(1000.0 to 76.0, 2000.0 to 151.0, 3000.0 to 226.0, 4000.0 to 301.0, 5000.0 to 376.0),
        26 to mapOf(1000.0 to 82.0, 2000.0 to 164.0, 3000.0 to 246.0, 4000.0 to 327.0, 5000.0 to 409.0),
        27 to mapOf(1000.0 to 90.0, 2000.0 to 178.0, 3000.0 to 268.0, 4000.0 to 356.0, 5000.0 to 446.0),
        28 to mapOf(1000.0 to 97.0, 2000.0 to 194.0, 3000.0 to 292.0, 4000.0 to 388.0, 5000.0 to 485.0),
        29 to mapOf(1000.0 to 106.0, 2000.0 to 212.0, 3000.0 to 318.0, 4000.0 to 423.0, 5000.0 to 529.0),
        30 to mapOf(1000.0 to 116.0, 2000.0 to 231.0, 3000.0 to 347.0, 4000.0 to 462.0, 5000.0 to 577.0),
        31 to mapOf(1000.0 to 126.0, 2000.0 to 252.0, 3000.0 to 379.0, 4000.0 to 504.0, 5000.0 to 630.0),
        32 to mapOf(1000.0 to 138.0, 2000.0 to 276.0, 3000.0 to 414.0, 4000.0 to 551.0, 5000.0 to 689.0),
        33 to mapOf(1000.0 to 151.0, 2000.0 to 302.0, 3000.0 to 453.0, 4000.0 to 602.0, 5000.0 to 752.0),
        34 to mapOf(1000.0 to 165.0, 2000.0 to 330.0, 3000.0 to 495.0, 4000.0 to 659.0, 5000.0 to 824.0),
        35 to mapOf(1000.0 to 181.0, 2000.0 to 362.0, 3000.0 to 543.0, 4000.0 to 722.0, 5000.0 to 902.0),
        36 to mapOf(1000.0 to 198.0, 2000.0 to 396.0, 3000.0 to 594.0, 4000.0 to 792.0, 5000.0 to 990.0),
        37 to mapOf(1000.0 to 218.0, 2000.0 to 436.0, 3000.0 to 654.0, 4000.0 to 870.0, 5000.0 to 1087.0),
        38 to mapOf(1000.0 to 240.0, 2000.0 to 480.0, 3000.0 to 720.0, 4000.0 to 957.0, 5000.0 to 1196.0),
        39 to mapOf(1000.0 to 264.0, 2000.0 to 528.0, 3000.0 to 792.0, 4000.0 to 1054.0, 5000.0 to 1318.0),
        40 to mapOf(1000.0 to 291.0, 2000.0 to 582.0, 3000.0 to 873.0, 4000.0 to 1164.0, 5000.0 to 1454.0)
    )
    
    /**
     * Corpus return to nominee (fixed regardless of entry age)
     */
    private val corpusReturnChart = mapOf(
        1000.0 to 170000.0,  // ₹1.7 lakh
        2000.0 to 340000.0,  // ₹3.4 lakh
        3000.0 to 510000.0,  // ₹5.1 lakh
        4000.0 to 680000.0,  // ₹6.8 lakh
        5000.0 to 850000.0   // ₹8.5 lakh
    )
    
    /**
     * Valid pension amounts
     */
    val validPensionAmounts = listOf(1000.0, 2000.0, 3000.0, 4000.0, 5000.0)
    
    /**
     * Calculate APY premium and details
     * @param entryAge Age at which person joins APY (18-40 years)
     * @param pensionAmount Desired monthly pension (₹1000, ₹2000, ₹3000, ₹4000, or ₹5000)
     * @return APYResult with all calculation details
     */
    fun calculateAPY(entryAge: Int, pensionAmount: Double): APYResult {
        // Validate age
        if (entryAge < 18 || entryAge > 40) {
            return APYResult(
                entryAge = entryAge,
                pensionAmount = pensionAmount,
                monthlyContribution = 0.0,
                yearsToRetirement = 0,
                totalContribution = 0.0,
                corpusReturn = 0.0,
                monthlyPension = 0.0,
                isValid = false,
                errorMessage = "Age must be between 18-40 years"
            )
        }
        
        // Validate pension amount
        if (pensionAmount !in validPensionAmounts) {
            return APYResult(
                entryAge = entryAge,
                pensionAmount = pensionAmount,
                monthlyContribution = 0.0,
                yearsToRetirement = 0,
                totalContribution = 0.0,
                corpusReturn = 0.0,
                monthlyPension = 0.0,
                isValid = false,
                errorMessage = "Pension amount must be ₹1000, ₹2000, ₹3000, ₹4000, or ₹5000"
            )
        }
        
        // Get monthly contribution from chart
        val monthlyContribution = apyContributionChart[entryAge]?.get(pensionAmount) ?: 0.0
        
        if (monthlyContribution == 0.0) {
            return APYResult(
                entryAge = entryAge,
                pensionAmount = pensionAmount,
                monthlyContribution = 0.0,
                yearsToRetirement = 0,
                totalContribution = 0.0,
                corpusReturn = 0.0,
                monthlyPension = 0.0,
                isValid = false,
                errorMessage = "Invalid age or pension amount combination"
            )
        }
        
        // Calculate years to retirement
        val yearsToRetirement = 60 - entryAge
        
        // Calculate total contribution
        val totalMonths = yearsToRetirement * 12
        val totalContribution = monthlyContribution * totalMonths
        
        // Get corpus return to nominee
        val corpusReturn = corpusReturnChart[pensionAmount] ?: 0.0
        
        return APYResult(
            entryAge = entryAge,
            pensionAmount = pensionAmount,
            monthlyContribution = Math.round(monthlyContribution * 100.0) / 100.0,
            yearsToRetirement = yearsToRetirement,
            totalContribution = Math.round(totalContribution * 100.0) / 100.0,
            corpusReturn = corpusReturn,
            monthlyPension = pensionAmount,
            isValid = true,
            errorMessage = null
        )
    }
    
    /**
     * Get all available pension amounts
     */
    fun getAvailablePensionAmounts(): List<Double> {
        return validPensionAmounts
    }
    
    /**
     * Check if age is valid for APY
     */
    fun isValidAge(age: Int): Boolean {
        return age >= 18 && age <= 40
    }
    
    /**
     * Check if pension amount is valid
     */
    fun isValidPensionAmount(amount: Double): Boolean {
        return amount in validPensionAmounts
    }
    
    /**
     * Result data class for APY calculation
     */
    data class APYResult(
        val entryAge: Int,
        val pensionAmount: Double,
        val monthlyContribution: Double,
        val yearsToRetirement: Int,
        val totalContribution: Double,
        val corpusReturn: Double,
        val monthlyPension: Double,
        val isValid: Boolean,
        val errorMessage: String?
    )
}
