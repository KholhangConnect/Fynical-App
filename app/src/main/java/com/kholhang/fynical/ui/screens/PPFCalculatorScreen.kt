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
import com.kholhang.fynical.utils.PPFCalculator
import com.kholhang.fynical.utils.ResultExporter
import com.kholhang.fynical.utils.showToast
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PPFCalculatorScreen(navController: NavController? = null) {
    var annualContribution by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("") }
    var years by remember { mutableStateOf("") }
    
    var result by remember { mutableStateOf<PPFCalculator.PPFResult?>(null) }
    var showShareDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ppf_calculator)) },
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
                value = annualContribution,
                onValueChange = { annualContribution = it },
                label = { Text(stringResource(R.string.annual_contribution)) },
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
                label = { Text(stringResource(R.string.ppf_interest_rate)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                suffix = { Text("%", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(end = 8.dp)) }
            )
            
            OutlinedTextField(
                value = years,
                onValueChange = { years = it },
                label = { Text("${stringResource(R.string.tenure)} (${stringResource(R.string.years)}, min 15)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            
            Button(
                onClick = {
                    if (annualContribution.isBlank()) {
                        showToast(context, "Please enter annual contribution")
                        return@Button
                    }
                    if (interestRate.isBlank()) {
                        showToast(context, "Please enter interest rate")
                        return@Button
                    }
                    if (years.isBlank()) {
                        showToast(context, "Please enter tenure")
                        return@Button
                    }
                    
                    val contribution = annualContribution.toDoubleOrNull()
                    val rate = interestRate.toDoubleOrNull()
                    val yrs = years.toIntOrNull()
                    
                    if (contribution == null || contribution <= 0) {
                        showToast(context, "Please enter a valid annual contribution")
                        return@Button
                    }
                    if (rate == null || rate <= 0 || rate > 100) {
                        showToast(context, "Please enter a valid interest rate (0-100%)")
                        return@Button
                    }
                    if (yrs == null || yrs < 15) {
                        showToast(context, "Please enter a valid tenure (minimum 15 years)")
                        return@Button
                    }
                    
                    result = PPFCalculator.calculate(contribution, rate, yrs)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.calculate))
            }
            
            var showYearlyBreakdown by remember { mutableStateOf(false) }
            
            if (result != null) {
                val yearlyBreakdown = remember(annualContribution, interestRate, years) {
                    val contribution = annualContribution.toDoubleOrNull() ?: 0.0
                    val rate = interestRate.toDoubleOrNull() ?: 0.0
                    val yrs = years.toIntOrNull() ?: 0
                    if (contribution > 0 && rate > 0 && yrs >= 15) {
                        PPFCalculator.calculateYearlyBreakdown(contribution, rate, yrs)
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
                            stringResource(R.string.results),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Divider()
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.ppf_maturity_amount))
                            Text(
                                "₹${CurrencyFormatter.format(result!!.maturityAmount)}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.total_contribution))
                            Text("₹${CurrencyFormatter.format(result!!.totalContribution)}")
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.interest_earned))
                            Text(
                                "₹${CurrencyFormatter.format(result!!.interestEarned)}",
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
                                TableCell(text = "Contribution", weight = 1f, header = true)
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
                                    TableCell(text = CurrencyFormatter.format(entry.contribution), weight = 1f)
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
                            val contribution = annualContribution.toDoubleOrNull() ?: 0.0
                            val rate = interestRate.toDoubleOrNull() ?: 0.0
                            val yrs = years.toIntOrNull() ?: 0
                            if (contribution > 0 && rate > 0 && yrs >= 15) {
                                ResultExporter.exportPPFAsPDF(context, contribution, rate, yrs, result!!)?.let { uri ->
                                    ResultExporter.shareFile(context, uri, "application/pdf", context.getString(R.string.share_pdf))
                                }
                            }
                        }
                    },
                    onShareImage = {
                        showShareDialog = false
                        coroutineScope.launch {
                            val contribution = annualContribution.toDoubleOrNull() ?: 0.0
                            val rate = interestRate.toDoubleOrNull() ?: 0.0
                            val yrs = years.toIntOrNull() ?: 0
                            if (contribution > 0 && rate > 0 && yrs >= 15) {
                                ResultExporter.exportPPFAsImage(context, contribution, rate, yrs, result!!)?.let { uri ->
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

