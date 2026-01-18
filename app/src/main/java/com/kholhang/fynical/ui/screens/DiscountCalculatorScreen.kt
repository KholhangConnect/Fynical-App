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
import com.kholhang.fynical.utils.DiscountCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscountCalculatorScreen(navController: NavController? = null) {
    var originalPrice by remember { mutableStateOf("") }
    var discountValue by remember { mutableStateOf("") }
    var discountMode by remember { mutableStateOf(DiscountCalculator.DiscountMode.PERCENTAGE_OFF) }
    
    var result by remember { mutableStateOf<DiscountCalculator.DiscountResult?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.discount_calculator)) },
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
                value = originalPrice,
                onValueChange = { originalPrice = it },
                label = { Text(stringResource(R.string.original_price)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                leadingIcon = {
                    Text("₹", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
                }
            )
            
            Text(
                text = stringResource(R.string.discount_mode),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = discountMode == DiscountCalculator.DiscountMode.PERCENTAGE_OFF,
                    onClick = { discountMode = DiscountCalculator.DiscountMode.PERCENTAGE_OFF },
                    label = { Text(stringResource(R.string.percentage_off)) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = discountMode == DiscountCalculator.DiscountMode.FIXED_AMOUNT_OFF,
                    onClick = { discountMode = DiscountCalculator.DiscountMode.FIXED_AMOUNT_OFF },
                    label = { Text(stringResource(R.string.fixed_amount_off)) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            OutlinedTextField(
                value = discountValue,
                onValueChange = { discountValue = it },
                label = { 
                    Text(
                        if (discountMode == DiscountCalculator.DiscountMode.PERCENTAGE_OFF) 
                            stringResource(R.string.discount_percentage)
                        else 
                            stringResource(R.string.discount_amount)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                suffix = {
                    if (discountMode == DiscountCalculator.DiscountMode.PERCENTAGE_OFF) {
                        Text("%", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(end = 8.dp))
                    } else {
                        Text("₹", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(end = 8.dp))
                    }
                }
            )
            
            Button(
                onClick = {
                    val price = originalPrice.toDoubleOrNull() ?: 0.0
                    val discount = discountValue.toDoubleOrNull() ?: 0.0
                    
                    if (price > 0 && discount >= 0) {
                        result = when (discountMode) {
                            DiscountCalculator.DiscountMode.PERCENTAGE_OFF -> 
                                DiscountCalculator.calculatePercentageOff(price, discount)
                            DiscountCalculator.DiscountMode.FIXED_AMOUNT_OFF -> 
                                DiscountCalculator.calculateFixedAmountOff(price, discount)
                            else -> null
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
                            Text(stringResource(R.string.original_price))
                            Text(CurrencyFormatter.format(result!!.originalPrice))
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.discount_amount))
                            Text(
                                CurrencyFormatter.format(result!!.discountAmount),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.final_price))
                            Text(
                                CurrencyFormatter.format(result!!.finalPrice),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.savings))
                            Text(
                                CurrencyFormatter.format(result!!.savings),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}


