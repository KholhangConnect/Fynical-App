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
import com.kholhang.fynical.api.CurrencyApiService
import com.kholhang.fynical.utils.CurrencyConverter
import com.kholhang.fynical.utils.CurrencyFormatter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

val CURRENCIES = listOf(
    "USD", "EUR", "GBP", "INR", "JPY", "AUD", "CAD", "CHF", "CNY", "SGD"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConverterScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val currencyConverter = remember { CurrencyConverter(context) }
    val apiService = remember { CurrencyApiService.create() }
    val scope = rememberCoroutineScope()
    
    var amount by remember { mutableStateOf("") }
    var fromCurrency by remember { mutableStateOf("USD") }
    var toCurrency by remember { mutableStateOf("INR") }
    var exchangeRate by remember { mutableStateOf<Double?>(null) }
    var convertedAmount by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lastUpdated by remember { mutableStateOf<String?>(null) }
    var isOnline by remember { mutableStateOf(true) }
    var cachedRates by remember { mutableStateOf<Map<String, Double>?>(null) }
    
    // Load cached rates
    LaunchedEffect(Unit) {
        currencyConverter.getCachedRatesFlow().collect { rates ->
            cachedRates = rates
            if (rates != null && fromCurrency in rates && toCurrency in rates) {
                val rate = if (fromCurrency == "USD") {
                    rates[toCurrency] ?: 1.0
                } else if (toCurrency == "USD") {
                    1.0 / (rates[fromCurrency] ?: 1.0)
                } else {
                    val fromRate = rates[fromCurrency] ?: 1.0
                    val toRate = rates[toCurrency] ?: 1.0
                    toRate / fromRate
                }
                exchangeRate = rate
                val amt = amount.toDoubleOrNull() ?: 0.0
                convertedAmount = currencyConverter.convert(amt, fromCurrency, toCurrency, rate)
            }
        }
    }
    
    fun fetchRates() {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                val response = apiService.getLatestRates()
                val ratesMap = response.rates.toMutableMap()
                ratesMap["USD"] = 1.0 // Add USD to rates
                
                currencyConverter.saveRates(ratesMap)
                cachedRates = ratesMap
                
                // Calculate exchange rate
                val rate = if (fromCurrency == "USD") {
                    ratesMap[toCurrency] ?: 1.0
                } else if (toCurrency == "USD") {
                    1.0 / (ratesMap[fromCurrency] ?: 1.0)
                } else {
                    val fromRate = ratesMap[fromCurrency] ?: 1.0
                    val toRate = ratesMap[toCurrency] ?: 1.0
                    toRate / fromRate
                }
                
                exchangeRate = rate
                val amt = amount.toDoubleOrNull() ?: 0.0
                convertedAmount = currencyConverter.convert(amt, fromCurrency, toCurrency, rate)
                lastUpdated = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                isOnline = true
            } catch (e: Exception) {
                isOnline = false
                if (cachedRates == null) {
                    errorMessage = "Connection failed. If the problem persists, please check your internet connection or VPN solution for this"
                } else {
                    errorMessage = "Using cached rates. Connection failed. If the problem persists, please check your internet connection or VPN solution for this"
                }
            } finally {
                isLoading = false
            }
        }
    }
    
    // Auto-fetch on screen load
    LaunchedEffect(Unit) {
        fetchRates()
    }
    
    // Update conversion when inputs change
    LaunchedEffect(amount, fromCurrency, toCurrency, exchangeRate) {
        val amt = amount.toDoubleOrNull() ?: 0.0
        val rate = exchangeRate ?: 1.0
        convertedAmount = currencyConverter.convert(amt, fromCurrency, toCurrency, rate)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.currency_converter)) },
                navigationIcon = {
                    if (navController != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { fetchRates() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.refresh_rates)
                        )
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
            if (isLoading) {
                CircularProgressIndicator()
            }
            
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOnline) 
                            MaterialTheme.colorScheme.tertiaryContainer 
                        else 
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage ?: "",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            if (lastUpdated != null) {
                Text(
                    text = "${stringResource(R.string.last_updated)}: $lastUpdated",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text(stringResource(R.string.amount_to_convert)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var fromExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = fromExpanded,
                    onExpandedChange = { fromExpanded = !fromExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = fromCurrency,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.from_currency)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = fromExpanded,
                        onDismissRequest = { fromExpanded = false }
                    ) {
                        CURRENCIES.forEach { currency ->
                            DropdownMenuItem(
                                text = { Text(currency) },
                                onClick = {
                                    fromCurrency = currency
                                    fromExpanded = false
                                    fetchRates()
                                }
                            )
                        }
                    }
                }
                
                IconButton(
                    onClick = {
                        val temp = fromCurrency
                        fromCurrency = toCurrency
                        toCurrency = temp
                        fetchRates()
                    }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Swap")
                }
                
                var toExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = toExpanded,
                    onExpandedChange = { toExpanded = !toExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = toCurrency,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.to_currency)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = toExpanded,
                        onDismissRequest = { toExpanded = false }
                    ) {
                        CURRENCIES.forEach { currency ->
                            DropdownMenuItem(
                                text = { Text(currency) },
                                onClick = {
                                    toCurrency = currency
                                    toExpanded = false
                                    fetchRates()
                                }
                            )
                        }
                    }
                }
            }
            
            if (exchangeRate != null) {
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
                            Text(stringResource(R.string.exchange_rate))
                            Text(
                                "1 $fromCurrency = ${String.format("%.4f", exchangeRate)} $toCurrency",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Divider()
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                stringResource(R.string.converted_amount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${String.format("%.2f", convertedAmount)} $toCurrency",
                                style = MaterialTheme.typography.titleLarge,
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

