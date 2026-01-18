package com.kholhang.fynical.utils

object GSTCalculator {
    enum class GSTType {
        CGST_SGST, // Split equally between CGST and SGST
        IGST // Integrated GST
    }
    
    data class GSTResult(
        val baseAmount: Double,
        val gstAmount: Double,
        val cgstAmount: Double,
        val sgstAmount: Double,
        val igstAmount: Double,
        val totalAmount: Double,
        val gstType: GSTType
    )
    
    fun calculateFromBase(
        baseAmount: Double,
        gstRate: Double,
        gstType: GSTType = GSTType.CGST_SGST
    ): GSTResult {
        if (baseAmount <= 0 || gstRate < 0 || gstRate > 100) {
            return GSTResult(0.0, 0.0, 0.0, 0.0, 0.0, baseAmount, gstType)
        }
        
        val gstAmount = (baseAmount * gstRate) / 100.0
        val cgstAmount = if (gstType == GSTType.CGST_SGST) gstAmount / 2.0 else 0.0
        val sgstAmount = if (gstType == GSTType.CGST_SGST) gstAmount / 2.0 else 0.0
        val igstAmount = if (gstType == GSTType.IGST) gstAmount else 0.0
        val totalAmount = baseAmount + gstAmount
        
        return GSTResult(
            baseAmount = baseAmount,
            gstAmount = gstAmount,
            cgstAmount = cgstAmount,
            sgstAmount = sgstAmount,
            igstAmount = igstAmount,
            totalAmount = totalAmount,
            gstType = gstType
        )
    }
    
    fun calculateFromTotal(
        totalAmount: Double,
        gstRate: Double,
        gstType: GSTType = GSTType.CGST_SGST
    ): GSTResult {
        if (totalAmount <= 0 || gstRate < 0 || gstRate > 100) {
            return GSTResult(0.0, 0.0, 0.0, 0.0, 0.0, totalAmount, gstType)
        }
        
        // Base Amount = Total Amount / (1 + GST Rate / 100)
        val baseAmount = totalAmount / (1 + (gstRate / 100.0))
        val gstAmount = totalAmount - baseAmount
        val cgstAmount = if (gstType == GSTType.CGST_SGST) gstAmount / 2.0 else 0.0
        val sgstAmount = if (gstType == GSTType.CGST_SGST) gstAmount / 2.0 else 0.0
        val igstAmount = if (gstType == GSTType.IGST) gstAmount else 0.0
        
        return GSTResult(
            baseAmount = baseAmount,
            gstAmount = gstAmount,
            cgstAmount = cgstAmount,
            sgstAmount = sgstAmount,
            igstAmount = igstAmount,
            totalAmount = totalAmount,
            gstType = gstType
        )
    }
}


