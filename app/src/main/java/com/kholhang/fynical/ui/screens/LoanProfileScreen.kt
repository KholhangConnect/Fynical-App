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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kholhang.fynical.R
import com.kholhang.fynical.data.LoanProfile
import com.kholhang.fynical.data.LoanProfileRepository
import com.kholhang.fynical.utils.CurrencyFormatter
import com.kholhang.fynical.utils.EMICalculator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanProfileScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val repository = remember { LoanProfileRepository(context) }
    val scope = rememberCoroutineScope()
    
    val profiles by repository.getLoanProfilesFlow().collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<LoanProfile?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.loan_profile_manager)) },
                navigationIcon = {
                    if (navController != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Loan Profile")
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
            if (profiles.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "No loan profiles saved. Tap + to add one.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                profiles.forEach { profile ->
                    LoanProfileCard(
                        profile = profile,
                        onEdit = { editingProfile = it },
                        onDelete = { id ->
                            scope.launch {
                                repository.deleteLoanProfile(id)
                            }
                        }
                    )
                }
            }
        }
    }
    
    if (showAddDialog || editingProfile != null) {
        AddEditLoanProfileDialog(
            profile = editingProfile,
            onDismiss = {
                showAddDialog = false
                editingProfile = null
            },
            onSave = { profile ->
                scope.launch {
                    repository.saveLoanProfile(profile)
                    showAddDialog = false
                    editingProfile = null
                }
            }
        )
    }
}

@Composable
fun LoanProfileCard(
    profile: LoanProfile,
    onEdit: (LoanProfile) -> Unit,
    onDelete: (String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.bankName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = profile.loanType,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    IconButton(onClick = { onEdit(profile) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Principal:", style = MaterialTheme.typography.bodySmall)
                Text(CurrencyFormatter.format(profile.principal))
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Rate:", style = MaterialTheme.typography.bodySmall)
                Text("${profile.interestRate}%")
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("EMI:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(CurrencyFormatter.format(profile.emi), fontWeight = FontWeight.Bold)
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Start Date:", style = MaterialTheme.typography.bodySmall)
                Text(profile.startDate)
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Loan Profile") },
            text = { Text("Are you sure you want to delete this loan profile?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(profile.id)
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLoanProfileDialog(
    profile: LoanProfile?,
    onDismiss: () -> Unit,
    onSave: (LoanProfile) -> Unit
) {
    var bankName by remember { mutableStateOf(profile?.bankName ?: "") }
    var principal by remember { mutableStateOf(profile?.principal?.toString() ?: "") }
    var interestRate by remember { mutableStateOf(profile?.interestRate?.toString() ?: "") }
    var tenureMonths by remember { mutableStateOf(profile?.tenureMonths?.toString() ?: "") }
    var startDate by remember { mutableStateOf(profile?.startDate ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var loanType by remember { mutableStateOf(profile?.loanType ?: "Home Loan") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (profile == null) "Add Loan Profile" else "Edit Loan Profile") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Bank Name") },
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
                
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Start Date (DD/MM/YYYY)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = loanType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Loan Type") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("Home Loan", "Car Loan", "Personal Loan", "Education Loan").forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    loanType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = principal.toDoubleOrNull() ?: 0.0
                    val r = interestRate.toDoubleOrNull() ?: 0.0
                    val t = tenureMonths.toIntOrNull() ?: 0
                    
                    if (bankName.isNotEmpty() && p > 0 && r > 0 && t > 0) {
                        val emi = EMICalculator.calculateEMI(p, r, t)
                        val totalAmount = EMICalculator.calculateTotalAmount(emi, t)
                        val totalInterest = EMICalculator.calculateTotalInterest(p, totalAmount)
                        
                        val newProfile = LoanProfile(
                            id = profile?.id ?: UUID.randomUUID().toString(),
                            bankName = bankName,
                            principal = p,
                            interestRate = r,
                            tenureMonths = t,
                            startDate = startDate,
                            loanType = loanType,
                            emi = emi,
                            totalAmount = totalAmount,
                            totalInterest = totalInterest
                        )
                        onSave(newProfile)
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


