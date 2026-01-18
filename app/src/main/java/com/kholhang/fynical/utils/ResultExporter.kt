package com.kholhang.fynical.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ResultExporter {
    /**
     * Export EMI calculation results as PDF
     */
    suspend fun exportEMIAsPDF(
        context: Context,
        principal: Double,
        interestRate: Double,
        tenureMonths: Int,
        emi: Double,
        totalAmount: Double,
        totalInterest: Double
    ): Uri? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            // Helper function to draw a line
            fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float) {
                val linePaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    strokeWidth = 1f
                }
                canvas.drawLine(x1, y1, x2, y2, linePaint)
            }
            
            // Helper function to draw a rectangle
            fun drawRect(left: Float, top: Float, right: Float, bottom: Float, fillColor: Int? = null) {
                val rectPaint = android.graphics.Paint().apply {
                    if (fillColor != null) {
                        color = fillColor
                        style = android.graphics.Paint.Style.FILL
                    } else {
                        color = android.graphics.Color.BLACK
                        style = android.graphics.Paint.Style.STROKE
                        strokeWidth = 1f
                    }
                }
                canvas.drawRect(left, top, right, bottom, rectPaint)
            }
            
            var y = 40f
            val leftMargin = 50f
            val rightMargin = 545f
            val pageWidth = 595f
            val pageHeight = 842f
            
            val paint = android.graphics.Paint()
            paint.color = android.graphics.Color.BLACK
            
            // Header with background
            val headerHeight = 60f
            drawRect(leftMargin, y, rightMargin, y + headerHeight, android.graphics.Color.parseColor("#1976D2"))
            
            paint.color = android.graphics.Color.WHITE
            paint.textSize = 24f
            paint.isFakeBoldText = true
            paint.textAlign = android.graphics.Paint.Align.CENTER
            canvas.drawText("EMI CALCULATION STATEMENT", pageWidth / 2, y + 35f, paint)
            
            paint.textAlign = android.graphics.Paint.Align.LEFT
            paint.color = android.graphics.Color.BLACK
            y += headerHeight + 30f
            
            // Date
            paint.textSize = 10f
            paint.isFakeBoldText = false
            val dateFormat = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault())
            canvas.drawText("Generated on: ${dateFormat.format(Date())}", leftMargin, y, paint)
            y += 25f
            
            // Summary Box
            val summaryBoxTop = y
            drawRect(leftMargin, summaryBoxTop, rightMargin, summaryBoxTop + 120f)
            y += 15f
            
            paint.textSize = 14f
            paint.isFakeBoldText = true
            canvas.drawText("LOAN SUMMARY", leftMargin + 10f, y, paint)
            y += 25f
            
            paint.textSize = 11f
            paint.isFakeBoldText = false
            val summaryLeft = leftMargin + 15f
            val summaryRight = (leftMargin + rightMargin) / 2
            
            canvas.drawText("Principal Amount:", summaryLeft, y, paint)
            paint.isFakeBoldText = true
            canvas.drawText(CurrencyFormatter.format(principal), summaryRight, y, paint)
            paint.isFakeBoldText = false
            y += 18f
            
            canvas.drawText("Interest Rate:", summaryLeft, y, paint)
            paint.isFakeBoldText = true
            canvas.drawText("$interestRate% per annum", summaryRight, y, paint)
            paint.isFakeBoldText = false
            y += 18f
            
            canvas.drawText("Loan Tenure:", summaryLeft, y, paint)
            paint.isFakeBoldText = true
            canvas.drawText("$tenureMonths months (${String.format("%.1f", tenureMonths / 12.0)} years)", summaryRight, y, paint)
            paint.isFakeBoldText = false
            y += 18f
            
            canvas.drawText("Monthly EMI:", summaryLeft, y, paint)
            paint.isFakeBoldText = true
            paint.textSize = 12f
            paint.color = android.graphics.Color.parseColor("#1976D2")
            canvas.drawText(CurrencyFormatter.format(emi), summaryRight, y, paint)
            paint.color = android.graphics.Color.BLACK
            paint.textSize = 11f
            paint.isFakeBoldText = false
            y = summaryBoxTop + 120f + 20f
            
            // Results Box
            val resultsBoxTop = y
            drawRect(leftMargin, resultsBoxTop, rightMargin, resultsBoxTop + 80f, android.graphics.Color.parseColor("#F5F5F5"))
            y += 15f
            
            paint.textSize = 14f
            paint.isFakeBoldText = true
            canvas.drawText("PAYMENT SUMMARY", leftMargin + 10f, y, paint)
            y += 25f
            
            paint.textSize = 11f
            paint.isFakeBoldText = false
            val resultsLeft = leftMargin + 15f
            val resultsRight = (leftMargin + rightMargin) / 2
            
            canvas.drawText("Total Amount Payable:", resultsLeft, y, paint)
            paint.isFakeBoldText = true
            canvas.drawText(CurrencyFormatter.format(totalAmount), resultsRight, y, paint)
            paint.isFakeBoldText = false
            y += 18f
            
            canvas.drawText("Total Interest Payable:", resultsLeft, y, paint)
            paint.isFakeBoldText = true
            canvas.drawText(CurrencyFormatter.format(totalInterest), resultsRight, y, paint)
            paint.isFakeBoldText = false
            y = resultsBoxTop + 80f + 25f
            
            // Amortization Schedule Table
            paint.textSize = 14f
            paint.isFakeBoldText = true
            canvas.drawText("AMORTIZATION SCHEDULE (First 12 Months)", leftMargin, y, paint)
            y += 25f
            
            val schedule = AmortizationCalculator.calculateSchedule(principal, interestRate, tenureMonths)
            val tableTop = y
            val rowHeight = 20f
            val headerHeight_table = 25f
            val numRows = minOf(12, schedule.entries.size)
            val tableHeight = headerHeight_table + (rowHeight * numRows)
            
            // Table borders
            drawRect(leftMargin, tableTop, rightMargin, tableTop + tableHeight)
            
            // Column widths
            val col1Width = 60f  // Month
            val col2Width = 120f // Beginning Balance
            val col3Width = 100f // Principal
            val col4Width = 100f // Interest
            val col5Width = 120f // Ending Balance
            
            val col1X = leftMargin + 5f
            val col2X = col1X + col1Width + 5f
            val col3X = col2X + col2Width + 5f
            val col4X = col3X + col3Width + 5f
            val col5X = col4X + col4Width + 5f
            
            // Draw column dividers
            drawLine(col1X + col1Width, tableTop, col1X + col1Width, tableTop + tableHeight)
            drawLine(col2X + col2Width, tableTop, col2X + col2Width, tableTop + tableHeight)
            drawLine(col3X + col3Width, tableTop, col3X + col3Width, tableTop + tableHeight)
            drawLine(col4X + col4Width, tableTop, col4X + col4Width, tableTop + tableHeight)
            
            // Header row background
            drawRect(leftMargin + 1f, tableTop + 1f, rightMargin - 1f, tableTop + headerHeight_table, 
                android.graphics.Color.parseColor("#E3F2FD"))
            
            // Header text
            y = tableTop + 18f
            paint.textSize = 10f
            paint.isFakeBoldText = true
            canvas.drawText("Month", col1X, y, paint)
            canvas.drawText("Beg. Balance", col2X, y, paint)
            canvas.drawText("Principal", col3X, y, paint)
            canvas.drawText("Interest", col4X, y, paint)
            canvas.drawText("End. Balance", col5X, y, paint)
            
            // Header row divider
            drawLine(leftMargin, tableTop + headerHeight_table, rightMargin, tableTop + headerHeight_table)
            
            // Table rows
            paint.textSize = 9f
            paint.isFakeBoldText = false
            
            schedule.entries.take(12).forEachIndexed { index, entry ->
                // Position text in the center of the row
                val rowY = tableTop + headerHeight_table + (index * rowHeight) + 12f
                
                // Draw text content
                canvas.drawText("${entry.month}", col1X, rowY, paint)
                canvas.drawText(CurrencyFormatter.format(entry.beginningBalance), col2X, rowY, paint)
                canvas.drawText(CurrencyFormatter.format(entry.principalPayment), col3X, rowY, paint)
                canvas.drawText(CurrencyFormatter.format(entry.interestPayment), col4X, rowY, paint)
                canvas.drawText(CurrencyFormatter.format(entry.endingBalance), col5X, rowY, paint)
                
                // Row separator lines removed to avoid misalignment issues
            }
            
            // Footer
            y = pageHeight - 40f
            paint.textSize = 8f
            paint.isFakeBoldText = false
            paint.textAlign = android.graphics.Paint.Align.CENTER
            canvas.drawText("This is a computer-generated document. No signature required.", pageWidth / 2, y, paint)
            y += 15f
            canvas.drawText("Generated by Fincal - Financial Calculator App", pageWidth / 2, y, paint)
            
            pdfDocument.finishPage(page)
            
            val file = File(context.cacheDir, "emi_result_${System.currentTimeMillis()}.pdf")
            FileOutputStream(file).use { fos ->
                pdfDocument.writeTo(fos)
            }
            pdfDocument.close()
            
            getFileUri(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Export EMI calculation results as Image
     */
    suspend fun exportEMIAsImage(
        context: Context,
        principal: Double,
        interestRate: Double,
        tenureMonths: Int,
        emi: Double,
        totalAmount: Double,
        totalInterest: Double
    ): Uri? {
        return try {
            val width = 800
            val height = 1200
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(android.graphics.Color.WHITE)
            
            val paint = android.graphics.Paint()
            paint.color = android.graphics.Color.BLACK
            paint.textSize = 24f
            paint.isFakeBoldText = true
            
            var y = 50f
            
            canvas.drawText("EMI Calculator Results", 50f, y, paint)
            y += 40f
            
            paint.textSize = 16f
            paint.isFakeBoldText = false
            
            canvas.drawText("Principal: ${CurrencyFormatter.format(principal)}", 50f, y, paint)
            y += 30f
            canvas.drawText("Interest Rate: $interestRate% p.a.", 50f, y, paint)
            y += 30f
            canvas.drawText("Tenure: $tenureMonths months", 50f, y, paint)
            y += 40f
            
            paint.isFakeBoldText = true
            canvas.drawText("Monthly EMI: ${CurrencyFormatter.format(emi)}", 50f, y, paint)
            y += 30f
            paint.isFakeBoldText = false
            canvas.drawText("Total Amount: ${CurrencyFormatter.format(totalAmount)}", 50f, y, paint)
            y += 30f
            canvas.drawText("Total Interest: ${CurrencyFormatter.format(totalInterest)}", 50f, y, paint)
            
            val file = File(context.cacheDir, "emi_result_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            
            getFileUri(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Export FD calculation results as PDF
     */
    suspend fun exportFDAsPDF(
        context: Context,
        principal: Double,
        interestRate: Double,
        tenureYears: Double,
        compoundingFrequency: FDCalculator.CompoundingFrequency,
        result: FDCalculator.FDResult
    ): Uri? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            var y = 50f
            val paint = android.graphics.Paint()
            paint.textSize = 18f
            paint.isFakeBoldText = true
            
            canvas.drawText("Fixed Deposit Calculator Results", 50f, y, paint)
            y += 40f
            
            paint.textSize = 12f
            paint.isFakeBoldText = false
            
            canvas.drawText("Principal: ${CurrencyFormatter.format(principal)}", 50f, y, paint)
            y += 20f
            canvas.drawText("Interest Rate: $interestRate% p.a.", 50f, y, paint)
            y += 20f
            canvas.drawText("Tenure: $tenureYears years", 50f, y, paint)
            y += 20f
            canvas.drawText("Compounding: ${compoundingFrequency.name}", 50f, y, paint)
            y += 30f
            
            paint.isFakeBoldText = true
            canvas.drawText("Maturity Amount: ${CurrencyFormatter.format(result.maturityAmount)}", 50f, y, paint)
            y += 25f
            paint.isFakeBoldText = false
            canvas.drawText("Interest Earned: ${CurrencyFormatter.format(result.interestEarned)}", 50f, y, paint)
            
            pdfDocument.finishPage(page)
            
            val file = File(context.cacheDir, "fd_result_${System.currentTimeMillis()}.pdf")
            FileOutputStream(file).use { fos ->
                pdfDocument.writeTo(fos)
            }
            pdfDocument.close()
            
            getFileUri(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Export FD calculation results as Image
     */
    suspend fun exportFDAsImage(
        context: Context,
        principal: Double,
        interestRate: Double,
        tenureYears: Double,
        compoundingFrequency: FDCalculator.CompoundingFrequency,
        result: FDCalculator.FDResult
    ): Uri? {
        return try {
            val width = 800
            val height = 600
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(android.graphics.Color.WHITE)
            
            val paint = android.graphics.Paint()
            paint.color = android.graphics.Color.BLACK
            paint.textSize = 24f
            paint.isFakeBoldText = true
            
            var y = 50f
            canvas.drawText("Fixed Deposit Results", 50f, y, paint)
            y += 40f
            
            paint.textSize = 16f
            paint.isFakeBoldText = false
            canvas.drawText("Principal: ${CurrencyFormatter.format(principal)}", 50f, y, paint)
            y += 30f
            canvas.drawText("Rate: $interestRate% | Tenure: $tenureYears years", 50f, y, paint)
            y += 40f
            paint.isFakeBoldText = true
            paint.textSize = 20f
            canvas.drawText("Maturity: ${CurrencyFormatter.format(result.maturityAmount)}", 50f, y, paint)
            y += 30f
            paint.isFakeBoldText = false
            paint.textSize = 16f
            canvas.drawText("Interest: ${CurrencyFormatter.format(result.interestEarned)}", 50f, y, paint)
            
            val file = File(context.cacheDir, "fd_result_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            
            getFileUri(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Export RD calculation results as PDF
     */
    suspend fun exportRDAsPDF(
        context: Context,
        monthlyDeposit: Double,
        interestRate: Double,
        tenureMonths: Int,
        result: RDCalculator.RDResult
    ): Uri? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            var y = 50f
            val paint = android.graphics.Paint()
            paint.textSize = 18f
            paint.isFakeBoldText = true
            
            canvas.drawText("Recurring Deposit Calculator Results", 50f, y, paint)
            y += 40f
            
            paint.textSize = 12f
            paint.isFakeBoldText = false
            
            canvas.drawText("Monthly Deposit: ${CurrencyFormatter.format(monthlyDeposit)}", 50f, y, paint)
            y += 20f
            canvas.drawText("Interest Rate: $interestRate% p.a.", 50f, y, paint)
            y += 20f
            canvas.drawText("Tenure: $tenureMonths months", 50f, y, paint)
            y += 30f
            
            paint.isFakeBoldText = true
            canvas.drawText("Maturity Amount: ${CurrencyFormatter.format(result.maturityAmount)}", 50f, y, paint)
            y += 25f
            paint.isFakeBoldText = false
            canvas.drawText("Total Deposits: ${CurrencyFormatter.format(result.totalDeposits)}", 50f, y, paint)
            y += 20f
            canvas.drawText("Interest Earned: ${CurrencyFormatter.format(result.interestEarned)}", 50f, y, paint)
            
            pdfDocument.finishPage(page)
            
            val file = File(context.cacheDir, "rd_result_${System.currentTimeMillis()}.pdf")
            FileOutputStream(file).use { fos ->
                pdfDocument.writeTo(fos)
            }
            pdfDocument.close()
            
            getFileUri(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Export RD calculation results as Image
     */
    suspend fun exportRDAsImage(
        context: Context,
        monthlyDeposit: Double,
        interestRate: Double,
        tenureMonths: Int,
        result: RDCalculator.RDResult
    ): Uri? {
        return try {
            val width = 800
            val height = 600
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(android.graphics.Color.WHITE)
            
            val paint = android.graphics.Paint()
            paint.color = android.graphics.Color.BLACK
            paint.textSize = 24f
            paint.isFakeBoldText = true
            
            var y = 50f
            canvas.drawText("Recurring Deposit Results", 50f, y, paint)
            y += 40f
            
            paint.textSize = 16f
            paint.isFakeBoldText = false
            canvas.drawText("Monthly Deposit: ${CurrencyFormatter.format(monthlyDeposit)}", 50f, y, paint)
            y += 30f
            canvas.drawText("Rate: $interestRate% | Tenure: $tenureMonths months", 50f, y, paint)
            y += 40f
            paint.isFakeBoldText = true
            paint.textSize = 20f
            canvas.drawText("Maturity: ${CurrencyFormatter.format(result.maturityAmount)}", 50f, y, paint)
            y += 30f
            paint.isFakeBoldText = false
            paint.textSize = 16f
            canvas.drawText("Interest: ${CurrencyFormatter.format(result.interestEarned)}", 50f, y, paint)
            
            val file = File(context.cacheDir, "rd_result_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            
            getFileUri(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Export SIP calculation results as PDF
     */
    suspend fun exportSIPAsPDF(
        context: Context,
        monthlyInvestment: Double,
        annualRate: Double,
        tenureMonths: Int,
        result: SIPCalculator.SIPResult
    ): Uri? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            var y = 50f
            val paint = android.graphics.Paint()
            paint.textSize = 18f
            paint.isFakeBoldText = true
            
            canvas.drawText("SIP Calculator Results", 50f, y, paint)
            y += 40f
            
            paint.textSize = 12f
            paint.isFakeBoldText = false
            
            canvas.drawText("Monthly Investment: ${CurrencyFormatter.format(monthlyInvestment)}", 50f, y, paint)
            y += 20f
            canvas.drawText("Expected Return: $annualRate% p.a.", 50f, y, paint)
            y += 20f
            canvas.drawText("Tenure: $tenureMonths months", 50f, y, paint)
            y += 30f
            
            paint.isFakeBoldText = true
            canvas.drawText("Maturity Amount: ${CurrencyFormatter.format(result.maturityAmount)}", 50f, y, paint)
            y += 25f
            paint.isFakeBoldText = false
            canvas.drawText("Total Investment: ${CurrencyFormatter.format(result.totalInvestment)}", 50f, y, paint)
            y += 20f
            canvas.drawText("Estimated Returns: ${CurrencyFormatter.format(result.returns)}", 50f, y, paint)
            
            pdfDocument.finishPage(page)
            
            val file = File(context.cacheDir, "sip_result_${System.currentTimeMillis()}.pdf")
            FileOutputStream(file).use { fos ->
                pdfDocument.writeTo(fos)
            }
            pdfDocument.close()
            
            getFileUri(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Export SIP calculation results as Image
     */
    suspend fun exportSIPAsImage(
        context: Context,
        monthlyInvestment: Double,
        annualRate: Double,
        tenureMonths: Int,
        result: SIPCalculator.SIPResult
    ): Uri? {
        return try {
            val width = 800
            val height = 600
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(android.graphics.Color.WHITE)
            
            val paint = android.graphics.Paint()
            paint.color = android.graphics.Color.BLACK
            paint.textSize = 24f
            paint.isFakeBoldText = true
            
            var y = 50f
            canvas.drawText("SIP Calculator Results", 50f, y, paint)
            y += 40f
            
            paint.textSize = 16f
            paint.isFakeBoldText = false
            canvas.drawText("Monthly Investment: ${CurrencyFormatter.format(monthlyInvestment)}", 50f, y, paint)
            y += 30f
            canvas.drawText("Return: $annualRate% | Tenure: $tenureMonths months", 50f, y, paint)
            y += 40f
            paint.isFakeBoldText = true
            paint.textSize = 20f
            canvas.drawText("Maturity: ${CurrencyFormatter.format(result.maturityAmount)}", 50f, y, paint)
            y += 30f
            paint.isFakeBoldText = false
            paint.textSize = 16f
            canvas.drawText("Returns: ${CurrencyFormatter.format(result.returns)}", 50f, y, paint)
            
            val file = File(context.cacheDir, "sip_result_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            
            getFileUri(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Export PPF calculation results as PDF
     */
    suspend fun exportPPFAsPDF(
        context: Context,
        annualContribution: Double,
        interestRate: Double,
        years: Int,
        result: PPFCalculator.PPFResult
    ): Uri? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            var y = 50f
            val paint = android.graphics.Paint()
            paint.textSize = 18f
            paint.isFakeBoldText = true
            
            canvas.drawText("PPF Calculator Results", 50f, y, paint)
            y += 40f
            
            paint.textSize = 12f
            paint.isFakeBoldText = false
            
            canvas.drawText("Annual Contribution: ${CurrencyFormatter.format(annualContribution)}", 50f, y, paint)
            y += 20f
            canvas.drawText("Interest Rate: $interestRate% p.a.", 50f, y, paint)
            y += 20f
            canvas.drawText("Tenure: $years years", 50f, y, paint)
            y += 30f
            
            paint.isFakeBoldText = true
            canvas.drawText("Maturity Amount: ${CurrencyFormatter.format(result.maturityAmount)}", 50f, y, paint)
            y += 25f
            paint.isFakeBoldText = false
            canvas.drawText("Total Contribution: ${CurrencyFormatter.format(result.totalContribution)}", 50f, y, paint)
            y += 20f
            canvas.drawText("Interest Earned: ${CurrencyFormatter.format(result.interestEarned)}", 50f, y, paint)
            
            pdfDocument.finishPage(page)
            
            val file = File(context.cacheDir, "ppf_result_${System.currentTimeMillis()}.pdf")
            FileOutputStream(file).use { fos ->
                pdfDocument.writeTo(fos)
            }
            pdfDocument.close()
            
            getFileUri(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Export PPF calculation results as Image
     */
    suspend fun exportPPFAsImage(
        context: Context,
        annualContribution: Double,
        interestRate: Double,
        years: Int,
        result: PPFCalculator.PPFResult
    ): Uri? {
        return try {
            val width = 800
            val height = 600
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(android.graphics.Color.WHITE)
            
            val paint = android.graphics.Paint()
            paint.color = android.graphics.Color.BLACK
            paint.textSize = 24f
            paint.isFakeBoldText = true
            
            var y = 50f
            canvas.drawText("PPF Calculator Results", 50f, y, paint)
            y += 40f
            
            paint.textSize = 16f
            paint.isFakeBoldText = false
            canvas.drawText("Annual Contribution: ${CurrencyFormatter.format(annualContribution)}", 50f, y, paint)
            y += 30f
            canvas.drawText("Rate: $interestRate% | Tenure: $years years", 50f, y, paint)
            y += 40f
            paint.isFakeBoldText = true
            paint.textSize = 20f
            canvas.drawText("Maturity: ${CurrencyFormatter.format(result.maturityAmount)}", 50f, y, paint)
            y += 30f
            paint.isFakeBoldText = false
            paint.textSize = 16f
            canvas.drawText("Interest: ${CurrencyFormatter.format(result.interestEarned)}", 50f, y, paint)
            
            val file = File(context.cacheDir, "ppf_result_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            
            getFileUri(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Export Lumpsum calculation results as PDF
     */
    suspend fun exportLumpsumAsPDF(
        context: Context,
        investmentAmount: Double,
        expectedReturnRate: Double,
        investmentPeriodYears: Double,
        result: LumpsumCalculator.LumpsumResult
    ): Uri? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            var y = 50f
            val paint = android.graphics.Paint()
            paint.textSize = 18f
            paint.isFakeBoldText = true
            
            canvas.drawText("Lumpsum Investment Calculator Results", 50f, y, paint)
            y += 40f
            
            paint.textSize = 12f
            paint.isFakeBoldText = false
            
            canvas.drawText("Investment Amount: ${CurrencyFormatter.format(investmentAmount)}", 50f, y, paint)
            y += 20f
            canvas.drawText("Expected Return: $expectedReturnRate% p.a.", 50f, y, paint)
            y += 20f
            canvas.drawText("Investment Period: $investmentPeriodYears years", 50f, y, paint)
            y += 30f
            
            paint.isFakeBoldText = true
            canvas.drawText("Future Value: ${CurrencyFormatter.format(result.futureValue)}", 50f, y, paint)
            y += 25f
            paint.isFakeBoldText = false
            canvas.drawText("Returns: ${CurrencyFormatter.format(result.returns)}", 50f, y, paint)
            y += 20f
            canvas.drawText("Absolute Return: ${String.format("%.2f", result.absoluteReturn)}%", 50f, y, paint)
            
            pdfDocument.finishPage(page)
            
            val file = File(context.cacheDir, "lumpsum_result_${System.currentTimeMillis()}.pdf")
            FileOutputStream(file).use { fos ->
                pdfDocument.writeTo(fos)
            }
            pdfDocument.close()
            
            getFileUri(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Export Lumpsum calculation results as Image
     */
    suspend fun exportLumpsumAsImage(
        context: Context,
        investmentAmount: Double,
        expectedReturnRate: Double,
        investmentPeriodYears: Double,
        result: LumpsumCalculator.LumpsumResult
    ): Uri? {
        return try {
            val width = 800
            val height = 600
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(android.graphics.Color.WHITE)
            
            val paint = android.graphics.Paint()
            paint.color = android.graphics.Color.BLACK
            paint.textSize = 24f
            paint.isFakeBoldText = true
            
            var y = 50f
            canvas.drawText("Lumpsum Investment Results", 50f, y, paint)
            y += 40f
            
            paint.textSize = 16f
            paint.isFakeBoldText = false
            canvas.drawText("Investment: ${CurrencyFormatter.format(investmentAmount)}", 50f, y, paint)
            y += 30f
            canvas.drawText("Return: $expectedReturnRate% | Period: $investmentPeriodYears years", 50f, y, paint)
            y += 40f
            paint.isFakeBoldText = true
            paint.textSize = 20f
            canvas.drawText("Future Value: ${CurrencyFormatter.format(result.futureValue)}", 50f, y, paint)
            y += 30f
            paint.isFakeBoldText = false
            paint.textSize = 16f
            canvas.drawText("Returns: ${CurrencyFormatter.format(result.returns)} (${String.format("%.2f", result.absoluteReturn)}%)", 50f, y, paint)
            
            val file = File(context.cacheDir, "lumpsum_result_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            
            getFileUri(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Export Simple Interest calculation results as PDF
     */
    suspend fun exportSimpleInterestAsPDF(
        context: Context,
        principal: Double,
        rate: Double,
        time: Double,
        timeUnit: SimpleInterestCalculator.TimeUnit,
        result: SimpleInterestCalculator.SimpleInterestResult
    ): Uri? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            var y = 50f
            val paint = android.graphics.Paint()
            paint.textSize = 18f
            paint.isFakeBoldText = true
            
            canvas.drawText("Simple Interest Calculator Results", 50f, y, paint)
            y += 40f
            
            paint.textSize = 12f
            paint.isFakeBoldText = false
            
            canvas.drawText("Principal: ${CurrencyFormatter.format(principal)}", 50f, y, paint)
            y += 20f
            canvas.drawText("Rate: $rate% p.a.", 50f, y, paint)
            y += 20f
            canvas.drawText("Time: $time ${timeUnit.name.lowercase()}", 50f, y, paint)
            y += 30f
            
            paint.isFakeBoldText = true
            canvas.drawText("Interest Amount: ${CurrencyFormatter.format(result.interestAmount)}", 50f, y, paint)
            y += 25f
            paint.isFakeBoldText = false
            canvas.drawText("Total Amount: ${CurrencyFormatter.format(result.totalAmount)}", 50f, y, paint)
            
            pdfDocument.finishPage(page)
            
            val file = File(context.cacheDir, "simple_interest_result_${System.currentTimeMillis()}.pdf")
            FileOutputStream(file).use { fos ->
                pdfDocument.writeTo(fos)
            }
            pdfDocument.close()
            
            getFileUri(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Export Simple Interest calculation results as Image
     */
    suspend fun exportSimpleInterestAsImage(
        context: Context,
        principal: Double,
        rate: Double,
        time: Double,
        timeUnit: SimpleInterestCalculator.TimeUnit,
        result: SimpleInterestCalculator.SimpleInterestResult
    ): Uri? {
        return try {
            val width = 800
            val height = 600
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(android.graphics.Color.WHITE)
            
            val paint = android.graphics.Paint()
            paint.color = android.graphics.Color.BLACK
            paint.textSize = 24f
            paint.isFakeBoldText = true
            
            var y = 50f
            canvas.drawText("Simple Interest Results", 50f, y, paint)
            y += 40f
            
            paint.textSize = 16f
            paint.isFakeBoldText = false
            canvas.drawText("Principal: ${CurrencyFormatter.format(principal)}", 50f, y, paint)
            y += 30f
            canvas.drawText("Rate: $rate% | Time: $time ${timeUnit.name.lowercase()}", 50f, y, paint)
            y += 40f
            paint.isFakeBoldText = true
            paint.textSize = 20f
            canvas.drawText("Interest: ${CurrencyFormatter.format(result.interestAmount)}", 50f, y, paint)
            y += 30f
            paint.isFakeBoldText = false
            paint.textSize = 16f
            canvas.drawText("Total: ${CurrencyFormatter.format(result.totalAmount)}", 50f, y, paint)
            
            val file = File(context.cacheDir, "simple_interest_result_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            
            getFileUri(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Export Compound Interest calculation results as PDF
     */
    suspend fun exportCompoundInterestAsPDF(
        context: Context,
        principal: Double,
        rate: Double,
        timeYears: Double,
        compoundingFrequency: CompoundInterestCalculator.CompoundingFrequency,
        result: CompoundInterestCalculator.CompoundInterestResult
    ): Uri? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            var y = 50f
            val paint = android.graphics.Paint()
            paint.textSize = 18f
            paint.isFakeBoldText = true
            
            canvas.drawText("Compound Interest Calculator Results", 50f, y, paint)
            y += 40f
            
            paint.textSize = 12f
            paint.isFakeBoldText = false
            
            canvas.drawText("Principal: ${CurrencyFormatter.format(principal)}", 50f, y, paint)
            y += 20f
            canvas.drawText("Rate: $rate% p.a.", 50f, y, paint)
            y += 20f
            canvas.drawText("Time: $timeYears years", 50f, y, paint)
            y += 20f
            canvas.drawText("Compounding: ${compoundingFrequency.name}", 50f, y, paint)
            y += 30f
            
            paint.isFakeBoldText = true
            canvas.drawText("Total Amount: ${CurrencyFormatter.format(result.totalAmount)}", 50f, y, paint)
            y += 25f
            paint.isFakeBoldText = false
            canvas.drawText("Compound Interest: ${CurrencyFormatter.format(result.compoundInterest)}", 50f, y, paint)
            
            pdfDocument.finishPage(page)
            
            val file = File(context.cacheDir, "compound_interest_result_${System.currentTimeMillis()}.pdf")
            FileOutputStream(file).use { fos ->
                pdfDocument.writeTo(fos)
            }
            pdfDocument.close()
            
            getFileUri(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Export Compound Interest calculation results as Image
     */
    suspend fun exportCompoundInterestAsImage(
        context: Context,
        principal: Double,
        rate: Double,
        timeYears: Double,
        compoundingFrequency: CompoundInterestCalculator.CompoundingFrequency,
        result: CompoundInterestCalculator.CompoundInterestResult
    ): Uri? {
        return try {
            val width = 800
            val height = 600
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(android.graphics.Color.WHITE)
            
            val paint = android.graphics.Paint()
            paint.color = android.graphics.Color.BLACK
            paint.textSize = 24f
            paint.isFakeBoldText = true
            
            var y = 50f
            canvas.drawText("Compound Interest Results", 50f, y, paint)
            y += 40f
            
            paint.textSize = 16f
            paint.isFakeBoldText = false
            canvas.drawText("Principal: ${CurrencyFormatter.format(principal)}", 50f, y, paint)
            y += 30f
            canvas.drawText("Rate: $rate% | Time: $timeYears years", 50f, y, paint)
            y += 40f
            paint.isFakeBoldText = true
            paint.textSize = 20f
            canvas.drawText("Total: ${CurrencyFormatter.format(result.totalAmount)}", 50f, y, paint)
            y += 30f
            paint.isFakeBoldText = false
            paint.textSize = 16f
            canvas.drawText("Interest: ${CurrencyFormatter.format(result.compoundInterest)}", 50f, y, paint)
            
            val file = File(context.cacheDir, "compound_interest_result_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            
            getFileUri(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Get file URI for sharing
     */
    private fun getFileUri(context: Context, file: File): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } else {
            Uri.fromFile(file)
        }
    }
    
    /**
     * Share file via intent
     */
    fun shareFile(context: Context, uri: Uri, mimeType: String, title: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, title))
    }
}

