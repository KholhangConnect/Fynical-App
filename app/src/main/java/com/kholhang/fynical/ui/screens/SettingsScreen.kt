package com.kholhang.fynical.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kholhang.fynical.R
import com.kholhang.fynical.data.*
import com.kholhang.fynical.utils.CurrencyFormatter
import com.kholhang.fynical.utils.NumberToWordsConverter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }
    val historyRepository = remember { HistoryRepository(context) }
    val denominationRepository = remember { DenominationRepository(context) }
    val scope = rememberCoroutineScope()
    
    val settings by settingsRepository.getSettingsFlow().collectAsState(
        initial = AppSettings(
            isDarkMode = false,
            keepScreenOn = false,
            hiddenDenominations = emptySet(),
            cardSizeMultiplier = 1.0f,
            textSizeMultiplier = 1.0f,
            cardPadding = 1.0f,
            cardSpacing = 6.0f
        )
    )
    
    val history by historyRepository.getHistoryFlow().collectAsState(initial = emptyList())
    val notes by denominationRepository.getRupeesFlow().collectAsState(initial = emptyList())
    val coins by denominationRepository.getCoinsFlow().collectAsState(initial = emptyList())
    
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.appearance),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.dark_mode))
                        Switch(
                            checked = settings.isDarkMode,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    settingsRepository.setDarkMode(enabled)
                                }
                            }
                        )
                    }
                }
            }
            
            // Display Settings
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
                        text = stringResource(R.string.display_settings),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.keep_screen_on))
                        Switch(
                            checked = settings.keepScreenOn,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    settingsRepository.setKeepScreenOn(enabled)
                                }
                            }
                        )
                    }
                    
                    Divider()
                    
                    Text(stringResource(R.string.card_size))
                    Slider(
                        value = settings.cardSizeMultiplier,
                        onValueChange = { value ->
                            scope.launch {
                                settingsRepository.setCardSizeMultiplier(value)
                            }
                        },
                        valueRange = 0.5f..2.0f,
                        steps = 14
                    )
                    Text(
                        text = "${(settings.cardSizeMultiplier * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    Divider()
                    
                    Text(stringResource(R.string.text_size))
                    Slider(
                        value = settings.textSizeMultiplier,
                        onValueChange = { value ->
                            scope.launch {
                                settingsRepository.setTextSizeMultiplier(value)
                            }
                        },
                        valueRange = 0.5f..2.0f,
                        steps = 14
                    )
                    Text(
                        text = "${(settings.textSizeMultiplier * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    Divider()
                    
                    Text(stringResource(R.string.card_padding))
                    Slider(
                        value = settings.cardPadding,
                        onValueChange = { value ->
                            scope.launch {
                                settingsRepository.setCardPadding(value)
                            }
                        },
                        valueRange = 0.3f..2.5f,
                        steps = 21
                    )
                    Text(
                        text = "${(settings.cardPadding * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    Divider()
                    
                    Text(stringResource(R.string.card_spacing))
                    Slider(
                        value = settings.cardSpacing,
                        onValueChange = { value ->
                            scope.launch {
                                settingsRepository.setCardSpacing(value)
                            }
                        },
                        valueRange = 0f..24f,
                        steps = 23
                    )
                    Text(
                        text = "${settings.cardSpacing.toInt()}dp",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (settings.cardSpacing <= 1f) {
                        Text(
                            text = stringResource(R.string.card_spacing_divider_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Denomination Visibility
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
                    Text(
                        text = stringResource(R.string.denomination_visibility),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = stringResource(R.string.notes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    notes.forEach { note ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = CurrencyFormatter.format(note.value),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(
                                checked = !settings.hiddenDenominations.contains(note.id),
                                onCheckedChange = { visible ->
                                    scope.launch {
                                        if (visible) {
                                            settingsRepository.setHiddenDenominations(
                                                settings.hiddenDenominations - note.id
                                            )
                                        } else {
                                            settingsRepository.setHiddenDenominations(
                                                settings.hiddenDenominations + note.id
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                    
                    Divider()
                    
                    Text(
                        text = stringResource(R.string.coins),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    coins.forEach { coin ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = CurrencyFormatter.format(coin.value),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(
                                checked = !settings.hiddenDenominations.contains(coin.id),
                                onCheckedChange = { visible ->
                                    scope.launch {
                                        if (visible) {
                                            settingsRepository.setHiddenDenominations(
                                                settings.hiddenDenominations - coin.id
                                            )
                                        } else {
                                            settingsRepository.setHiddenDenominations(
                                                settings.hiddenDenominations + coin.id
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
            
            // History Management
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.history_management),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showHistoryDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = stringResource(R.string.view_history)
                            )
                        }
                    }
                    
                    Text(
                        text = stringResource(R.string.total_entries, history.size),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Button(
                        onClick = { showClearHistoryDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.clear_history))
                    }
                }
            }
        }
    }
    
    // History Dialog
    if (showHistoryDialog) {
        HistoryDialog(
            history = history,
            onDismiss = { showHistoryDialog = false },
            onDeleteEntry = { id ->
                scope.launch {
                    historyRepository.deleteHistoryEntry(id)
                }
            }
        )
    }
    
    // Clear History Confirmation Dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text(stringResource(R.string.clear_history)) },
            text = { Text(stringResource(R.string.clear_history_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            historyRepository.clearHistory()
                            showClearHistoryDialog = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.clear))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun HistoryDialog(
    history: List<CashBookHistoryEntry>,
    onDismiss: () -> Unit,
    onDeleteEntry: (String) -> Unit
) {
    var entryToDelete by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.cash_book_history)) },
        text = {
            if (history.isEmpty()) {
                Text(stringResource(R.string.no_history_entries))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    history.forEach { entry ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = entry.date,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = { entryToDelete = entry.id },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.delete),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = stringResource(R.string.opening_balance) + ": " + CurrencyFormatter.format(entry.openingBalance),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = stringResource(R.string.total_deposit) + ": " + CurrencyFormatter.format(entry.totalDeposit),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = stringResource(R.string.total_payment) + ": " + CurrencyFormatter.format(entry.totalPayment),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = stringResource(R.string.closing_balance) + ": " + CurrencyFormatter.format(entry.closingBalance),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.grand_total) + ": " + CurrencyFormatter.format(entry.grandTotal),
                                    style = MaterialTheme.typography.bodySmall
                                )
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
                        onDeleteEntry(id)
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

