package com.kholhang.fynical.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import com.kholhang.fynical.utils.APYCalculator
import com.kholhang.fynical.utils.CurrencyFormatter
import com.kholhang.fynical.utils.showToast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun APYCalculatorScreen(navController: NavController? = null) {
    var entryAge by remember { mutableStateOf("") }
    var pensionAmount by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<APYCalculator.APYResult?>(null) }
    
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Atal Pension Yojana (APY)") },
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
            // Header Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Atal Pension Yojana (APY)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Government Pension Scheme",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "• Eligibility: Age 18-40 years (Income-tax payers NOT eligible from Oct 1, 2022)\n" +
                        "• Retirement Age: 60 years\n" +
                        "• Pension Options: ₹1000, ₹2000, ₹3000, ₹4000, ₹5000/month\n" +
                        "• Guaranteed pension for life after 60\n" +
                        "• Latest rates as per 2026\n" +
                        "• Government co-contribution: Discontinued for new subscribers (after March 31, 2016)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            // Input Fields
            OutlinedTextField(
                value = entryAge,
                onValueChange = { entryAge = it },
                label = { Text("Entry Age (18-40 years)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                placeholder = { Text("Enter age between 18-40") }
            )
            
            // Pension Amount Selection
            Text(
                "Select Monthly Pension Amount",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                APYCalculator.validPensionAmounts.forEach { amount ->
                    FilterChip(
                        selected = pensionAmount == amount.toString(),
                        onClick = { pensionAmount = amount.toString() },
                        label = { Text("₹${amount.toInt()}") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Or manual input
            OutlinedTextField(
                value = pensionAmount,
                onValueChange = { pensionAmount = it },
                label = { Text("Or Enter Pension Amount (₹1000, ₹2000, ₹3000, ₹4000, or ₹5000)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                leadingIcon = {
                    Text("₹", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
                },
                placeholder = { Text("1000, 2000, 3000, 4000, or 5000") }
            )
            
            // Calculate Button
            Button(
                onClick = {
                    if (entryAge.isBlank()) {
                        showToast(context, "Please enter entry age")
                        return@Button
                    }
                    if (pensionAmount.isBlank()) {
                        showToast(context, "Please select or enter pension amount")
                        return@Button
                    }
                    
                    val age = entryAge.toIntOrNull()
                    val pension = pensionAmount.toDoubleOrNull()
                    
                    if (age == null) {
                        showToast(context, "Please enter a valid age")
                        return@Button
                    }
                    if (pension == null) {
                        showToast(context, "Please enter a valid pension amount")
                        return@Button
                    }
                    
                    if (!APYCalculator.isValidAge(age)) {
                        showToast(context, "Age must be between 18-40 years")
                        return@Button
                    }
                    if (!APYCalculator.isValidPensionAmount(pension)) {
                        showToast(context, "Pension amount must be ₹1000, ₹2000, ₹3000, ₹4000, or ₹5000")
                        return@Button
                    }
                    
                    result = APYCalculator.calculateAPY(age, pension)
                    
                    if (!result!!.isValid) {
                        showToast(context, result!!.errorMessage ?: "Invalid calculation")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Calculate Premium")
            }
            
            // Results
            if (result != null && result!!.isValid) {
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
                            "APY Calculation Results",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Divider()
                        
                        APYResultRow("Entry Age:", "${result!!.entryAge} years")
                        APYResultRow("Retirement Age:", "60 years")
                        APYResultRow("Years to Retirement:", "${result!!.yearsToRetirement} years")
                        APYResultRow("Selected Monthly Pension:", CurrencyFormatter.format(result!!.pensionAmount))
                        APYResultRow("Monthly Contribution:", CurrencyFormatter.format(result!!.monthlyContribution))
                        APYResultRow("Total Contribution:", CurrencyFormatter.format(result!!.totalContribution))
                        APYResultRow("Corpus Return to Nominee:", CurrencyFormatter.format(result!!.corpusReturn))
                        APYResultRow("Guaranteed Monthly Pension:", CurrencyFormatter.format(result!!.monthlyPension))
                        
                        Divider()
                        
                        Text(
                            "Important: Government co-contribution was available only for subscribers who joined between June 1, 2015 and March 31, 2016. For all new subscribers joining after March 31, 2016 (including 2026), government co-contribution is NOT available.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            "Note: From October 1, 2022, income-tax payers are not eligible to enroll in APY.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                
                // Death Benefits & Nominee Rules Section
                var expandedDeathBenefits by remember { mutableStateOf(false) }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Death Benefits & Nominee Rules",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { expandedDeathBenefits = !expandedDeathBenefits }) {
                                Icon(
                                    if (expandedDeathBenefits) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (expandedDeathBenefits) "Collapse" else "Expand"
                                )
                            }
                        }
                        
                        if (expandedDeathBenefits) {
                            Divider()
                            
                            // Nomination Rules
                            Text(
                                "📋 Nomination Rules:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "• Nomination is MANDATORY when joining APY\n" +
                                "• If married: Spouse is default nominee\n" +
                                "• If unmarried: Any individual can be nominated\n" +
                                "• Savings/Post Office account required",
                                style = MaterialTheme.typography.bodySmall
                            )
                            
                            Divider()
                            
                            // Death Before Age 60
                            Text(
                                "⚠️ Death Before Age 60:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                "Two options available:\n\n" +
                                "Option 1: Spouse Continues APY\n" +
                                "• Spouse can continue contributions in subscriber's name\n" +
                                "• Must contribute same monthly amount\n" +
                                "• Spouse receives same pension when subscriber would have reached 60\n" +
                                "• Account transfers to spouse's name\n\n" +
                                "Option 2: Cancel & Refund\n" +
                                "• If spouse cannot/will not continue\n" +
                                "• Accumulated pension wealth (contributions + returns) refunded\n" +
                                "• Refund goes to spouse or nominee\n" +
                                "• Note: For subscribers who joined before March 31, 2016, government co-contribution may not be refunded",
                                style = MaterialTheme.typography.bodySmall
                            )
                            
                            Divider()
                            
                            // Death After Age 60
                            Text(
                                "✅ Death After Age 60:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "• Spouse receives same monthly pension for life\n" +
                                "• Pension continues until spouse's death\n" +
                                "• After spouse's death: Nominee receives lump-sum\n" +
                                "• Lump-sum = Pension wealth accumulated up to subscriber's 60th birthday\n" +
                                "• Nominee gets corpus return amount",
                                style = MaterialTheme.typography.bodySmall
                            )
                            
                            Divider()
                            
                            // Important Details to Follow
                            Text(
                                "📌 Important Details to Follow:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "1. Ensure nominee details are correctly filled at enrollment\n" +
                                "2. Keep nominee information updated if circumstances change\n" +
                                "3. Spouse must inform bank/PO within 30 days of subscriber's death\n" +
                                "4. Required documents: Death certificate, nominee ID proof, bank account details\n" +
                                "5. If spouse continues: Must maintain same contribution schedule\n" +
                                "6. Voluntary exit before 60: Only contributions + net returns refunded\n" +
                                "7. Income-tax payers are NOT eligible to enroll (from Oct 1, 2022)\n" +
                                "8. Government co-contribution was only for subscribers joining between June 2015 - March 2016",
                                style = MaterialTheme.typography.bodySmall
                            )
                            
                            Divider()
                            
                            // Corpus Return Information
                            Text(
                                "💰 Corpus Return to Nominee (Fixed Amounts):",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "• ₹1,000 pension → ₹1.7 Lakh corpus\n" +
                                "• ₹2,000 pension → ₹3.4 Lakh corpus\n" +
                                "• ₹3,000 pension → ₹5.1 Lakh corpus\n" +
                                "• ₹4,000 pension → ₹6.8 Lakh corpus\n" +
                                "• ₹5,000 pension → ₹8.5 Lakh corpus\n\n" +
                                "These amounts are fixed regardless of entry age and are paid to nominee after subscriber and spouse both pass away.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Text(
                                "Tap to view death benefits, nominee rules, and important details",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            } else if (result != null && !result!!.isValid) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        result!!.errorMessage ?: "Invalid calculation",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
fun APYResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
