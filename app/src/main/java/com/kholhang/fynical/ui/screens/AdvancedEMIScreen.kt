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
import com.kholhang.fynical.utils.EMICalculator
import com.kholhang.fynical.utils.MoratoriumCalculator
import com.kholhang.fynical.utils.PrepaymentCalculator
import com.kholhang.fynical.utils.ROIChangeCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedEMIScreen(navController: NavController? = null) {
    var selectedTab by remember { mutableStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.advanced_emi)) },
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
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Basic EMI") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Prepayment") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("ROI Change") }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Moratorium") }
                )
            }
            
            when (selectedTab) {
                0 -> BasicEMITab()
                1 -> PrepaymentTab()
                2 -> ROIChangeTab()
                3 -> MoratoriumTab()
            }
        }
    }
}

@Composable
fun BasicEMITab() {
    var principal by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("") }
    var tenureMonths by remember { mutableStateOf("") }
    
    var emi by remember { mutableStateOf(0.0) }
    var totalAmount by remember { mutableStateOf(0.0) }
    var totalInterest by remember { mutableStateOf(0.0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
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
                Text("₹", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
            }
        )
        
        OutlinedTextField(
            value = interestRate,
            onValueChange = { interestRate = it },
            label = { Text(stringResource(R.string.interest_rate)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            suffix = { Text("%", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(end = 8.dp)) }
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
                val p = principal.toDoubleOrNull() ?: 0.0
                val r = interestRate.toDoubleOrNull() ?: 0.0
                val t = tenureMonths.toIntOrNull() ?: 0
                
                if (p > 0 && r > 0 && t > 0) {
                    emi = EMICalculator.calculateEMI(p, r, t)
                    totalAmount = EMICalculator.calculateTotalAmount(emi, t)
                    totalInterest = EMICalculator.calculateTotalInterest(p, totalAmount)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.calculate))
        }
        
        if (emi > 0) {
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
                        Text(stringResource(R.string.monthly_emi))
                        Text(
                            CurrencyFormatter.format(emi),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.total_amount))
                        Text(CurrencyFormatter.format(totalAmount))
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.total_interest))
                        Text(CurrencyFormatter.format(totalInterest))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrepaymentTab() {
    var principal by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("") }
    var tenureMonths by remember { mutableStateOf("") }
    var prepaymentAmount by remember { mutableStateOf("") }
    var prepaymentMonth by remember { mutableStateOf("") }
    var prepaymentOption by remember { mutableStateOf(PrepaymentCalculator.PrepaymentOption.REDUCE_EMI) }
    
    var result by remember { mutableStateOf<PrepaymentCalculator.PrepaymentResult?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
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
            singleLine = true
        )
        
        OutlinedTextField(
            value = interestRate,
            onValueChange = { interestRate = it },
            label = { Text(stringResource(R.string.interest_rate_pa)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        
        OutlinedTextField(
            value = tenureMonths,
            onValueChange = { tenureMonths = it },
            label = { Text("${stringResource(R.string.tenure)} (${stringResource(R.string.months)})") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        
        OutlinedTextField(
            value = prepaymentAmount,
            onValueChange = { prepaymentAmount = it },
            label = { Text("Prepayment Amount") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        
        OutlinedTextField(
            value = prepaymentMonth,
            onValueChange = { prepaymentMonth = it },
            label = { Text("Prepayment Month") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = prepaymentOption == PrepaymentCalculator.PrepaymentOption.REDUCE_EMI,
                onClick = { prepaymentOption = PrepaymentCalculator.PrepaymentOption.REDUCE_EMI },
                label = { Text("Reduce EMI") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = prepaymentOption == PrepaymentCalculator.PrepaymentOption.REDUCE_TENURE,
                onClick = { prepaymentOption = PrepaymentCalculator.PrepaymentOption.REDUCE_TENURE },
                label = { Text("Reduce Tenure") },
                modifier = Modifier.weight(1f)
            )
        }
        
        Button(
            onClick = {
                val p = principal.toDoubleOrNull() ?: 0.0
                val r = interestRate.toDoubleOrNull() ?: 0.0
                val t = tenureMonths.toIntOrNull() ?: 0
                val prepay = prepaymentAmount.toDoubleOrNull() ?: 0.0
                val prepayMonth = prepaymentMonth.toIntOrNull() ?: 0
                
                if (p > 0 && r > 0 && t > 0 && prepay > 0 && prepayMonth > 0) {
                    result = when (prepaymentOption) {
                        PrepaymentCalculator.PrepaymentOption.REDUCE_EMI -> 
                            PrepaymentCalculator.calculateWithEMIReduction(p, r, t, prepay, prepayMonth)
                        PrepaymentCalculator.PrepaymentOption.REDUCE_TENURE -> 
                            PrepaymentCalculator.calculateWithTenureReduction(p, r, t, prepay, prepayMonth)
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
                    
                    if (result!!.newEMI != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("New EMI")
                            Text(
                                CurrencyFormatter.format(result!!.newEMI ?: 0.0),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    if (result!!.newTenureMonths != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("New Tenure")
                            Text("${result!!.newTenureMonths} ${stringResource(R.string.months)}")
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Interest Savings")
                        Text(
                            CurrencyFormatter.format(result!!.interestSavings),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ROIChangeTab() {
    var principal by remember { mutableStateOf("") }
    var originalRate by remember { mutableStateOf("") }
    var newRate by remember { mutableStateOf("") }
    var tenureMonths by remember { mutableStateOf("") }
    
    var result by remember { mutableStateOf<ROIChangeCalculator.ROIChangeResult?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
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
            singleLine = true
        )
        
        OutlinedTextField(
            value = originalRate,
            onValueChange = { originalRate = it },
            label = { Text("Original Rate (%)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        
        OutlinedTextField(
            value = newRate,
            onValueChange = { newRate = it },
            label = { Text("New Rate (%)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
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
                val p = principal.toDoubleOrNull() ?: 0.0
                val origRate = originalRate.toDoubleOrNull() ?: 0.0
                val nRate = newRate.toDoubleOrNull() ?: 0.0
                val t = tenureMonths.toIntOrNull() ?: 0
                
                if (p > 0 && origRate >= 0 && nRate >= 0 && t > 0) {
                    result = ROIChangeCalculator.calculate(p, origRate, nRate, t)
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
                        Text("New EMI")
                        Text(
                            CurrencyFormatter.format(result!!.newEMI),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("EMI Difference")
                        Text(
                            CurrencyFormatter.format(result!!.emiDifference),
                            color = if (result!!.emiDifference >= 0) 
                                MaterialTheme.colorScheme.error 
                            else 
                                MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Interest Difference")
                        Text(
                            CurrencyFormatter.format(result!!.totalInterestDifference),
                            color = if (result!!.totalInterestDifference >= 0) 
                                MaterialTheme.colorScheme.error 
                            else 
                                MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MoratoriumTab() {
    var principal by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("") }
    var tenureMonths by remember { mutableStateOf("") }
    var moratoriumMonths by remember { mutableStateOf("") }
    
    var result by remember { mutableStateOf<MoratoriumCalculator.MoratoriumResult?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
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
            singleLine = true
        )
        
        OutlinedTextField(
            value = interestRate,
            onValueChange = { interestRate = it },
            label = { Text(stringResource(R.string.interest_rate_pa)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        
        OutlinedTextField(
            value = tenureMonths,
            onValueChange = { tenureMonths = it },
            label = { Text("${stringResource(R.string.tenure)} (${stringResource(R.string.months)})") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        
        OutlinedTextField(
            value = moratoriumMonths,
            onValueChange = { moratoriumMonths = it },
            label = { Text("Moratorium (${stringResource(R.string.months)})") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        
        Button(
            onClick = {
                val p = principal.toDoubleOrNull() ?: 0.0
                val r = interestRate.toDoubleOrNull() ?: 0.0
                val t = tenureMonths.toIntOrNull() ?: 0
                val m = moratoriumMonths.toIntOrNull() ?: 0
                
                if (p > 0 && r > 0 && t > 0 && m > 0) {
                    result = MoratoriumCalculator.calculate(p, r, t, m)
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
                        Text("New EMI")
                        Text(
                            CurrencyFormatter.format(result!!.newEMI),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Extended Tenure")
                        Text("${result!!.extendedTenureMonths} ${stringResource(R.string.months)}")
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Additional Interest")
                        Text(
                            CurrencyFormatter.format(result!!.additionalInterest),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}


