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
import com.kholhang.fynical.utils.InflationCalculator
import com.kholhang.fynical.utils.showToast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InflationCalculatorScreen(navController: NavController? = null) {
    var presentValue by remember { mutableStateOf("") }
    var inflationRate by remember { mutableStateOf("6") } // Default inflation rate
    var years by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    
    val result = remember(presentValue, inflationRate, years) {
        try {
            if (presentValue.isBlank() || inflationRate.isBlank() || years.isBlank()) {
                return@remember null
            }
            
            val pv = presentValue.toDoubleOrNull()
            val rate = inflationRate.toDoubleOrNull()
            val yearsValue = years.toIntOrNull()
            
            if (pv == null || pv <= 0) {
                return@remember null
            }
            if (rate == null || rate < 0 || rate > 100) {
                return@remember null
            }
            if (yearsValue == null || yearsValue <= 0) {
                return@remember null
            }
            
            InflationCalculator.calculateFutureValue(pv, rate, yearsValue)
        } catch (e: Exception) {
            null
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.inflation_calculator)) },
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = presentValue,
                onValueChange = { presentValue = it },
                label = { Text(stringResource(R.string.current_amount)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Default.AccountCircle, null) },
                singleLine = true
            )
            
            OutlinedTextField(
                value = inflationRate,
                onValueChange = { inflationRate = it },
                label = { Text(stringResource(R.string.inflation_rate)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Default.AccountCircle, null) },
                suffix = { Text("%") },
                singleLine = true
            )
            
            OutlinedTextField(
                value = years,
                onValueChange = { years = it },
                label = { Text(stringResource(R.string.time_period)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Icon(Icons.Default.Settings, null) },
                suffix = { Text(stringResource(R.string.years)) },
                singleLine = true
            )
            
            Button(
                onClick = {
                    if (presentValue.isBlank()) {
                        showToast(context, "Please enter current amount")
                        return@Button
                    }
                    if (inflationRate.isBlank()) {
                        showToast(context, "Please enter inflation rate")
                        return@Button
                    }
                    if (years.isBlank()) {
                        showToast(context, "Please enter time period")
                        return@Button
                    }
                    
                    val pv = presentValue.toDoubleOrNull()
                    val rate = inflationRate.toDoubleOrNull()
                    val yrs = years.toIntOrNull()
                    
                    if (pv == null || pv <= 0) {
                        showToast(context, "Please enter a valid current amount")
                        return@Button
                    }
                    if (rate == null || rate < 0 || rate > 100) {
                        showToast(context, "Please enter a valid inflation rate (0-100%)")
                        return@Button
                    }
                    if (yrs == null || yrs <= 0) {
                        showToast(context, "Please enter a valid time period (in years)")
                        return@Button
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.calculate))
            }
            
            var showYearlyBreakdown by remember { mutableStateOf(false) }
            
            if (result != null) {
                val yearlyBreakdown = remember(presentValue, inflationRate, years) {
                    val pv = presentValue.toDoubleOrNull() ?: 0.0
                    val rate = inflationRate.toDoubleOrNull() ?: 0.0
                    val yrs = years.toIntOrNull() ?: 0
                    if (pv > 0 && rate >= 0 && yrs > 0) {
                        InflationCalculator.calculateYearlyBreakdown(pv, rate, yrs)
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
                            Text(stringResource(R.string.current_value))
                            Text(CurrencyFormatter.format(result.presentValue))
                        }
                        
                        Divider()
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                stringResource(R.string.future_value),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                CurrencyFormatter.format(result.futureValue),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.purchasing_power_loss))
                            Text(
                                CurrencyFormatter.format(result.purchasingPowerLoss),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        
                        if (yearlyBreakdown?.isNotEmpty() == true) {
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
                
                yearlyBreakdown?.let { breakdown ->
                    if (showYearlyBreakdown && breakdown.isNotEmpty()) {
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
                                    TableCell(text = "Start Value", weight = 1f, header = true)
                                    TableCell(text = "Inflation", weight = 1f, header = true)
                                    TableCell(text = "End Value", weight = 1f, header = true)
                                }
                                Divider()
                                breakdown.forEach { entry ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState())
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        TableCell(text = entry.year.toString(), weight = 0.5f)
                                        TableCell(text = CurrencyFormatter.format(entry.presentValue), weight = 1f)
                                        TableCell(text = CurrencyFormatter.format(entry.inflationAmount), weight = 1f)
                                        TableCell(text = CurrencyFormatter.format(entry.futureValue), weight = 1f)
                                    }
                                    Divider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

