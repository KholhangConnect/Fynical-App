package com.kholhang.fynical.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
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
import com.kholhang.fynical.ui.components.ShareOptionsDialog
import com.kholhang.fynical.ui.components.TableCell
import com.kholhang.fynical.utils.CurrencyFormatter
import com.kholhang.fynical.utils.DailyInterestCalculator
import com.kholhang.fynical.utils.showToast
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyInterestScreen(navController: NavController? = null) {
    var openingBalance by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("") }
    var startDateText by remember { mutableStateOf("") }
    var endDateText by remember { mutableStateOf("") }
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showDailyBreakdown by remember { mutableStateOf(false) }
    
    val transactions = remember { mutableStateListOf<DailyInterestCalculator.Transaction>() }
    var result by remember { mutableStateOf<DailyInterestCalculator.InterestResult?>(null) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    
    // Initialize dates to today
    LaunchedEffect(Unit) {
        val today = dateFormatter.format(Date())
        startDateText = today
        endDateText = today
    }
    
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
                actions = {
                    if (result != null) {
                        IconButton(onClick = { showShareDialog = true }) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.share))
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = openingBalance,
                onValueChange = { openingBalance = it },
                label = { Text("Opening Balance") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                leadingIcon = {
                    Text(
                        text = "₹",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            )
            
            OutlinedTextField(
                value = interestRate,
                onValueChange = { interestRate = it },
                label = { Text("Interest Rate (% per annum)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                suffix = {
                    Text(
                        text = "%",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = startDateText,
                    onValueChange = { startDateText = it },
                    label = { Text("Start Date (DD/MM/YYYY)") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("01/01/2024") }
                )
                
                OutlinedTextField(
                    value = endDateText,
                    onValueChange = { endDateText = it },
                    label = { Text("End Date (DD/MM/YYYY)") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("31/12/2024") }
                )
            }
            
            // Transactions Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Transactions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showAddTransactionDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                        }
                    }
                    
                    if (transactions.isEmpty()) {
                        Text(
                            "No transactions added",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    } else {
                        transactions.forEachIndexed { index, transaction ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        dateFormatter.format(transaction.date),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        "${if (transaction.amount >= 0) "+" else ""}${CurrencyFormatter.format(transaction.amount)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (transaction.amount >= 0) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.error
                                    )
                                    if (transaction.description.isNotEmpty()) {
                                        Text(
                                            transaction.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                                IconButton(onClick = { transactions.removeAt(index) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            if (index < transactions.size - 1) Divider()
                        }
                    }
                }
            }
            
            Button(
                onClick = {
                    if (openingBalance.isBlank()) {
                        showToast(context, "Please enter opening balance")
                        return@Button
                    }
                    if (interestRate.isBlank()) {
                        showToast(context, "Please enter interest rate")
                        return@Button
                    }
                    if (startDateText.isBlank()) {
                        showToast(context, "Please enter start date")
                        return@Button
                    }
                    if (endDateText.isBlank()) {
                        showToast(context, "Please enter end date")
                        return@Button
                    }
                    
                    val balance = openingBalance.toDoubleOrNull()
                    val rate = interestRate.toDoubleOrNull()
                    
                    if (balance == null || balance < 0) {
                        showToast(context, "Please enter a valid opening balance")
                        return@Button
                    }
                    if (rate == null || rate < 0 || rate > 100) {
                        showToast(context, "Please enter a valid interest rate (0-100%)")
                        return@Button
                    }
                    
                    try {
                        val startDate = dateFormatter.parse(startDateText)
                        val endDate = dateFormatter.parse(endDateText)
                        
                        if (startDate == null) {
                            showToast(context, "Please enter a valid start date (DD/MM/YYYY)")
                            return@Button
                        }
                        if (endDate == null) {
                            showToast(context, "Please enter a valid end date (DD/MM/YYYY)")
                            return@Button
                        }
                        if (startDate.after(endDate)) {
                            showToast(context, "Start date must be before or equal to end date")
                            return@Button
                        }
                        
                        result = DailyInterestCalculator.calculateDailyInterest(
                            balance,
                            rate,
                            startDate,
                            endDate,
                            transactions.toList()
                        )
                    } catch (e: Exception) {
                        showToast(context, "Invalid date format. Please use DD/MM/YYYY")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Calculate Interest")
            }
            
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
                            "Results",
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
                            Text("Final Balance:")
                            Text(
                                CurrencyFormatter.format(result!!.closingBalance),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Days:")
                            Text("${result!!.totalDays}")
                        }
                        
                        Divider()
                        
                        Button(
                            onClick = { showDailyBreakdown = !showDailyBreakdown },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (showDailyBreakdown) "Hide Daily Breakdown" else "Show Daily Breakdown")
                        }
                    }
                }
                
                if (showDailyBreakdown && result!!.dailyEntries.isNotEmpty()) {
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
                                "Daily Breakdown",
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
                                TableCell(text = "Opening", weight = 0.8f, header = true)
                                TableCell(text = "Transactions", weight = 0.8f, header = true)
                                TableCell(text = "Closing", weight = 0.8f, header = true)
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
                                    TableCell(
                                        text = dateFormatter.format(entry.date),
                                        weight = 1f
                                    )
                                    TableCell(
                                        text = CurrencyFormatter.format(entry.openingBalance),
                                        weight = 0.8f
                                    )
                                    TableCell(
                                        text = if (entry.transaction != 0.0) {
                                            CurrencyFormatter.format(entry.transaction)
                                        } else {
                                            "-"
                                        },
                                        weight = 0.8f
                                    )
                                    TableCell(
                                        text = CurrencyFormatter.format(entry.closingBalance),
                                        weight = 0.8f
                                    )
                                    TableCell(
                                        text = CurrencyFormatter.format(entry.interestForDay),
                                        weight = 0.8f
                                    )
                                }
                                Divider()
                            }
                            
                            if (result!!.dailyEntries.size > 30) {
                                Text(
                                    "Showing first 30 days of ${result!!.dailyEntries.size} total days",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
            
            if (showShareDialog && result != null) {
                ShareOptionsDialog(
                    onDismiss = { showShareDialog = false },
                    onSharePDF = {
                        showShareDialog = false
                        // TODO: Implement PDF export
                    },
                    onShareImage = {
                        showShareDialog = false
                        // TODO: Implement Image export
                    }
                )
            }
        }
    }
    
    // Add Transaction Dialog
    if (showAddTransactionDialog) {
        AddTransactionDialog(
            onDismiss = { showAddTransactionDialog = false },
            onAdd = { transaction ->
                transactions.add(transaction)
                showAddTransactionDialog = false
            }
        )
    }
}

@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onAdd: (DailyInterestCalculator.Transaction) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDateText by remember { mutableStateOf("") }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    
    LaunchedEffect(Unit) {
        selectedDateText = dateFormatter.format(Date())
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Transaction") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = {
                        Text(
                            text = "₹",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    },
                    placeholder = { Text("Positive for credit, negative for debit") }
                )
                
                OutlinedTextField(
                    value = selectedDateText,
                    onValueChange = { selectedDateText = it },
                    label = { Text("Date (DD/MM/YYYY)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("01/01/2024") }
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    try {
                        val date = dateFormatter.parse(selectedDateText) ?: Date()
                        if (amt != 0.0) {
                            onAdd(
                                DailyInterestCalculator.Transaction(
                                    date = date,
                                    amount = amt,
                                    description = description
                                )
                            )
                        }
                    } catch (e: Exception) {
                        // Invalid date format
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

