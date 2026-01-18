package com.kholhang.fynical.utils

import java.util.*

object DailyInterestCalculator {
    data class Transaction(
        val date: Date,
        val amount: Double, // Positive for deposits, negative for withdrawals
        val description: String = ""
    )
    
    data class DailyInterestEntry(
        val date: Date,
        val openingBalance: Double,
        val transaction: Double,
        val closingBalance: Double,
        val interestRate: Double,
        val interestForDay: Double
    )
    
    data class InterestResult(
        val totalInterest: Double,
        val totalDays: Int,
        val dailyEntries: List<DailyInterestEntry>,
        val openingBalance: Double,
        val closingBalance: Double
    )
    
    /**
     * Calculate daily interest for cash credit/Mudra loan
     * Interest is calculated on daily closing balance
     * Formula: Interest = (Balance × Rate × Days) / (100 × 365)
     */
    fun calculateDailyInterest(
        openingBalance: Double,
        interestRate: Double,
        startDate: Date,
        endDate: Date,
        transactions: List<Transaction> = emptyList()
    ): InterestResult {
        if (openingBalance < 0 || interestRate < 0) {
            return InterestResult(0.0, 0, emptyList(), openingBalance, openingBalance)
        }
        
        val calendar = Calendar.getInstance()
        val dailyEntries = mutableListOf<DailyInterestEntry>()
        
        // Sort transactions by date
        val sortedTransactions = transactions.sortedBy { it.date }
        
        var currentBalance = openingBalance
        var totalInterest = 0.0
        var currentDate = startDate
        
        calendar.time = startDate
        val endCalendar = Calendar.getInstance().apply { time = endDate }
        
        // Process each day
        while (calendar.before(endCalendar) || calendar.time == endDate) {
            val dayStart = calendar.time
            val openingBalanceForDay = currentBalance
            
            // Apply transactions for this day
            val dayTransactions = sortedTransactions.filter { 
                val transCal = Calendar.getInstance().apply { time = it.date }
                transCal.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                transCal.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
            }
            
            var dayTransaction = 0.0
            dayTransactions.forEach { transaction ->
                dayTransaction += transaction.amount
                currentBalance += transaction.amount
            }
            
            val closingBalanceForDay = currentBalance
            
            // Calculate interest for the day
            // Interest = (Balance × Rate) / (100 × 365)
            val dailyRate = interestRate / 100.0 / 365.0
            val interestForDay = closingBalanceForDay * dailyRate
            
            // Only add interest if balance is positive (for loans, negative balance means overdraft)
            val actualInterest = if (closingBalanceForDay > 0) interestForDay else 0.0
            totalInterest += actualInterest
            
            dailyEntries.add(
                DailyInterestEntry(
                    date = dayStart,
                    openingBalance = Math.round(openingBalanceForDay * 100.0) / 100.0,
                    transaction = Math.round(dayTransaction * 100.0) / 100.0,
                    closingBalance = Math.round(closingBalanceForDay * 100.0) / 100.0,
                    interestRate = interestRate,
                    interestForDay = Math.round(actualInterest * 100.0) / 100.0
                )
            )
            
            // Move to next day
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        val totalDays = dailyEntries.size
        
        return InterestResult(
            totalInterest = Math.round(totalInterest * 100.0) / 100.0,
            totalDays = totalDays,
            dailyEntries = dailyEntries,
            openingBalance = openingBalance,
            closingBalance = Math.round(currentBalance * 100.0) / 100.0
        )
    }
    
    /**
     * Calculate interest before a specific transaction date
     */
    fun calculateInterestBeforeTransaction(
        openingBalance: Double,
        interestRate: Double,
        startDate: Date,
        transactionDate: Date,
        transactionsBefore: List<Transaction> = emptyList()
    ): Double {
        val result = calculateDailyInterest(
            openingBalance = openingBalance,
            interestRate = interestRate,
            startDate = startDate,
            endDate = transactionDate,
            transactions = transactionsBefore
        )
        return result.totalInterest
    }
    
    /**
     * Calculate interest after a specific transaction date
     */
    fun calculateInterestAfterTransaction(
        balanceAfterTransaction: Double,
        interestRate: Double,
        transactionDate: Date,
        endDate: Date,
        transactionsAfter: List<Transaction> = emptyList()
    ): Double {
        val result = calculateDailyInterest(
            openingBalance = balanceAfterTransaction,
            interestRate = interestRate,
            startDate = transactionDate,
            endDate = endDate,
            transactions = transactionsAfter
        )
        return result.totalInterest
    }
}
