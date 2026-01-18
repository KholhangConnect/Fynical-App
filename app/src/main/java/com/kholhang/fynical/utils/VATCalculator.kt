package com.kholhang.fynical.utils

object VATCalculator {
    data class VATResult(
        val baseAmount: Double,
        val vatAmount: Double,
        val totalAmount: Double
    )
    
    fun calculateFromBase(
        baseAmount: Double,
        vatRate: Double
    ): VATResult {
        if (baseAmount <= 0 || vatRate < 0 || vatRate > 100) {
            return VATResult(baseAmount, 0.0, baseAmount)
        }
        
        val vatAmount = (baseAmount * vatRate) / 100.0
        val totalAmount = baseAmount + vatAmount
        
        return VATResult(
            baseAmount = baseAmount,
            vatAmount = vatAmount,
            totalAmount = totalAmount
        )
    }
    
    fun calculateFromTotal(
        totalAmount: Double,
        vatRate: Double
    ): VATResult {
        if (totalAmount <= 0 || vatRate < 0 || vatRate > 100) {
            return VATResult(0.0, 0.0, totalAmount)
        }
        
        // Base Amount = Total Amount / (1 + VAT Rate / 100)
        val baseAmount = totalAmount / (1 + (vatRate / 100.0))
        val vatAmount = totalAmount - baseAmount
        
        return VATResult(
            baseAmount = baseAmount,
            vatAmount = vatAmount,
            totalAmount = totalAmount
        )
    }
}


