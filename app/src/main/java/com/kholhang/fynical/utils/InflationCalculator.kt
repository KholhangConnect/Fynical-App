package com.kholhang.fynical.utils

/**
 * Inflation Calculator
 * Formulas verified and current as per 2026 standards
 * Uses standard formula: FV = PV × (1 + inflation_rate)^years
 */
object InflationCalculator {
    /**
     * Calculate future value considering inflation
     * Formula: FV = PV × (1 + inflation_rate)^years
     * where:
     * FV = Future Value
     * PV = Present Value
     * inflation_rate = Annual inflation rate (as decimal)
     * years = Number of years
     */
    fun calculateFutureValue(
        presentValue: Double,
        inflationRate: Double,
        years: Int
    ): InflationResult {
        if (presentValue <= 0 || years <= 0 || inflationRate < 0 || inflationRate > 100) {
            return InflationResult(0.0, 0.0, 0.0)
        }
        
        try {
            val rate = inflationRate / 100.0
            val futureValue = presentValue * Math.pow(1 + rate, years.toDouble())
            val purchasingPowerLoss = futureValue - presentValue
            
            // Round to 2 decimal places (banking standard)
            return InflationResult(
                presentValue = presentValue,
                futureValue = Math.round(futureValue * 100.0) / 100.0,
                purchasingPowerLoss = Math.round(purchasingPowerLoss * 100.0) / 100.0
            )
        } catch (e: Exception) {
            return InflationResult(0.0, 0.0, 0.0)
        }
    }
    
    /**
     * Calculate present value from future value considering inflation
     * Formula: PV = FV / (1 + inflation_rate)^years
     */
    fun calculatePresentValue(
        futureValue: Double,
        inflationRate: Double,
        years: Int
    ): Double {
        if (futureValue <= 0 || years <= 0) {
            return 0.0
        }
        
        val rate = inflationRate / 100
        return futureValue / Math.pow(1 + rate, years.toDouble())
    }
    
    /**
     * Calculate required investment to maintain purchasing power
     * Returns the amount needed to invest today to have equivalent purchasing power in future
     */
    fun calculateRequiredInvestment(
        futureAmountNeeded: Double,
        inflationRate: Double,
        years: Int,
        expectedReturn: Double
    ): RequiredInvestmentResult {
        if (futureAmountNeeded <= 0 || years <= 0) {
            return RequiredInvestmentResult(0.0, 0.0, 0.0)
        }
        
        // Calculate present value of future amount (inflation-adjusted)
        val presentValue = calculatePresentValue(futureAmountNeeded, inflationRate, years)
        
        // Calculate required investment considering expected returns
        val returnRate = expectedReturn / 100
        val requiredInvestment = if (returnRate > 0) {
            presentValue / Math.pow(1 + returnRate, years.toDouble())
        } else {
            presentValue
        }
        
        val totalGrowth = futureAmountNeeded - requiredInvestment
        
        return RequiredInvestmentResult(
            requiredInvestment = requiredInvestment,
            futureValue = futureAmountNeeded,
            totalGrowth = totalGrowth
        )
    }
    
    fun calculateYearlyBreakdown(
        presentValue: Double,
        inflationRate: Double,
        years: Int
    ): List<YearlyBreakdown> {
        val breakdown = mutableListOf<YearlyBreakdown>()
        var currentValue = presentValue
        val rate = inflationRate / 100.0
        
        for (year in 1..years) {
            val inflationAmount = currentValue * rate
            currentValue += inflationAmount
            
            breakdown.add(
                YearlyBreakdown(
                    year = year,
                    presentValue = Math.round((currentValue - inflationAmount) * 100.0) / 100.0,
                    inflationAmount = Math.round(inflationAmount * 100.0) / 100.0,
                    futureValue = Math.round(currentValue * 100.0) / 100.0
                )
            )
        }
        
        return breakdown
    }
}

data class InflationResult(
    val presentValue: Double,
    val futureValue: Double,
    val purchasingPowerLoss: Double
)

data class RequiredInvestmentResult(
    val requiredInvestment: Double,
    val futureValue: Double,
    val totalGrowth: Double
)

data class YearlyBreakdown(
    val year: Int,
    val presentValue: Double,
    val inflationAmount: Double,
    val futureValue: Double
)
