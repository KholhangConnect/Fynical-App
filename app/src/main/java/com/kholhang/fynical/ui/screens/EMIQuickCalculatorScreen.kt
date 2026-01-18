package com.kholhang.fynical.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kholhang.fynical.R
import com.kholhang.fynical.utils.CurrencyFormatter
import com.kholhang.fynical.utils.EMICalculator
import kotlin.math.PI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EMIQuickCalculatorScreen(navController: NavController? = null) {
    var principal by remember { mutableStateOf(1000000f) }
    var interestRate by remember { mutableStateOf(8.5f) }
    var tenureMonths by remember { mutableStateOf(240f) }
    
    val emi = remember(principal, interestRate, tenureMonths) {
        try {
            if (principal > 0 && interestRate >= 0 && tenureMonths > 0) {
                EMICalculator.calculateEMI(principal.toDouble(), interestRate.toDouble(), tenureMonths.toInt())
            } else {
                0.0
            }
        } catch (e: Exception) {
            0.0
        }
    }
    
    val totalAmount = remember(emi, tenureMonths) {
        try {
            if (emi > 0 && tenureMonths > 0) {
                EMICalculator.calculateTotalAmount(emi, tenureMonths.toInt())
            } else {
                0.0
            }
        } catch (e: Exception) {
            0.0
        }
    }
    
    val totalInterest = remember(principal, totalAmount) {
        try {
            if (principal > 0 && totalAmount > 0) {
                EMICalculator.calculateTotalInterest(principal.toDouble(), totalAmount)
            } else {
                0.0
            }
        } catch (e: Exception) {
            0.0
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.emi_quick_calculator)) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Principal Slider
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
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.principal_amount),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = CurrencyFormatter.format(principal.toDouble()),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = principal,
                        onValueChange = { principal = it },
                        valueRange = 100000f..50000000f,
                        steps = 99
                    )
                }
            }
            
            // Interest Rate Slider
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
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.interest_rate),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${interestRate.toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = interestRate,
                        onValueChange = { interestRate = it },
                        valueRange = 5f..20f,
                        steps = 14
                    )
                }
            }
            
            // Tenure Slider
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
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.loan_tenure),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${tenureMonths.toInt()} ${stringResource(R.string.months)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = tenureMonths,
                        onValueChange = { tenureMonths = it },
                        valueRange = 12f..360f,
                        steps = 28
                    )
                }
            }
            
            // Results Card with Graphics
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.results),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Pie Chart
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        PieChart(
                            principal = principal.toDouble(),
                            interest = totalInterest,
                            modifier = Modifier.size(180.dp)
                        )
                    }
                    
                    // Results
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
                    
                    // Breakdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Principal")
                        Text(CurrencyFormatter.format(principal.toDouble()))
                    }
                }
            }
        }
    }
}

@Composable
fun PieChart(
    principal: Double,
    interest: Double,
    modifier: Modifier = Modifier
) {
    val total = principal + interest
    val principalAngle = ((principal / total) * 360f).toFloat()
    val interestAngle = ((interest / total) * 360f).toFloat()
    
    Canvas(modifier = modifier) {
        val size = size.minDimension
        val radius = size / 2f
        val center = Offset(size / 2f, size / 2f)
        
        // Principal slice
        drawArc(
            color = Color(0xFF4CAF50),
            startAngle = -90f,
            sweepAngle = principalAngle,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius * 2f)
        )
        
        // Interest slice
        drawArc(
            color = Color(0xFFF44336),
            startAngle = -90f + principalAngle,
            sweepAngle = interestAngle,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius * 2f)
        )
        
        // Border
        drawCircle(
            color = Color.Black,
            radius = radius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}


