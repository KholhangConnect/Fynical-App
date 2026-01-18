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
import com.kholhang.fynical.utils.LoanEligibilityCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanEligibilityScreen(navController: NavController? = null) {
    var monthlyIncome by remember { mutableStateOf("") }
    var existingEMIs by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var loanType by remember { mutableStateOf(LoanEligibilityCalculator.LoanType.HOME_LOAN) }
    var interestRate by remember { mutableStateOf("8.5") }
    var tenureYears by remember { mutableStateOf("20") }
    
    var result by remember { mutableStateOf<LoanEligibilityCalculator.EligibilityResult?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.loan_eligibility)) },
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
            OutlinedTextField(
                value = monthlyIncome,
                onValueChange = { monthlyIncome = it },
                label = { Text("Monthly Income") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                leadingIcon = {
                    Text("₹", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
                }
            )
            
            OutlinedTextField(
                value = existingEMIs,
                onValueChange = { existingEMIs = it },
                label = { Text("Existing EMIs") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                leadingIcon = {
                    Text("₹", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
                }
            )
            
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Age") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            
            Text(
                text = "Loan Type",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = loanType == LoanEligibilityCalculator.LoanType.HOME_LOAN,
                    onClick = { loanType = LoanEligibilityCalculator.LoanType.HOME_LOAN },
                    label = { Text("Home Loan") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = loanType == LoanEligibilityCalculator.LoanType.CAR_LOAN,
                    onClick = { loanType = LoanEligibilityCalculator.LoanType.CAR_LOAN },
                    label = { Text("Car Loan") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = loanType == LoanEligibilityCalculator.LoanType.PERSONAL_LOAN,
                    onClick = { loanType = LoanEligibilityCalculator.LoanType.PERSONAL_LOAN },
                    label = { Text("Personal Loan") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = loanType == LoanEligibilityCalculator.LoanType.EDUCATION_LOAN,
                    onClick = { loanType = LoanEligibilityCalculator.LoanType.EDUCATION_LOAN },
                    label = { Text("Education Loan") },
                    modifier = Modifier.weight(1f)
                )
            }
            
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
                value = tenureYears,
                onValueChange = { tenureYears = it },
                label = { Text("${stringResource(R.string.tenure)} (${stringResource(R.string.years)})") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            
            Button(
                onClick = {
                    val income = monthlyIncome.toDoubleOrNull() ?: 0.0
                    val emis = existingEMIs.toDoubleOrNull() ?: 0.0
                    val ageValue = age.toIntOrNull() ?: 25
                    val rate = interestRate.toDoubleOrNull() ?: 8.5
                    val tenure = tenureYears.toIntOrNull() ?: 20
                    
                    if (income > 0 && ageValue > 0) {
                        result = LoanEligibilityCalculator.calculate(income, emis, ageValue, loanType, rate, tenure)
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
                            Text("Eligible Loan Amount")
                            Text(
                                CurrencyFormatter.format(result!!.eligibleLoanAmount),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Recommended Loan Amount")
                            Text(
                                CurrencyFormatter.format(result!!.recommendedLoanAmount),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Maximum EMI Capacity")
                            Text(CurrencyFormatter.format(result!!.maxEMICapacity))
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Maximum Tenure")
                            Text("${result!!.maxTenure} ${stringResource(R.string.years)}")
                        }
                    }
                }
            }
        }
    }
}


