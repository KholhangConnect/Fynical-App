package com.kholhang.fynical.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
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
import com.kholhang.fynical.utils.FDCalculator
import com.kholhang.fynical.utils.ResultExporter
import com.kholhang.fynical.utils.showToast
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FDCalculatorScreen(navController: NavController? = null) {
    var principal by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("") }
    var tenure by remember { mutableStateOf("") }
    var tenureUnit by remember { mutableStateOf(true) } // true = years, false = months
    var compoundingFrequency by remember { mutableStateOf(FDCalculator.CompoundingFrequency.QUARTERLY) }
    
    var result by remember { mutableStateOf<FDCalculator.FDResult?>(null) }
    var showShareDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.fd_calculator)) },
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
                value = principal,
                onValueChange = { principal = it },
                label = { Text(stringResource(R.string.principal)) },
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
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = tenure,
                    onValueChange = { tenure = it },
                    label = { Text(stringResource(R.string.tenure)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                
                Row(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = tenureUnit,
                        onClick = { tenureUnit = true },
                        label = { Text(stringResource(R.string.years)) }
                    )
                    FilterChip(
                        selected = !tenureUnit,
                        onClick = { tenureUnit = false },
                        label = { Text(stringResource(R.string.months)) }
                    )
                }
            }
            
            Text(
                text = stringResource(R.string.compounding_frequency),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = compoundingFrequency == FDCalculator.CompoundingFrequency.YEARLY,
                    onClick = { compoundingFrequency = FDCalculator.CompoundingFrequency.YEARLY },
                    label = { Text(stringResource(R.string.yearly)) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = compoundingFrequency == FDCalculator.CompoundingFrequency.HALF_YEARLY,
                    onClick = { compoundingFrequency = FDCalculator.CompoundingFrequency.HALF_YEARLY },
                    label = { Text(stringResource(R.string.half_yearly)) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = compoundingFrequency == FDCalculator.CompoundingFrequency.QUARTERLY,
                    onClick = { compoundingFrequency = FDCalculator.CompoundingFrequency.QUARTERLY },
                    label = { Text(stringResource(R.string.quarterly)) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = compoundingFrequency == FDCalculator.CompoundingFrequency.MONTHLY,
                    onClick = { compoundingFrequency = FDCalculator.CompoundingFrequency.MONTHLY },
                    label = { Text(stringResource(R.string.monthly)) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Button(
                onClick = {
                    if (principal.isBlank()) {
                        showToast(context, "Please enter principal amount")
                        return@Button
                    }
                    if (interestRate.isBlank()) {
                        showToast(context, "Please enter interest rate")
                        return@Button
                    }
                    if (tenure.isBlank()) {
                        showToast(context, "Please enter tenure")
                        return@Button
                    }
                    
                    val p = principal.toDoubleOrNull()
                    val r = interestRate.toDoubleOrNull()
                    val t = tenure.toDoubleOrNull()
                    
                    if (p == null || p <= 0) {
                        showToast(context, "Please enter a valid principal amount")
                        return@Button
                    }
                    if (r == null || r <= 0 || r > 100) {
                        showToast(context, "Please enter a valid interest rate (0-100%)")
                        return@Button
                    }
                    if (t == null || t <= 0) {
                        showToast(context, "Please enter a valid tenure")
                        return@Button
                    }
                    
                    val tenureYears = if (tenureUnit) t else t / 12.0
                    
                    if (p > 0 && r > 0 && t > 0) {
                        result = FDCalculator.calculate(p, r, tenureYears, compoundingFrequency)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.calculate))
            }
            
            var showYearlyBreakdown by remember { mutableStateOf(false) }
            
            if (result != null) {
                val yearlyBreakdown = remember(principal, interestRate, tenure, tenureUnit, compoundingFrequency) {
                    val p = principal.toDoubleOrNull() ?: 0.0
                    val r = interestRate.toDoubleOrNull() ?: 0.0
                    val t = tenure.toDoubleOrNull() ?: 0.0
                    val tenureYears = if (tenureUnit) t else t / 12.0
                    if (p > 0 && r > 0 && t > 0 && tenureYears >= 1) {
                        FDCalculator.calculateYearlyBreakdown(p, r, tenureYears.toInt(), compoundingFrequency)
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
                            Text(stringResource(R.string.interest_earned))
                            Text(
                                CurrencyFormatter.format(result!!.interestEarned),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.principal))
                            Text(CurrencyFormatter.format(result!!.principal))
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
                                TableCell(text = "Start Amount", weight = 1f, header = true)
                                TableCell(text = "Interest", weight = 1f, header = true)
                                TableCell(text = "End Amount", weight = 1f, header = true)
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
                                    TableCell(text = CurrencyFormatter.format(entry.principalAtStart), weight = 1f)
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
                            val p = principal.toDoubleOrNull() ?: 0.0
                            val r = interestRate.toDoubleOrNull() ?: 0.0
                            val t = tenure.toDoubleOrNull() ?: 0.0
                            val tenureYears = if (tenureUnit) t else t / 12.0
                            if (p > 0 && r > 0 && t > 0) {
                                ResultExporter.exportFDAsPDF(context, p, r, tenureYears, compoundingFrequency, result!!)?.let { uri ->
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
                            val t = tenure.toDoubleOrNull() ?: 0.0
                            val tenureYears = if (tenureUnit) t else t / 12.0
                            if (p > 0 && r > 0 && t > 0) {
                                ResultExporter.exportFDAsImage(context, p, r, tenureYears, compoundingFrequency, result!!)?.let { uri ->
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



