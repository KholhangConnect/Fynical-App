package com.kholhang.fynical.utils

object DiscountCalculator {
    enum class DiscountMode {
        PERCENTAGE_OFF,
        FIXED_AMOUNT_OFF,
        BUY_X_GET_Y
    }
    
    data class DiscountResult(
        val discountAmount: Double,
        val finalPrice: Double,
        val savings: Double,
        val originalPrice: Double
    )
    
    fun calculatePercentageOff(
        originalPrice: Double,
        discountPercentage: Double
    ): DiscountResult {
        if (originalPrice <= 0 || discountPercentage < 0 || discountPercentage > 100) {
            return DiscountResult(0.0, originalPrice, 0.0, originalPrice)
        }
        
        val discountAmount = (originalPrice * discountPercentage) / 100.0
        val finalPrice = originalPrice - discountAmount
        val savings = discountAmount
        
        return DiscountResult(
            discountAmount = discountAmount,
            finalPrice = finalPrice,
            savings = savings,
            originalPrice = originalPrice
        )
    }
    
    fun calculateFixedAmountOff(
        originalPrice: Double,
        discountAmount: Double
    ): DiscountResult {
        if (originalPrice <= 0 || discountAmount < 0 || discountAmount > originalPrice) {
            return DiscountResult(0.0, originalPrice, 0.0, originalPrice)
        }
        
        val finalPrice = originalPrice - discountAmount
        val savings = discountAmount
        
        return DiscountResult(
            discountAmount = discountAmount,
            finalPrice = finalPrice,
            savings = savings,
            originalPrice = originalPrice
        )
    }
    
    fun calculateBuyXGetY(
        originalPrice: Double,
        buyQuantity: Int,
        getQuantity: Int
    ): DiscountResult {
        if (originalPrice <= 0 || buyQuantity <= 0 || getQuantity <= 0) {
            return DiscountResult(0.0, originalPrice, 0.0, originalPrice)
        }
        
        val totalItems = buyQuantity + getQuantity
        val pricePerItem = originalPrice / buyQuantity // Assuming originalPrice is for buyQuantity items
        val totalOriginalPrice = pricePerItem * totalItems
        val finalPrice = originalPrice // Pay only for buyQuantity items
        val discountAmount = totalOriginalPrice - finalPrice
        val savings = discountAmount
        
        return DiscountResult(
            discountAmount = discountAmount,
            finalPrice = finalPrice,
            savings = savings,
            originalPrice = totalOriginalPrice
        )
    }
}


