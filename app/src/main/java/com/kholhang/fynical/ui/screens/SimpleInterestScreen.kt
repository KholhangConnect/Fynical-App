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
import com.kholhang.fynical.utils.ResultExporter
import com.kholhang.fynical.utils.SimpleInterestCalculator
import com.kholhang.fynical.utils.showToast
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleInterestScreen(navController: NavController? = null) {
    var principal by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var timeUnit by remember { mutableStateOf(SimpleInterestCalculator.TimeUnit.YEARS) }
    
    var result by remember { mutableStateOf<SimpleInterestCalculator.SimpleInterestResult?>(null) }
    var showShareDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.simple_interest_calculator)) },
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
                value = rate,
                onValueChange = { rate = it },
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
                    value = time,
                    onValueChange = { time = it },
                    label = { Text(stringResource(R.string.time_period)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                
                Row(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = timeUnit == SimpleInterestCalculator.TimeUnit.YEARS,
                        onClick = { timeUnit = SimpleInterestCalculator.TimeUnit.YEARS },
                        label = { Text(stringResource(R.string.years)) }
                    )
                    FilterChip(
                        selected = timeUnit == SimpleInterestCalculator.TimeUnit.MONTHS,
                        onClick = { timeUnit = SimpleInterestCalculator.TimeUnit.MONTHS },
                        label = { Text(stringResource(R.string.months)) }
                    )
                    FilterChip(
                        selected = timeUnit == SimpleInterestCalculator.TimeUnit.DAYS,
                        onClick = { timeUnit = SimpleInterestCalculator.TimeUnit.DAYS },
                        label = { Text(stringResource(R.string.days)) }
                    )
                }
            }
            
            Button(
                onClick = {
                    if (principal.isBlank()) {
                        showToast(context, "Please enter principal amount")
                        return@Button
                    }
                    if (rate.isBlank()) {
                        showToast(context, "Please enter interest rate")
                        return@Button
                    }
                    if (time.isBlank()) {
                        showToast(context, "Please enter time period")
                        return@Button
                    }
                    
                    val p = principal.toDoubleOrNull()
                    val r = rate.toDoubleOrNull()
                    val t = time.toDoubleOrNull()
                    
                    if (p == null || p <= 0) {
                        showToast(context, "Please enter a valid principal amount")
                        return@Button
                    }
                    if (r == null || r <= 0 || r > 100) {
                        showToast(context, "Please enter a valid interest rate (0-100%)")
                        return@Button
                    }
                    if (t == null || t <= 0) {
                        showToast(context, "Please enter a valid time period")
                        return@Button
                    }
                    
                    result = SimpleInterestCalculator.calculate(p, r, t, timeUnit)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.calculate))
            }
            
            var showYearlyBreakdown by remember { mutableStateOf(false) }
            
            if (result != null) {
                val yearlyBreakdown = remember(principal, rate, time, timeUnit) {
                    val p = principal.toDoubleOrNull() ?: 0.0
                    val r = rate.toDoubleOrNull() ?: 0.0
                    val t = time.toDoubleOrNull() ?: 0.0
                    val timeInYears = when (timeUnit) {
                        SimpleInterestCalculator.TimeUnit.YEARS -> t.toInt()
                        SimpleInterestCalculator.TimeUnit.MONTHS -> (t / 12.0).toInt()
                        SimpleInterestCalculator.TimeUnit.DAYS -> (t / 365.0).toInt()
                    }
                    if (p > 0 && r > 0 && t > 0 && timeInYears >= 1) {
                        SimpleInterestCalculator.calculateYearlyBreakdown(p, r, timeInYears, timeUnit)
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
                            Text(stringResource(R.string.interest_amount))
                            Text(
                                CurrencyFormatter.format(result!!.interestAmount),
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
                            val r = rate.toDoubleOrNull() ?: 0.0
                            val t = time.toDoubleOrNull() ?: 0.0
                            if (p > 0 && r > 0 && t > 0) {
                                ResultExporter.exportSimpleInterestAsPDF(context, p, r, t, timeUnit, result!!)?.let { uri ->
                                    ResultExporter.shareFile(context, uri, "application/pdf", context.getString(R.string.share_pdf))
                                }
                            }
                        }
                    },
                    onShareImage = {
                        showShareDialog = false
                        coroutineScope.launch {
                            val p = principal.toDoubleOrNull() ?: 0.0
                            val r = rate.toDoubleOrNull() ?: 0.0
                            val t = time.toDoubleOrNull() ?: 0.0
                            if (p > 0 && r > 0 && t > 0) {
                                ResultExporter.exportSimpleInterestAsImage(context, p, r, t, timeUnit, result!!)?.let { uri ->
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


