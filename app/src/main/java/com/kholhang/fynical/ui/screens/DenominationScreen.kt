package com.kholhang.fynical.ui.screens

import android.content.Context
import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kholhang.fynical.R
import com.kholhang.fynical.data.DenominationItem
import com.kholhang.fynical.data.DenominationRepository
import com.kholhang.fynical.data.DenominationType
import com.kholhang.fynical.data.SettingsRepository
import com.kholhang.fynical.data.HistoryRepository
import com.kholhang.fynical.data.DenominationHistoryRepository
import com.kholhang.fynical.data.DenominationSnapshot
import com.kholhang.fynical.utils.CurrencyFormatter
import com.kholhang.fynical.utils.NumberToWordsConverter
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DenominationScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val repository = remember { DenominationRepository(context) }
    val settingsRepository = remember { SettingsRepository(context) }
    val historyRepository = remember { HistoryRepository(context) }
    val denominationHistoryRepository = remember { DenominationHistoryRepository(context) }
    val scope = rememberCoroutineScope()
    
    var selectedTab by remember { mutableStateOf(0) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showLoadDialog by remember { mutableStateOf(false) }
    
    val savedSnapshots by denominationHistoryRepository.getSnapshotsFlow().collectAsState(initial = emptyList())
    
    // Cash Book state
    var openingBalance by remember { mutableStateOf("") }
    var totalDeposit by remember { mutableStateOf("") }
    var totalPayment by remember { mutableStateOf("") }
    
    val rupees = repository.getRupeesFlow().collectAsState(initial = emptyList())
    val coins = repository.getCoinsFlow().collectAsState(initial = emptyList())
    val settings by settingsRepository.getSettingsFlow().collectAsState(
        initial = com.kholhang.fynical.data.AppSettings(
            isDarkMode = false,
            keepScreenOn = false,
            hiddenDenominations = emptySet(),
            cardSizeMultiplier = 1.0f,
            textSizeMultiplier = 1.0f,
            cardPadding = 1.0f,
            cardSpacing = 6.0f
        )
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.denomination_manager)) },
                navigationIcon = {
                    if (navController != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showSaveDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.save_denomination)
                        )
                    }
                    IconButton(onClick = { showLoadDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = stringResource(R.string.load_denomination)
                        )
                    }
                    IconButton(onClick = {
                        val notesTotal = rupees.value.sumOf { it.total }
                        val coinsTotal = coins.value.sumOf { it.total }
                        val grandTotal = notesTotal + coinsTotal
                        val openingBalanceValue = openingBalance.toDoubleOrNull() ?: 0.0
                        val totalDepositValue = totalDeposit.toDoubleOrNull() ?: 0.0
                        val totalPaymentValue = totalPayment.toDoubleOrNull() ?: 0.0
                        val calculatedTotalCashInHand = openingBalanceValue + totalDepositValue
                        val closingBalance = calculatedTotalCashInHand - totalPaymentValue
                        shareDenominations(
                            context, 
                            rupees.value, 
                            coins.value, 
                            notesTotal, 
                            coinsTotal, 
                            grandTotal,
                            openingBalanceValue,
                            totalDepositValue,
                            totalPaymentValue,
                            calculatedTotalCashInHand,
                            closingBalance
                        )
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.share_denomination)
                        )
                    }
                    IconButton(onClick = { navController?.navigate("settings") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
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
                    text = { Text(stringResource(R.string.notes)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.coins)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(stringResource(R.string.cash_book)) }
                )
            }
            
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> {
                        DenominationList(
                            items = rupees.value.filter { !settings.hiddenDenominations.contains(it.id) },
                            onUpdate = { id, value, quantity ->
                                scope.launch {
                                    repository.updateRupee(id, value, quantity)
                                }
                            },
                            onDelete = { id ->
                                scope.launch {
                                    repository.deleteRupee(id)
                                }
                            },
                            denominationType = stringResource(R.string.notes),
                            settings = settings
                        )
                    }
                    1 -> {
                        DenominationList(
                            items = coins.value.filter { !settings.hiddenDenominations.contains(it.id) },
                            onUpdate = { id, value, quantity ->
                                scope.launch {
                                    repository.updateCoin(id, value, quantity)
                                }
                            },
                            onDelete = { id ->
                                scope.launch {
                                    repository.deleteCoin(id)
                                }
                            },
                            denominationType = stringResource(R.string.coins),
                            settings = settings
                        )
                    }
                    2 -> {
                        // Cash Book - Cash book view
                        CashBookScreen(
                            openingBalance = openingBalance,
                            onOpeningBalanceChange = { openingBalance = it },
                            totalDeposit = totalDeposit,
                            onTotalDepositChange = { totalDeposit = it },
                            totalPayment = totalPayment,
                            onTotalPaymentChange = { totalPayment = it },
                            notesTotal = rupees.value.sumOf { it.total },
                            coinsTotal = coins.value.sumOf { it.total },
                            onSaveHistory = { entry ->
                                scope.launch {
                                    historyRepository.addHistoryEntry(entry)
                                }
                            }
                        )
                    }
                }
            }
            
            // Combined Totals Section - Show only on Notes and Coins tabs
            if (selectedTab != 2) {
                CombinedTotalsSection(
                    rupeesTotal = rupees.value.sumOf { it.total },
                    coinsTotal = coins.value.sumOf { it.total },
                    context = context
                )
                
                // Clear All Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                rupees.value.forEach { note ->
                                    repository.updateRupee(note.id, note.value, 0)
                                }
                                coins.value.forEach { coin ->
                                    repository.updateCoin(coin.id, coin.value, 0)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.clear_all))
                    }
                }
            }
        }
    }
    
    // Save Denomination Dialog
    if (showSaveDialog) {
        val notesTotal = rupees.value.sumOf { it.total }
        val coinsTotal = coins.value.sumOf { it.total }
        
        SaveDenominationDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { date ->
                val snapshot = DenominationSnapshot(
                    id = java.util.UUID.randomUUID().toString(),
                    date = date,
                    notes = rupees.value,
                    coins = coins.value,
                    notesTotal = notesTotal,
                    coinsTotal = coinsTotal,
                    grandTotal = notesTotal + coinsTotal
                )
                scope.launch {
                    denominationHistoryRepository.saveSnapshot(snapshot)
                    showSaveDialog = false
                }
            },
            notesTotal = notesTotal,
            coinsTotal = coinsTotal
        )
    }
    
    // Load Denomination Dialog
    if (showLoadDialog) {
        LoadDenominationDialog(
            snapshots = savedSnapshots,
            onDismiss = { showLoadDialog = false },
            onLoad = { snapshot ->
                scope.launch {
                    snapshot.notes.forEach { note ->
                        repository.updateRupee(note.id, note.value, note.quantity)
                    }
                    snapshot.coins.forEach { coin ->
                        repository.updateCoin(coin.id, coin.value, coin.quantity)
                    }
                    showLoadDialog = false
                }
            },
            onDelete = { id ->
                scope.launch {
                    denominationHistoryRepository.deleteSnapshot(id)
                }
            }
        )
    }
}

fun shareDenominations(
    context: Context,
    notes: List<DenominationItem>,
    coins: List<DenominationItem>,
    notesTotal: Double,
    coinsTotal: Double,
    grandTotal: Double,
    openingBalance: Double = 0.0,
    totalDeposit: Double = 0.0,
    totalPayment: Double = 0.0,
    totalCashInHand: Double = 0.0,
    closingBalance: Double = 0.0
) {
    val shareText = buildString {
        appendLine("DENOMINATION REPORT")
        appendLine("===================")
        appendLine()
        
        // Notes Section - Only show denominations with quantity > 0
        val notesWithQuantity = notes.filter { it.quantity > 0 }
        if (notesWithQuantity.isNotEmpty()) {
            appendLine("NOTES:")
            appendLine("------")
            notesWithQuantity.forEach { note ->
                appendLine("${CurrencyFormatter.format(note.value)} x ${note.quantity} = ${CurrencyFormatter.format(note.total)}")
            }
            appendLine("Total Notes: ${CurrencyFormatter.format(notesTotal)}")
            appendLine()
        }
        
        // Coins Section - Only show denominations with quantity > 0
        val coinsWithQuantity = coins.filter { it.quantity > 0 }
        if (coinsWithQuantity.isNotEmpty()) {
            appendLine("COINS:")
            appendLine("------")
            coinsWithQuantity.forEach { coin ->
                appendLine("${CurrencyFormatter.format(coin.value)} x ${coin.quantity} = ${CurrencyFormatter.format(coin.total)}")
            }
            appendLine("Total Coins: ${CurrencyFormatter.format(coinsTotal)}")
            appendLine()
        }
        
        // Grand Total
        if (notesWithQuantity.isNotEmpty() || coinsWithQuantity.isNotEmpty()) {
            appendLine("GRAND TOTAL: ${CurrencyFormatter.format(grandTotal)}")
            appendLine("Amount in Words: ${NumberToWordsConverter.convertToWords(grandTotal)}")
            appendLine()
        }
        
        // Cash Book Details
        if (openingBalance > 0.0 || totalDeposit > 0.0 || totalPayment > 0.0 || totalCashInHand > 0.0) {
            appendLine("CASH BOOK DETAILS")
            appendLine("=================")
            appendLine()
            appendLine("Opening Balance: ${CurrencyFormatter.format(openingBalance)}")
            appendLine("Total Deposit: ${CurrencyFormatter.format(totalDeposit)}")
            appendLine("Total Cash in Hand: ${CurrencyFormatter.format(totalCashInHand)}")
            appendLine("Total Payment: ${CurrencyFormatter.format(totalPayment)}")
            appendLine("Closing Balance: ${CurrencyFormatter.format(closingBalance)}")
            appendLine()
            appendLine("Closing Balance in Words: ${NumberToWordsConverter.convertToWords(closingBalance)}")
        }
        
        // Summary
        appendLine()
        appendLine("---")
        appendLine("Generated by Fynical App")
    }
    
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "Denomination Report")
    }
    context.startActivity(Intent.createChooser(intent, "Share Denomination Report"))
}

@Composable
fun SaveDenominationDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    notesTotal: Double = 0.0,
    coinsTotal: Double = 0.0
) {
    var dateText by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val defaultDate = dateFormat.format(Date())
    val calendar = remember { Calendar.getInstance() }
    
    LaunchedEffect(Unit) {
        dateText = defaultDate
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.save_denomination)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.enter_date),
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { 
                        // Allow manual input but validate format
                        if (it.length <= 10) {
                            dateText = it
                        }
                    },
                    label = { Text(stringResource(R.string.date)) },
                    placeholder = { Text("DD/MM/YYYY") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.select_date)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text(
                    text = stringResource(R.string.date_format_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val grandTotal = notesTotal + coinsTotal
                    if (grandTotal == 0.0) {
                        showErrorDialog = true
                    } else {
                        val finalDate = if (dateText.isNotBlank() && dateText.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) {
                            dateText
                        } else {
                            defaultDate
                        }
                        onSave(finalDate)
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
    
    // Simple date picker using dropdowns
    if (showDatePicker) {
        var selectedDay by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
        var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
        var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
        
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text(stringResource(R.string.select_date)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Day
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Day", style = MaterialTheme.typography.labelSmall)
                            var dayText by remember { mutableStateOf(selectedDay.toString()) }
                            OutlinedTextField(
                                value = dayText,
                                onValueChange = { 
                                    dayText = it
                                    selectedDay = it.toIntOrNull()?.coerceIn(1, 31) ?: 1
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        // Month
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Month", style = MaterialTheme.typography.labelSmall)
                            var monthText by remember { mutableStateOf((selectedMonth + 1).toString()) }
                            OutlinedTextField(
                                value = monthText,
                                onValueChange = { 
                                    monthText = it
                                    selectedMonth = (it.toIntOrNull()?.coerceIn(1, 12) ?: 1) - 1
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        // Year
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Year", style = MaterialTheme.typography.labelSmall)
                            var yearText by remember { mutableStateOf(selectedYear.toString()) }
                            OutlinedTextField(
                                value = yearText,
                                onValueChange = { 
                                    yearText = it
                                    selectedYear = it.toIntOrNull()?.coerceIn(2000, 2100) ?: calendar.get(Calendar.YEAR)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        calendar.set(selectedYear, selectedMonth, selectedDay)
                        dateText = dateFormat.format(calendar.time)
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun LoadDenominationDialog(
    snapshots: List<DenominationSnapshot>,
    onDismiss: () -> Unit,
    onLoad: (DenominationSnapshot) -> Unit,
    onDelete: (String) -> Unit
) {
    var entryToDelete by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.load_denomination)) },
        text = {
            if (snapshots.isEmpty()) {
                Text(stringResource(R.string.no_saved_denominations))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    snapshots.forEach { snapshot ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLoad(snapshot)
                                    onDismiss()
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = snapshot.date,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = stringResource(R.string.grand_total) + ": " + CurrencyFormatter.format(snapshot.grandTotal),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = stringResource(R.string.notes) + ": " + CurrencyFormatter.format(snapshot.notesTotal) + 
                                               " | " + stringResource(R.string.coins) + ": " + CurrencyFormatter.format(snapshot.coinsTotal),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                IconButton(
                                    onClick = { entryToDelete = snapshot.id },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.delete),
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
    
    // Delete confirmation
    entryToDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text(stringResource(R.string.delete_entry)) },
            text = { Text(stringResource(R.string.delete_entry_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(id)
                        entryToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun DenominationList(
    items: List<DenominationItem>,
    onUpdate: (String, Double, Int) -> Unit,
    onDelete: (String) -> Unit,
    denominationType: String = "Notes", // "Notes" or "Coins"
    settings: com.kholhang.fynical.data.AppSettings
) {
    val spacing = settings.cardSpacing.dp
    val showDividers = spacing <= 1.dp
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = if (showDividers) {
            Arrangement.spacedBy(0.dp)
        } else {
            Arrangement.spacedBy(spacing)
        }
    ) {
        items.forEachIndexed { index, item ->
            DenominationItemCard(
                item = item,
                onUpdate = onUpdate,
                onDelete = onDelete,
                denominationType = denominationType,
                settings = settings
            )
            
            // Add divider if spacing is 0 or very small and not the last item
            if (showDividers && index < items.size - 1) {
                Divider(
                    modifier = Modifier.padding(vertical = 2.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            }
        }
        
        // Always show items, even if quantity is 0
    }
}

@Composable
fun CombinedTotalsSection(
    rupeesTotal: Double,
    coinsTotal: Double,
    context: Context
) {
    var showCopyToast by remember { mutableStateOf(false) }
    val grandTotal = rupeesTotal + coinsTotal
    
    if (rupeesTotal > 0 || coinsTotal > 0) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(
                        R.string.total_notes,
                        CurrencyFormatter.format(rupeesTotal)
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = stringResource(
                        R.string.total_coins,
                        CurrencyFormatter.format(coinsTotal)
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Divider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                )
                
                Text(
                    text = stringResource(
                        R.string.grand_total_equals,
                        CurrencyFormatter.format(grandTotal)
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = NumberToWordsConverter.convertToWords(grandTotal),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText(
                                "Denomination Total",
                                "Grand Total: ${CurrencyFormatter.format(grandTotal)}\n${NumberToWordsConverter.convertToWords(grandTotal)}"
                            )
                            clipboard.setPrimaryClip(clip)
                            showCopyToast = true
                        }
                    ) {
                        Text(
                            text = "📋",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
    
    if (showCopyToast) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            showCopyToast = false
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Text(
                text = stringResource(R.string.copied_to_clipboard),
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun DenominationItemCard(
    item: DenominationItem,
    onUpdate: (String, Double, Int) -> Unit,
    onDelete: (String) -> Unit,
    denominationType: String = "Notes",
    settings: com.kholhang.fynical.data.AppSettings
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val cardPadding = (12.dp * settings.cardPadding).coerceAtLeast(4.dp).coerceAtMost(30.dp)
    val iconSize = (40.dp * settings.cardSizeMultiplier).coerceAtLeast(24.dp).coerceAtMost(60.dp)
    val textStyle = MaterialTheme.typography.bodyLarge.copy(
        fontSize = MaterialTheme.typography.bodyLarge.fontSize * settings.textSizeMultiplier
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                Modifier.padding(
                    horizontal = (12.dp * settings.cardSizeMultiplier).coerceAtLeast(4.dp).coerceAtMost(24.dp),
                    vertical = (8.dp * settings.cardSizeMultiplier).coerceAtLeast(4.dp).coerceAtMost(16.dp)
                )
            )
            .clickable { showEditDialog = true },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = cardPadding, vertical = cardPadding * 0.67f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${CurrencyFormatter.format(item.value)}x${item.quantity}=${CurrencyFormatter.format(item.total)}",
                    style = textStyle,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Quick Decrement
                if (item.quantity > 0) {
                    IconButton(
                        onClick = {
                            onUpdate(item.id, item.value, maxOf(0, item.quantity - 1))
                            // Prevent card click from triggering
                        },
                        modifier = Modifier.size(iconSize * 0.8f)
                    ) {
                        Text(
                            text = "-",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(iconSize * 0.6f)
                        )
                    }
                }
                
                // Quick Increment
                IconButton(
                    onClick = {
                        onUpdate(item.id, item.value, item.quantity + 1)
                        // Prevent card click from triggering
                    },
                    modifier = Modifier.size(iconSize * 0.8f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.increase),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(iconSize * 0.4f)
                    )
                }
                
                // Edit (Full Dialog)
                IconButton(
                    onClick = { 
                        showEditDialog = true
                        // Prevent card click from triggering
                    },
                    modifier = Modifier.size(iconSize)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.edit_desc),
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(iconSize * 0.5f)
                    )
                }
                
                // Delete
                if (item.quantity > 0) {
                    IconButton(
                        onClick = { 
                            showDeleteDialog = true
                            // Prevent card click from triggering
                        },
                        modifier = Modifier.size(iconSize)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_desc),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(iconSize * 0.5f)
                        )
                    }
                }
            }
        }
    }
    
    if (showEditDialog) {
        EditQuantityDialog(
            currentQuantity = item.quantity,
            denominationValue = item.value,
            denominationType = denominationType,
            onDismiss = { showEditDialog = false },
            onSave = { quantity ->
                onUpdate(item.id, item.value, quantity)
                showEditDialog = false
            }
        )
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_denomination)) },
            text = { 
                Text(stringResource(R.string.delete_denomination_confirmation))
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(item.id)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
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

@Composable
fun EditQuantityDialog(
    currentQuantity: Int,
    denominationValue: Double,
    denominationType: String,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var quantity by remember { mutableStateOf(currentQuantity.toString()) }
    
    val isAdd = currentQuantity == 0
    val notesString = stringResource(R.string.notes)
    val denominationDisplay = CurrencyFormatter.format(denominationValue)
    val title = if (isAdd) {
        if (denominationType == notesString) {
            stringResource(R.string.add_notes_denomination_quantity, denominationDisplay)
        } else {
            stringResource(R.string.add_coins_denomination_quantity, denominationDisplay)
        }
    } else {
        if (denominationType == notesString) {
            stringResource(R.string.update_notes_denomination_quantity, denominationDisplay)
        } else {
            stringResource(R.string.update_coins_denomination_quantity, denominationDisplay)
        }
    }
    val buttonText = if (isAdd) stringResource(R.string.add) else stringResource(R.string.update)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.denomination_label, denominationDisplay),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text(stringResource(R.string.quantity)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val q = quantity.toIntOrNull() ?: 0
                    if (q >= 0) {
                        onSave(q)
                    }
                }
            ) {
                Text(buttonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CashBookScreen(
    openingBalance: String,
    onOpeningBalanceChange: (String) -> Unit,
    totalDeposit: String,
    onTotalDepositChange: (String) -> Unit,
    totalPayment: String,
    onTotalPaymentChange: (String) -> Unit,
    notesTotal: Double = 0.0,
    coinsTotal: Double = 0.0,
    onSaveHistory: (com.kholhang.fynical.data.CashBookHistoryEntry) -> Unit = {}
) {
    val openingBalanceValue = openingBalance.toDoubleOrNull() ?: 0.0
    val totalDepositValue = totalDeposit.toDoubleOrNull() ?: 0.0
    val totalPaymentValue = totalPayment.toDoubleOrNull() ?: 0.0
    // Total Cash in Hand = Opening Balance + Total Deposit
    val calculatedTotalCashInHand = openingBalanceValue + totalDepositValue
    // Closing Balance = Total Cash in Hand - Total Payment
    val closingBalance = calculatedTotalCashInHand - totalPaymentValue
    val grandTotal = notesTotal + coinsTotal
    
    var showSaveDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = openingBalance,
                    onValueChange = onOpeningBalanceChange,
                    label = { Text(stringResource(R.string.opening_balance)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = {
                        Text(
                            text = "₹",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                )
                
                OutlinedTextField(
                    value = totalDeposit,
                    onValueChange = onTotalDepositChange,
                    label = { Text(stringResource(R.string.total_deposit)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = {
                        Text(
                            text = "₹",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                )
                
                OutlinedTextField(
                    value = totalPayment,
                    onValueChange = onTotalPaymentChange,
                    label = { Text(stringResource(R.string.total_payment)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = {
                        Text(
                            text = "₹",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                )
            }
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
                    Text(
                        text = stringResource(R.string.opening_balance),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = CurrencyFormatter.format(openingBalanceValue),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.total_deposit),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = CurrencyFormatter.format(totalDepositValue),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.total_cash_in_hand),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = CurrencyFormatter.format(calculatedTotalCashInHand),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.total_payment),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = CurrencyFormatter.format(totalPaymentValue),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Divider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.3f)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.closing_balance),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = CurrencyFormatter.format(closingBalance),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Divider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.3f)
                )
                
                Text(
                    text = NumberToWordsConverter.convertToWords(closingBalance),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Button(
            onClick = { showSaveDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.save_to_history))
        }
    }
    
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text(stringResource(R.string.save_to_history)) },
            text = { Text(stringResource(R.string.save_to_history_confirmation)) },
            confirmButton = {
                Button(
                    onClick = {
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        val entry = com.kholhang.fynical.data.CashBookHistoryEntry(
                            id = java.util.UUID.randomUUID().toString(),
                            date = dateFormat.format(Date()),
                            openingBalance = openingBalanceValue,
                            totalDeposit = totalDepositValue,
                            totalPayment = totalPaymentValue,
                            totalCashInHand = calculatedTotalCashInHand,
                            closingBalance = closingBalance,
                            notesTotal = notesTotal,
                            coinsTotal = coinsTotal,
                            grandTotal = grandTotal
                        )
                        onSaveHistory(entry)
                        showSaveDialog = false
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

