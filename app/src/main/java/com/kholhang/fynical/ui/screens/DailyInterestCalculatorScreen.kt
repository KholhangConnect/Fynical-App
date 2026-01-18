package com.kholhang.fynical.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kholhang.fynical.R
import com.kholhang.fynical.ui.components.TableCell
import com.kholhang.fynical.utils.CurrencyFormatter
import com.kholhang.fynical.utils.DailyInterestCalculator
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyInterestCalculatorScreen(navController: NavController? = null) {
    var openingBalance by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var transactionDate by remember { mutableStateOf("") }
    var transactionAmount by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf(true) } // true = deposit, false = withdrawal
    var transactionDescription by remember { mutableStateOf("") }
    
    var transactions by remember { mutableStateOf<List<DailyInterestCalculator.Transaction>>(emptyList()) }
    var result by remember { mutableStateOf<DailyInterestCalculator.InterestResult?>(null) }
    var showTransactionDialog by remember { mutableStateOf(false) }
    var showDailyBreakdown by remember { mutableStateOf(false) }
    var showBeforeAfter by remember { mutableStateOf(false) }
    
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Interest Calculator") },
                navigationIcon = {
                    if (navController != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Input Fields
            OutlinedTextField(
                value = openingBalance,
                onValueChange = { openingBalance = it },
                label = { Text("Opening Balance (₹)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = {
                    Text("₹", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
                }
            )
            
            OutlinedTextField(
                value = interestRate,
                onValueChange = { interestRate = it },
                label = { Text("Interest Rate (% per annum)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("%", modifier = Modifier.padding(end = 8.dp)) }
            )
            
            OutlinedTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = { Text("Start Date (dd/MM/yyyy)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("01/01/2024") }
            )
            
            OutlinedTextField(
                value = endDate,
                onValueChange = { endDate = it },
                label = { Text("End Date (dd/MM/yyyy)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("31/01/2024") }
            )
            
            Divider()
            
            // Transaction Section
            Text(
                text = "Transactions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (transactions.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        transactions.forEachIndexed { index, transaction ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dateFormat.format(transaction.date),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = if (transaction.amount > 0) "Deposit" else "Withdrawal",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (transaction.amount > 0) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.error
                                    )
                                    if (transaction.description.isNotEmpty()) {
                                        Text(
                                            text = transaction.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                                Text(
                                    text = CurrencyFormatter.format(transaction.amount),
                                    fontWeight = FontWeight.Bold,
                                    color = if (transaction.amount > 0) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.error
                                )
                                IconButton(
                                    onClick = {
                                        transactions = transactions.filterIndexed { i, _ -> i != index }
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                            if (index < transactions.size - 1) Divider()
                        }
                    }
                }
            }
            
            Button(
                onClick = { showTransactionDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Add Transaction")
            }
            
            // Calculate Button
            Button(
                onClick = {
                    try {
                        val balance = openingBalance.toDoubleOrNull() ?: 0.0
                        val rate = interestRate.toDoubleOrNull() ?: 0.0
                        val start = dateFormat.parse(startDate) ?: Date()
                        val end = dateFormat.parse(endDate) ?: Date()
                        
                        if (balance >= 0 && rate >= 0 && start.before(end) || start == end) {
                            result = DailyInterestCalculator.calculateDailyInterest(
                                openingBalance = balance,
                                interestRate = rate,
                                startDate = start,
                                endDate = end,
                                transactions = transactions
                            )
                        }
                    } catch (e: Exception) {
                        // Handle error
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Calculate Interest")
            }
            
            // Results
            if (result != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Results",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Divider()
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Interest:")
                            Text(
                                CurrencyFormatter.format(result!!.totalInterest),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Days:")
                            Text("${result!!.totalDays}")
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Opening Balance:")
                            Text(CurrencyFormatter.format(result!!.openingBalance))
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Closing Balance:")
                            Text(CurrencyFormatter.format(result!!.closingBalance))
                        }
                        
                        Divider()
                        
                        Button(
                            onClick = { showDailyBreakdown = !showDailyBreakdown },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (showDailyBreakdown) "Hide Daily Breakdown" else "Show Daily Breakdown")
                        }
                        
                        if (transactions.isNotEmpty()) {
                            Button(
                                onClick = { showBeforeAfter = !showBeforeAfter },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (showBeforeAfter) "Hide Before/After Analysis" else "Show Before/After Analysis")
                            }
                        }
                    }
                }
                
                // Daily Breakdown
                if (showDailyBreakdown && result != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Daily Breakdown",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            // Show first 30 days, then allow scrolling
                            val entriesToShow = result!!.dailyEntries.take(30)
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                TableCell(text = "Date", weight = 1f, header = true)
                                TableCell(text = "Open", weight = 0.8f, header = true)
                                TableCell(text = "Trans", weight = 0.8f, header = true)
                                TableCell(text = "Close", weight = 0.8f, header = true)
                                TableCell(text = "Interest", weight = 0.8f, header = true)
                            }
                            Divider()
                            
                            entriesToShow.forEach { entry ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    TableCell(text = dateFormat.format(entry.date), weight = 1f)
                                    TableCell(text = CurrencyFormatter.format(entry.openingBalance), weight = 0.8f)
                                    TableCell(
                                        text = if (entry.transaction != 0.0) CurrencyFormatter.format(entry.transaction) else "-",
                                        weight = 0.8f
                                    )
                                    TableCell(text = CurrencyFormatter.format(entry.closingBalance), weight = 0.8f)
                                    TableCell(text = CurrencyFormatter.format(entry.interestForDay), weight = 0.8f)
                                }
                                Divider()
                            }
                            
                            if (result!!.dailyEntries.size > 30) {
                                Text(
                                    text = "Showing first 30 days. Total: ${result!!.dailyEntries.size} days",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
                
                // Before/After Analysis
                if (showBeforeAfter && transactions.isNotEmpty() && result != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Interest Before/After Transactions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            transactions.forEach { transaction ->
                                val interestBefore = DailyInterestCalculator.calculateInterestBeforeTransaction(
                                    openingBalance = result!!.openingBalance,
                                    interestRate = result!!.dailyEntries.first().interestRate,
                                    startDate = result!!.dailyEntries.first().date,
                                    transactionDate = transaction.date,
                                    transactionsBefore = transactions.filter { it.date.before(transaction.date) }
                                )
                                
                                val balanceAfter = result!!.dailyEntries
                                    .firstOrNull { 
                                        val cal1 = Calendar.getInstance().apply { time = it.date }
                                        val cal2 = Calendar.getInstance().apply { time = transaction.date }
                                        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                                        cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
                                    }?.closingBalance ?: result!!.openingBalance
                                
                                val interestAfter = DailyInterestCalculator.calculateInterestAfterTransaction(
                                    balanceAfterTransaction = balanceAfter,
                                    interestRate = result!!.dailyEntries.first().interestRate,
                                    transactionDate = transaction.date,
                                    endDate = result!!.dailyEntries.last().date,
                                    transactionsAfter = transactions.filter { it.date.after(transaction.date) }
                                )
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = dateFormat.format(transaction.date),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (transaction.amount > 0) "Deposit" else "Withdrawal",
                                            color = if (transaction.amount > 0) 
                                                MaterialTheme.colorScheme.primary 
                                            else 
                                                MaterialTheme.colorScheme.error
                                        )
                                        Text(
                                            text = "Amount: ${CurrencyFormatter.format(transaction.amount)}"
                                        )
                                        Divider()
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Interest Before:")
                                            Text(CurrencyFormatter.format(interestBefore))
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Interest After:")
                                            Text(CurrencyFormatter.format(interestAfter))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Transaction Dialog
    if (showTransactionDialog) {
        AlertDialog(
            onDismissRequest = { showTransactionDialog = false },
            title = { Text("Add Transaction") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = transactionDate,
                        onValueChange = { transactionDate = it },
                        label = { Text("Date (dd/MM/yyyy)") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("15/01/2024") }
                    )
                    
                    OutlinedTextField(
                        value = transactionAmount,
                        onValueChange = { transactionAmount = it },
                        label = { Text("Amount (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = {
                            Text("₹", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
                        }
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = transactionType,
                            onClick = { transactionType = true },
                            label = { Text("Deposit") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !transactionType,
                            onClick = { transactionType = false },
                            label = { Text("Withdrawal") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    OutlinedTextField(
                        value = transactionDescription,
                        onValueChange = { transactionDescription = it },
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            val date = dateFormat.parse(transactionDate) ?: Date()
                            val amount = transactionAmount.toDoubleOrNull() ?: 0.0
                            val finalAmount = if (transactionType) amount else -amount
                            
                            if (amount > 0) {
                                transactions = transactions + DailyInterestCalculator.Transaction(
                                    date = date,
                                    amount = finalAmount,
                                    description = transactionDescription
                                )
                                transactionDate = ""
                                transactionAmount = ""
                                transactionDescription = ""
                                transactionType = true
                                showTransactionDialog = false
                            }
                        } catch (e: Exception) {
                            // Handle error
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransactionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

