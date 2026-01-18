package com.kholhang.fynical.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kholhang.fynical.R
import com.kholhang.fynical.utils.CurrencyFormatter
import com.kholhang.fynical.utils.GSTCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GSTCalculatorScreen(navController: NavController? = null) {
    var amount by remember { mutableStateOf("") }
    var gstRate by remember { mutableStateOf("") }
    var calculationMode by remember { mutableStateOf(true) } // true = from base, false = from total
    var gstType by remember { mutableStateOf(GSTCalculator.GSTType.CGST_SGST) }
    
    var result by remember { mutableStateOf<GSTCalculator.GSTResult?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.gst_calculator)) },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = calculationMode,
                    onClick = { calculationMode = true },
                    label = { Text(stringResource(R.string.gst_exclusive)) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = !calculationMode,
                    onClick = { calculationMode = false },
                    label = { Text(stringResource(R.string.gst_inclusive)) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { 
                    Text(
                        if (calculationMode) stringResource(R.string.base_amount)
                        else stringResource(R.string.total_amount)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                leadingIcon = {
                    Text("₹", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
                }
            )
            
            OutlinedTextField(
                value = gstRate,
                onValueChange = { gstRate = it },
                label = { Text(stringResource(R.string.gst_rate)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                suffix = { Text("%", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(end = 8.dp)) }
            )
            
            Text(
                text = stringResource(R.string.gst_type),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = gstType == GSTCalculator.GSTType.CGST_SGST,
                    onClick = { gstType = GSTCalculator.GSTType.CGST_SGST },
                    label = { Text("CGST + SGST") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = gstType == GSTCalculator.GSTType.IGST,
                    onClick = { gstType = GSTCalculator.GSTType.IGST },
                    label = { Text(stringResource(R.string.igst)) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    val rate = gstRate.toDoubleOrNull() ?: 0.0
                    
                    if (amt > 0 && rate >= 0 && rate <= 100) {
                        result = if (calculationMode) {
                            GSTCalculator.calculateFromBase(amt, rate, gstType)
                        } else {
                            GSTCalculator.calculateFromTotal(amt, rate, gstType)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.calculate))
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
                            text = stringResource(R.string.results),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.base_amount))
                            Text(CurrencyFormatter.format(result!!.baseAmount))
                        }
                        
                        if (result!!.gstType == GSTCalculator.GSTType.CGST_SGST) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.cgst))
                                Text(CurrencyFormatter.format(result!!.cgstAmount))
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.sgst))
                                Text(CurrencyFormatter.format(result!!.sgstAmount))
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.igst))
                                Text(CurrencyFormatter.format(result!!.igstAmount))
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.gst_amount))
                            Text(
                                CurrencyFormatter.format(result!!.gstAmount),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.total_amount))
                            Text(
                                CurrencyFormatter.format(result!!.totalAmount),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}


