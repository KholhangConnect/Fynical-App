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
import com.kholhang.fynical.ui.components.ShareOptionsDialog
import com.kholhang.fynical.ui.components.TableCell
import com.kholhang.fynical.utils.CurrencyFormatter
import com.kholhang.fynical.utils.ResultExporter
import com.kholhang.fynical.utils.SIPCalculator
import com.kholhang.fynical.utils.showToast
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SIPCalculatorScreen(navController: NavController? = null) {
    var monthlyInvestment by remember { mutableStateOf("") }
    var annualRate by remember { mutableStateOf("") }
    var tenureMonths by remember { mutableStateOf("") }
    
    var showShareDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val result = remember(monthlyInvestment, annualRate, tenureMonths) {
        try {
            if (monthlyInvestment.isBlank() || annualRate.isBlank() || tenureMonths.isBlank()) {
                return@remember null
            }
            
            val investment = monthlyInvestment.toDoubleOrNull()
            val rate = annualRate.toDoubleOrNull()
            val months = tenureMonths.toIntOrNull()
            
            if (investment == null || investment <= 0) {
                return@remember null
            }
            if (rate == null || rate < 0 || rate > 100) {
                return@remember null
            }
            if (months == null || months <= 0) {
                return@remember null
            }
            
            SIPCalculator.calculateSIPMaturity(investment, rate, months)
        } catch (e: Exception) {
            null
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sip_calculator)) },
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
                value = monthlyInvestment,
                onValueChange = { monthlyInvestment = it },
                label = { Text(stringResource(R.string.monthly_investment)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Default.AccountCircle, null) },
                singleLine = true
            )
            
            OutlinedTextField(
                value = annualRate,
                onValueChange = { annualRate = it },
                label = { Text(stringResource(R.string.expected_annual_return)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Default.AccountCircle, null) },
                suffix = { Text("%") },
                singleLine = true
            )
            
            OutlinedTextField(
                value = tenureMonths,
                onValueChange = { tenureMonths = it },
                label = { Text(stringResource(R.string.investment_tenure)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Icon(Icons.Default.Settings, null) },
                suffix = { Text(stringResource(R.string.months)) },
                singleLine = true
            )
            
            Button(
                onClick = {
                    if (monthlyInvestment.isBlank()) {
                        showToast(context, "Please enter monthly investment")
                        return@Button
                    }
                    if (annualRate.isBlank()) {
                        showToast(context, "Please enter expected annual return")
                        return@Button
                    }
                    if (tenureMonths.isBlank()) {
                        showToast(context, "Please enter investment tenure")
                        return@Button
                    }
                    
                    val investment = monthlyInvestment.toDoubleOrNull()
                    val rate = annualRate.toDoubleOrNull()
                    val months = tenureMonths.toIntOrNull()
                    
                    if (investment == null || investment <= 0) {
                        showToast(context, "Please enter a valid monthly investment")
                        return@Button
                    }
                    if (rate == null || rate < 0 || rate > 100) {
                        showToast(context, "Please enter a valid annual return rate (0-100%)")
                        return@Button
                    }
                    if (months == null || months <= 0) {
                        showToast(context, "Please enter a valid investment tenure (in months)")
                        return@Button
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.calculate))
            }
            
            var showYearlyBreakdown by remember { mutableStateOf(false) }
            
            if (result != null) {
                val yearlyBreakdown = remember(monthlyInvestment, annualRate, tenureMonths) {
                    val investment = monthlyInvestment.toDoubleOrNull() ?: 0.0
                    val rate = annualRate.toDoubleOrNull() ?: 0.0
                    val months = tenureMonths.toIntOrNull() ?: 0
                    if (investment > 0 && rate >= 0 && months > 0 && months >= 12) {
                        SIPCalculator.calculateYearlyBreakdown(investment, rate, months)
                    } else null
                }
                
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                stringResource(R.string.maturity_amount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                CurrencyFormatter.format(result.maturityAmount),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Divider()
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.total_investment))
                            Text(CurrencyFormatter.format(result.totalInvestment))
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.estimated_returns))
                            Text(
                                CurrencyFormatter.format(result.returns),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.return_percentage))
                            Text(
                                if (result.totalInvestment > 0) {
                                    "${String.format("%.2f", (result.returns / result.totalInvestment) * 100)}%"
                                } else {
                                    "0.00%"
                                },
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        if (yearlyBreakdown != null && yearlyBreakdown.isNotEmpty()) {
                            Divider()
                            Button(
                                onClick = { showYearlyBreakdown = !showYearlyBreakdown },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (showYearlyBreakdown) "Hide Yearly Breakdown" else "Show Yearly Breakdown")
                            }
                        }
                    }
                }
                
                if (showYearlyBreakdown && yearlyBreakdown != null && yearlyBreakdown.isNotEmpty()) {
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
                                text = "Yearly Breakdown",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                TableCell(text = "Year", weight = 0.5f, header = true)
                                TableCell(text = "Investment", weight = 1f, header = true)
                                TableCell(text = "Interest", weight = 1f, header = true)
                                TableCell(text = "Amount", weight = 1f, header = true)
                            }
                            Divider()
                            yearlyBreakdown.forEach { entry ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    TableCell(text = entry.year.toString(), weight = 0.5f)
                                    TableCell(text = CurrencyFormatter.format(entry.investment), weight = 1f)
                                    TableCell(text = CurrencyFormatter.format(entry.interestEarned), weight = 1f)
                                    TableCell(text = CurrencyFormatter.format(entry.amountAtEnd), weight = 1f)
                                }
                                Divider()
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
                        coroutineScope.launch {
                            val investment = monthlyInvestment.toDoubleOrNull() ?: 0.0
                            val rate = annualRate.toDoubleOrNull() ?: 0.0
                            val months = tenureMonths.toIntOrNull() ?: 0
                            if (investment > 0 && rate >= 0 && months > 0) {
                                ResultExporter.exportSIPAsPDF(context, investment, rate, months, result!!)?.let { uri ->
                                    ResultExporter.shareFile(context, uri, "application/pdf", context.getString(R.string.share_pdf))
                                }
                            }
                        }
                    },
                    onShareImage = {
                        showShareDialog = false
                        coroutineScope.launch {
                            val investment = monthlyInvestment.toDoubleOrNull() ?: 0.0
                            val rate = annualRate.toDoubleOrNull() ?: 0.0
                            val months = tenureMonths.toIntOrNull() ?: 0
                            if (investment > 0 && rate >= 0 && months > 0) {
                                ResultExporter.exportSIPAsImage(context, investment, rate, months, result!!)?.let { uri ->
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

