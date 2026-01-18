package com.kholhang.fynical.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kholhang.fynical.R
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleCalculatorScreen(navController: NavController? = null) {
    var display by remember { mutableStateOf("0") }
    var expression by remember { mutableStateOf("") }
    var operand1 by remember { mutableStateOf(0.0) }
    var operator by remember { mutableStateOf<String?>(null) }
    var waitingForOperand by remember { mutableStateOf(false) }
    var memory by remember { mutableStateOf(0.0) }
    
    val formatter = DecimalFormat("#.##########")
    val numberFormatter = NumberFormat.getNumberInstance(Locale.getDefault())
    
    fun formatDisplay(value: String): String {
        val num = value.toDoubleOrNull()
        return if (num != null) {
            if (num % 1.0 == 0.0) {
                numberFormatter.format(num.toLong())
            } else {
                val formatted = formatter.format(num).replace(",", "")
                if (formatted.length > 12) {
                    String.format("%.9e", num)
                } else {
                    formatted
                }
            }
        } else {
            value
        }
    }
    
    fun inputNumber(number: String) {
        if (waitingForOperand) {
            display = number
            waitingForOperand = false
        } else {
            display = if (display == "0" || display == "Error") number else display + number
        }
    }
    
    fun performOperation() {
        val operand2 = display.toDoubleOrNull() ?: 0.0
        val result = when (operator) {
            "+" -> operand1 + operand2
            "-" -> operand1 - operand2
            "×" -> operand1 * operand2
            "÷" -> if (operand2 != 0.0) operand1 / operand2 else Double.NaN
            else -> operand2
        }
        display = if (result.isNaN() || result.isInfinite()) "Error" else formatter.format(result).replace(",", "")
        expression = ""
        operator = null
        waitingForOperand = true
    }
    
    fun handleOperator(op: String) {
        val inputValue = display.toDoubleOrNull() ?: 0.0
        
        if (operator != null && !waitingForOperand) {
            operand1 = inputValue
            performOperation()
            operand1 = display.toDoubleOrNull() ?: 0.0
        } else {
            operand1 = inputValue
        }
        
        operator = op
        expression = formatDisplay(operand1.toString()) + " " + op
        waitingForOperand = true
    }
    
    fun clear() {
        display = "0"
        operand1 = 0.0
        operator = null
        waitingForOperand = false
        expression = ""
    }
    
    fun clearEntry() {
        display = "0"
        waitingForOperand = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.simple_calculator)) },
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
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Display Area (Windows 11 style - right aligned)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.End
            ) {
                if (expression.isNotEmpty() && expression != display) {
                    Text(
                        text = expression,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Text(
                    text = formatDisplay(display),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp),
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            }
            
            // Calculator Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Row 1: Memory functions (hidden by default, can be shown)
                // Row 2: Functions (CE, C, Backspace, ÷)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalcButton(
                        text = "CE",
                        onClick = { clearEntry() },
                        modifier = Modifier.weight(1f),
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                        textColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    CalcButton(
                        text = "C",
                        onClick = { clear() },
                        modifier = Modifier.weight(1f),
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                        textColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    CalcButton(
                        text = "⌫",
                        onClick = {
                            if (display.length > 1 && display != "Error") {
                                display = display.dropLast(1)
                            } else {
                                display = "0"
                            }
                        },
                        modifier = Modifier.weight(1f),
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                        textColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    CalcButton(
                        text = "÷",
                        onClick = { handleOperator("÷") },
                        modifier = Modifier.weight(1f),
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        textColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                // Row 3: 7, 8, 9, ×
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalcButton(text = "7", onClick = { inputNumber("7") }, modifier = Modifier.weight(1f))
                    CalcButton(text = "8", onClick = { inputNumber("8") }, modifier = Modifier.weight(1f))
                    CalcButton(text = "9", onClick = { inputNumber("9") }, modifier = Modifier.weight(1f))
                    CalcButton(
                        text = "×",
                        onClick = { handleOperator("×") },
                        modifier = Modifier.weight(1f),
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        textColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                // Row 4: 4, 5, 6, -
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalcButton(text = "4", onClick = { inputNumber("4") }, modifier = Modifier.weight(1f))
                    CalcButton(text = "5", onClick = { inputNumber("5") }, modifier = Modifier.weight(1f))
                    CalcButton(text = "6", onClick = { inputNumber("6") }, modifier = Modifier.weight(1f))
                    CalcButton(
                        text = "-",
                        onClick = { handleOperator("-") },
                        modifier = Modifier.weight(1f),
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        textColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                // Row 5: 1, 2, 3, +
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalcButton(text = "1", onClick = { inputNumber("1") }, modifier = Modifier.weight(1f))
                    CalcButton(text = "2", onClick = { inputNumber("2") }, modifier = Modifier.weight(1f))
                    CalcButton(text = "3", onClick = { inputNumber("3") }, modifier = Modifier.weight(1f))
                    CalcButton(
                        text = "+",
                        onClick = { handleOperator("+") },
                        modifier = Modifier.weight(1f),
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        textColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                // Row 6: ±, 0, ., =
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalcButton(
                        text = "±",
                        onClick = {
                            val num = display.toDoubleOrNull()
                            if (num != null && num != 0.0) {
                                display = (-num).toString()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    CalcButton(
                        text = "0",
                        onClick = { inputNumber("0") },
                        modifier = Modifier.weight(1f)
                    )
                    CalcButton(
                        text = ".",
                        onClick = {
                            if (!display.contains(".")) {
                                display += "."
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    CalcButton(
                        text = "=",
                        onClick = { performOperation() },
                        modifier = Modifier.weight(1f),
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        textColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun CalcButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Box(
        modifier = modifier
            .height(72.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
            fontWeight = FontWeight.Normal,
            color = textColor
        )
    }
}
