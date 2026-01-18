package com.kholhang.fynical.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kholhang.fynical.R
import com.kholhang.fynical.ui.components.ShareOptionsDialog
import com.kholhang.fynical.ui.components.TableCell
import com.kholhang.fynical.utils.CurrencyFormatter
import com.kholhang.fynical.utils.RDCalculator
import com.kholhang.fynical.utils.ResultExporter
import com.kholhang.fynical.utils.showToast
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RDCalculatorScreen(navController: NavController? = null) {
    var monthlyDeposit by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("") }
    var tenureMonths by remember { mutableStateOf("") }
    
    var result by remember { mutableStateOf<RDCalculator.RDResult?>(null) }
    var showShareDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.rd_calculator)) },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = monthlyDeposit,
                onValueChange = { monthlyDeposit = it },
                label = { Text(stringResource(R.string.monthly_deposit)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                leadingIcon = {
                    Text("₹", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
                }
            )
            
            OutlinedTextField(
                value = interestRate,
                onValueChange = { interestRate = it },
                label = { Text(stringResource(R.string.interest_rate_pa)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                suffix = { Text("%", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(end = 8.dp)) }
            )
            
            OutlinedTextField(
                value = tenureMonths,
                onValueChange = { tenureMonths = it },
                label = { Text("${stringResource(R.string.tenure)} (${stringResource(R.string.months)})") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            
            Button(
                onClick = {
                    if (monthlyDeposit.isBlank()) {
                        showToast(context, "Please enter monthly deposit")
                        return@Button
                    }
                    if (interestRate.isBlank()) {
                        showToast(context, "Please enter interest rate")
                        return@Button
                    }
                    if (tenureMonths.isBlank()) {
                        showToast(context, "Please enter tenure")
                        return@Button
                    }
                    
                    val deposit = monthlyDeposit.toDoubleOrNull()
                    val rate = interestRate.toDoubleOrNull()
                    val tenure = tenureMonths.toIntOrNull()
                    
                    if (deposit == null || deposit <= 0) {
                        showToast(context, "Please enter a valid monthly deposit")
                        return@Button
                    }
                    if (rate == null || rate <= 0 || rate > 100) {
                        showToast(context, "Please enter a valid interest rate (0-100%)")
                        return@Button
                    }
                    if (tenure == null || tenure <= 0) {
                        showToast(context, "Please enter a valid tenure (in months)")
                        return@Button
                    }
                    
                    result = RDCalculator.calculate(deposit, rate, tenure)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.calculate))
            }
            
            var showYearlyBreakdown by remember { mutableStateOf(false) }
            
            if (result != null) {
                val yearlyBreakdown = remember(monthlyDeposit, interestRate, tenureMonths) {
                    val deposit = monthlyDeposit.toDoubleOrNull() ?: 0.0
                    val rate = interestRate.toDoubleOrNull() ?: 0.0
                    val tenure = tenureMonths.toIntOrNull() ?: 0
                    if (deposit > 0 && rate > 0 && tenure > 0 && tenure >= 12) {
                        RDCalculator.calculateYearlyBreakdown(deposit, rate, tenure)
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
                        Text(
                            text = stringResource(R.string.results),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.maturity_amount))
                            Text(
                                CurrencyFormatter.format(result!!.maturityAmount),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.total_deposits))
                            Text(CurrencyFormatter.format(result!!.totalDeposits))
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.interest_earned))
                            Text(
                                CurrencyFormatter.format(result!!.interestEarned),
                                fontWeight = FontWeight.Bold
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
                                TableCell(text = "Deposits", weight = 1f, header = true)
                                TableCell(text = "Interest", weight = 1f, header = true)
                                TableCell(text = "Cumulative", weight = 1f, header = true)
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
                                    TableCell(text = CurrencyFormatter.format(entry.deposits), weight = 1f)
                                    TableCell(text = CurrencyFormatter.format(entry.interestEarned), weight = 1f)
                                    TableCell(text = CurrencyFormatter.format(entry.cumulativeAmount), weight = 1f)
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
                            val deposit = monthlyDeposit.toDoubleOrNull() ?: 0.0
                            val rate = interestRate.toDoubleOrNull() ?: 0.0
                            val tenure = tenureMonths.toIntOrNull() ?: 0
                            if (deposit > 0 && rate > 0 && tenure > 0) {
                                ResultExporter.exportRDAsPDF(context, deposit, rate, tenure, result!!)?.let { uri ->
                                    ResultExporter.shareFile(context, uri, "application/pdf", context.getString(R.string.share_pdf))
                                }
                            }
                        }
                    },
                    onShareImage = {
                        showShareDialog = false
                        coroutineScope.launch {
                            val deposit = monthlyDeposit.toDoubleOrNull() ?: 0.0
                            val rate = interestRate.toDoubleOrNull() ?: 0.0
                            val tenure = tenureMonths.toIntOrNull() ?: 0
                            if (deposit > 0 && rate > 0 && tenure > 0) {
                                ResultExporter.exportRDAsImage(context, deposit, rate, tenure, result!!)?.let { uri ->
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



