package com.kholhang.fynical.ui.screens

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kholhang.fynical.R
import com.kholhang.fynical.utils.CurrencyFormatter
import com.kholhang.fynical.utils.EMICalculator

data class ComparisonLoan(
    val id: String,
    val name: String,
    val principal: Double,
    val interestRate: Double,
    val tenureMonths: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanComparisonScreen(navController: NavController? = null) {
    var loans by remember { mutableStateOf<List<ComparisonLoan>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingLoan by remember { mutableStateOf<ComparisonLoan?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.loan_comparison)) },
                navigationIcon = {
                    if (navController != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    }
                },
                actions = {
                    if (loans.size < 4) {
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Loan")
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (loans.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "Add loans to compare. You can compare up to 4 loans.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                // Comparison table
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
                            text = "Comparison Results",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Header row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Loan", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            Text("EMI", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            Text("Total", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            Text("Interest", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        }
                        
                        Divider()
                        
                        // Loan rows
                        loans.forEach { loan ->
                            val emi = EMICalculator.calculateEMI(loan.principal, loan.interestRate, loan.tenureMonths)
                            val total = EMICalculator.calculateTotalAmount(emi, loan.tenureMonths)
                            val interest = EMICalculator.calculateTotalInterest(loan.principal, total)
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(loan.name, fontWeight = FontWeight.Bold)
                                    Text("${loan.interestRate}%", style = MaterialTheme.typography.bodySmall)
                                }
                                Text(CurrencyFormatter.format(emi), modifier = Modifier.weight(1f))
                                Text(CurrencyFormatter.format(total), modifier = Modifier.weight(1f))
                                Text(CurrencyFormatter.format(interest), modifier = Modifier.weight(1f))
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(
                                    onClick = { editingLoan = loan },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { loans = loans.filter { it.id != loan.id } },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                                }
                            }
                            
                            Divider()
                        }
                        
                        // Best option highlight
                        if (loans.size > 1) {
                            val bestLoan = loans.minByOrNull { loan ->
                                val emi = EMICalculator.calculateEMI(loan.principal, loan.interestRate, loan.tenureMonths)
                                val total = EMICalculator.calculateTotalAmount(emi, loan.tenureMonths)
                                total
                            }
                            
                            if (bestLoan != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Best Option: ${bestLoan.name}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showAddDialog || editingLoan != null) {
        AddEditComparisonLoanDialog(
            loan = editingLoan,
            onDismiss = {
                showAddDialog = false
                editingLoan = null
            },
            onSave = { loan ->
                if (editingLoan != null) {
                    loans = loans.map { if (it.id == loan.id) loan else it }
                } else {
                    loans = loans + loan
                }
                showAddDialog = false
                editingLoan = null
            }
        )
    }
}

@Composable
fun AddEditComparisonLoanDialog(
    loan: ComparisonLoan?,
    onDismiss: () -> Unit,
    onSave: (ComparisonLoan) -> Unit
) {
    var name by remember { mutableStateOf(loan?.name ?: "") }
    var principal by remember { mutableStateOf(loan?.principal?.toString() ?: "") }
    var interestRate by remember { mutableStateOf(loan?.interestRate?.toString() ?: "") }
    var tenureMonths by remember { mutableStateOf(loan?.tenureMonths?.toString() ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (loan == null) "Add Loan" else "Edit Loan") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Loan Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = principal.toDoubleOrNull() ?: 0.0
                    val r = interestRate.toDoubleOrNull() ?: 0.0
                    val t = tenureMonths.toIntOrNull() ?: 0
                    
                    if (name.isNotEmpty() && p > 0 && r > 0 && t > 0) {
                        onSave(
                            ComparisonLoan(
                                id = loan?.id ?: java.util.UUID.randomUUID().toString(),
                                name = name,
                                principal = p,
                                interestRate = r,
                                tenureMonths = t
                            )
                        )
                    }
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}


