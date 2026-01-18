package com.kholhang.fynical.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontWeight
import com.kholhang.fynical.R
import com.kholhang.fynical.ui.components.ShareOptionsDialog
import com.kholhang.fynical.ui.components.TableCell
import com.kholhang.fynical.utils.AmortizationCalculator
import com.kholhang.fynical.utils.CurrencyFormatter
import com.kholhang.fynical.utils.EMICalculator
import com.kholhang.fynical.utils.ResultExporter
import com.kholhang.fynical.utils.showToast
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EMIScreen(navController: NavController? = null) {
    var principal by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("") }
    var tenureMonths by remember { mutableStateOf("") }
    
    var emi by remember { mutableStateOf(0.0) }
    var totalAmount by remember { mutableStateOf(0.0) }
    var totalInterest by remember { mutableStateOf(0.0) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showAmortizationSchedule by remember { mutableStateOf(false) }
    var selectedView by remember { mutableStateOf(0) } // 0 = Monthly, 1 = Yearly
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.emi_calculator)) },
                navigationIcon = {
                    if (navController != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    }
                },
                actions = {
                    if (emi > 0) {
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = principal,
                onValueChange = { principal = it },
                label = { Text(stringResource(R.string.principal_amount)) },
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
                label = { Text(stringResource(R.string.interest_rate)) },
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
            
            OutlinedTextField(
                value = tenureMonths,
                onValueChange = { tenureMonths = it },
                label = { Text(stringResource(R.string.loan_tenure)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            
            Button(
                onClick = {
                    try {
                        if (principal.isBlank()) {
                            showToast(context, "Please enter principal amount")
                            return@Button
                        }
                        if (interestRate.isBlank()) {
                            showToast(context, "Please enter interest rate")
                            return@Button
                        }
                        if (tenureMonths.isBlank()) {
                            showToast(context, "Please enter loan tenure")
                            return@Button
                        }
                        
                        val p = principal.toDoubleOrNull()
                        val r = interestRate.toDoubleOrNull()
                        val t = tenureMonths.toIntOrNull()
                        
                        if (p == null || p <= 0) {
                            showToast(context, "Please enter a valid principal amount")
                            return@Button
                        }
                        if (r == null || r <= 0 || r > 100) {
                            showToast(context, "Please enter a valid interest rate (0-100%)")
                            return@Button
                        }
                        if (t == null || t <= 0) {
                            showToast(context, "Please enter a valid loan tenure (in months)")
                            return@Button
                        }
                        
                        emi = EMICalculator.calculateEMI(p, r, t)
                        totalAmount = EMICalculator.calculateTotalAmount(emi, t)
                        totalInterest = EMICalculator.calculateTotalInterest(p, totalAmount)
                    } catch (e: Exception) {
                        showToast(context, "Error calculating EMI: ${e.message}")
                        emi = 0.0
                        totalAmount = 0.0
                        totalInterest = 0.0
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(stringResource(R.string.calculate), modifier = Modifier.padding(8.dp))
            }
            
            if (emi > 0) {
                val schedule = remember(principal, interestRate, tenureMonths) {
                    val p = principal.toDoubleOrNull() ?: 0.0
                    val r = interestRate.toDoubleOrNull() ?: 0.0
                    val t = tenureMonths.toIntOrNull() ?: 0
                    if (p > 0 && r >= 0 && t > 0) {
                        AmortizationCalculator.calculateSchedule(p, r, t)
                    } else null
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.results),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Divider()
                        
                        ResultRow(
                            label = stringResource(R.string.monthly_emi),
                            value = currencyFormatter.format(emi)
                        )
                        
                        ResultRow(
                            label = stringResource(R.string.total_amount),
                            value = currencyFormatter.format(totalAmount)
                        )
                        
                        ResultRow(
                            label = stringResource(R.string.total_interest),
                            value = currencyFormatter.format(totalInterest)
                        )
                        
                        Divider()
                        
                        Button(
                            onClick = { showAmortizationSchedule = !showAmortizationSchedule },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (showAmortizationSchedule) "Hide Amortization Schedule" else "Show Amortization Schedule")
                        }
                    }
                }
                
                if (showAmortizationSchedule && schedule != null) {
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
                                text = "Amortization Schedule",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = selectedView == 0,
                                    onClick = { selectedView = 0 },
                                    label = { Text("Monthly") },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = selectedView == 1,
                                    onClick = { selectedView = 1 },
                                    label = { Text("Yearly") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            if (selectedView == 0) {
                                // Monthly Schedule (first 12 months)
                                MonthlyAmortizationTable(schedule.entries.take(12))
                            } else {
                                // Yearly Summary
                                val yearlySummary = AmortizationCalculator.getYearlySummary(schedule)
                                YearlyAmortizationTable(yearlySummary)
                            }
                        }
                    }
                }
            }
            
            if (showShareDialog) {
                ShareOptionsDialog(
                    onDismiss = { showShareDialog = false },
                    onSharePDF = {
                        showShareDialog = false
                        coroutineScope.launch {
                            val p = principal.toDoubleOrNull() ?: 0.0
                            val r = interestRate.toDoubleOrNull() ?: 0.0
                            val t = tenureMonths.toIntOrNull() ?: 0
                            if (p > 0 && r > 0 && t > 0) {
                                val calculatedEMI = EMICalculator.calculateEMI(p, r, t)
                                val calculatedTotalAmount = EMICalculator.calculateTotalAmount(calculatedEMI, t)
                                val calculatedTotalInterest = EMICalculator.calculateTotalInterest(p, calculatedTotalAmount)
                                ResultExporter.exportEMIAsPDF(context, p, r, t, calculatedEMI, calculatedTotalAmount, calculatedTotalInterest)?.let { uri ->
                                    ResultExporter.shareFile(context, uri, "application/pdf", context.getString(R.string.share_pdf))
                                }
                            }
                        }
                    },
                    onShareImage = {
                        showShareDialog = false
                        coroutineScope.launch {
                            val p = principal.toDoubleOrNull() ?: 0.0
                            val r = interestRate.toDoubleOrNull() ?: 0.0
                            val t = tenureMonths.toIntOrNull() ?: 0
                            if (p > 0 && r > 0 && t > 0) {
                                val calculatedEMI = EMICalculator.calculateEMI(p, r, t)
                                val calculatedTotalAmount = EMICalculator.calculateTotalAmount(calculatedEMI, t)
                                val calculatedTotalInterest = EMICalculator.calculateTotalInterest(p, calculatedTotalAmount)
                                ResultExporter.exportEMIAsImage(context, p, r, t, calculatedEMI, calculatedTotalAmount, calculatedTotalInterest)?.let { uri ->
                                    ResultExporter.shareFile(context, uri, "image/png", context.getString(R.string.share_image))
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun MonthlyAmortizationTable(entries: List<AmortizationCalculator.AmortizationEntry>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            TableCell(text = "Month", weight = 0.5f, header = true)
            TableCell(text = "Start Bal", weight = 1f, header = true)
            TableCell(text = "EMI", weight = 0.8f, header = true)
            TableCell(text = "Principal", weight = 0.8f, header = true)
            TableCell(text = "Interest", weight = 0.8f, header = true)
            TableCell(text = "End Bal", weight = 1f, header = true)
        }
        Divider()
        entries.forEach { entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                TableCell(text = entry.month.toString(), weight = 0.5f)
                TableCell(text = CurrencyFormatter.format(entry.beginningBalance), weight = 1f)
                TableCell(text = CurrencyFormatter.format(entry.emi), weight = 0.8f)
                TableCell(text = CurrencyFormatter.format(entry.principalPayment), weight = 0.8f)
                TableCell(text = CurrencyFormatter.format(entry.interestPayment), weight = 0.8f)
                TableCell(text = CurrencyFormatter.format(entry.endingBalance), weight = 1f)
            }
            Divider()
        }
    }
}

@Composable
fun YearlyAmortizationTable(summary: List<AmortizationCalculator.YearlySummary>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            TableCell(text = "Year", weight = 0.5f, header = true)
            TableCell(text = "Start Bal", weight = 1f, header = true)
            TableCell(text = "Principal", weight = 1f, header = true)
            TableCell(text = "Interest", weight = 1f, header = true)
            TableCell(text = "End Bal", weight = 1f, header = true)
        }
        Divider()
        var previousBalance = 0.0
        summary.forEach { entry ->
            val startingBalance = if (entry.year == 1) {
                // For first year, starting balance = total paid + remaining balance
                entry.totalPaid + entry.remainingBalance
            } else {
                previousBalance
            }
            previousBalance = entry.remainingBalance
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                TableCell(text = entry.year.toString(), weight = 0.5f)
                TableCell(text = CurrencyFormatter.format(startingBalance), weight = 1f)
                TableCell(text = CurrencyFormatter.format(entry.totalPrincipalPaid), weight = 1f)
                TableCell(text = CurrencyFormatter.format(entry.totalInterestPaid), weight = 1f)
                TableCell(text = CurrencyFormatter.format(entry.remainingBalance), weight = 1f)
            }
            Divider()
        }
    }
}


