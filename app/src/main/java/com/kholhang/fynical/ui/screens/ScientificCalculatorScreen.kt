package com.kholhang.fynical.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScientificCalculatorScreen(navController: NavController? = null) {
    var display by remember { mutableStateOf("0") }
    var angleMode by remember { mutableStateOf(true) } // true = degrees, false = radians
    var expression by remember { mutableStateOf("") }
    
    fun inputNumber(number: String) {
        if (display == "0" || display == "Error") {
            display = number
        } else {
            display += number
        }
        expression = display
    }
    
    fun inputOperator(op: String) {
        if (display != "Error") {
            display += op
            expression = display
        }
    }
    
    fun formatResult(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toLong().toString()
        } else {
            val formatted = String.format("%.10f", value).trimEnd('0').trimEnd('.')
            if (formatted.isEmpty()) "0" else formatted
        }
    }
    
    fun evaluateTerm(expr: String): Double {
        var expression = expr.trim()
        if (expression.isEmpty()) return 0.0
        
        // Handle power operator (right associative, highest precedence)
        // Find the rightmost ** operator
        var powerIndex = -1
        var depth = 0
        for (i in expression.length - 2 downTo 0) {
            when (expression[i]) {
                ')' -> depth++
                '(' -> depth--
            }
            if (depth == 0 && expression.substring(i, i + 2) == "**") {
                powerIndex = i
                break
            }
        }
        
        if (powerIndex >= 0) {
            val left = expression.substring(0, powerIndex)
            val right = expression.substring(powerIndex + 2)
            val leftVal = evaluateTerm(left)
            val rightVal = evaluateTerm(right)
            if (leftVal.isNaN() || rightVal.isNaN() || leftVal.isInfinite() || rightVal.isInfinite()) {
                return Double.NaN
            }
            return Math.pow(leftVal, rightVal)
        }
        
        // Handle multiplication and division (left to right)
        val parts = mutableListOf<String>()
        var current = StringBuilder()
        var i = 0
        depth = 0
        
        while (i < expression.length) {
            val char = expression[i]
            when (char) {
                '(' -> depth++
                ')' -> depth--
            }
            
            if (depth == 0) {
                when {
                    char == '*' && (i == 0 || expression[i-1] != '*') -> {
                        if (current.isNotEmpty()) {
                            parts.add(current.toString())
                            parts.add("*")
                            current.clear()
                        }
                    }
                    char == '/' -> {
                        if (current.isNotEmpty()) {
                            parts.add(current.toString())
                            parts.add("/")
                            current.clear()
                        }
                    }
                    else -> current.append(char)
                }
            } else {
                current.append(char)
            }
            i++
        }
        
        if (current.isNotEmpty()) {
            parts.add(current.toString())
        }
        
        if (parts.isEmpty()) return expression.toDoubleOrNull() ?: 0.0
        if (parts.size == 1) return parts[0].toDoubleOrNull() ?: 0.0
        
        var result = parts[0].toDoubleOrNull() ?: 0.0
        var idx = 1
        while (idx < parts.size) {
            val op = parts[idx]
            val next = parts[idx + 1].toDoubleOrNull() ?: 0.0
            result = when (op) {
                "*" -> result * next
                "/" -> if (next != 0.0) result / next else Double.NaN
                else -> result
            }
            if (result.isNaN() || result.isInfinite()) return Double.NaN
            idx += 2
        }
        
        return result
    }
    
    fun evaluate(expr: String): Double {
        var expression = expr.trim()
        if (expression.isEmpty()) return 0.0
        
        // Handle parentheses recursively
        var depth = 0
        var start = -1
        for (i in expression.indices) {
            when (expression[i]) {
                '(' -> {
                    if (depth == 0) start = i
                    depth++
                }
                ')' -> {
                    depth--
                    if (depth == 0 && start >= 0) {
                        val subExpr = expression.substring(start + 1, i)
                        val subResult = evaluate(subExpr)
                        expression = expression.substring(0, start) + subResult.toString() + expression.substring(i + 1)
                        return evaluate(expression)
                    }
                }
            }
        }
        
        // Handle addition and subtraction
        var result = 0.0
        var current = StringBuilder()
        var operator = '+'
        var i = 0
        
        while (i < expression.length) {
            val char = expression[i]
            val isOperator = (char == '+' || char == '-') && 
                           (i == 0 || (expression[i-1] != '*' && expression[i-1] != '/' && 
                            expression[i-1] != '^' && expression[i-1] != 'e' && expression[i-1] != 'E'))
            
            when {
                isOperator -> {
                    if (current.isNotEmpty()) {
                        val value = evaluateTerm(current.toString())
                        if (value.isNaN() || value.isInfinite()) return Double.NaN
                        result = when (operator) {
                            '+' -> result + value
                            '-' -> result - value
                            else -> value
                        }
                        current.clear()
                    }
                    operator = char
                }
                else -> current.append(char)
            }
            i++
        }
        
        if (current.isNotEmpty()) {
            val value = evaluateTerm(current.toString())
            if (value.isNaN() || value.isInfinite()) return Double.NaN
            result = when (operator) {
                '+' -> result + value
                '-' -> result - value
                else -> value
            }
        }
        
        return result
    }
    
    fun evaluateArithmetic(expr: String): Double {
        try {
            var expression = expr.replace(" ", "")
            if (expression.isEmpty()) return 0.0
            
            // Handle unary minus
            expression = expression.replace("(-", "(0-")
            if (expression.startsWith("-")) {
                expression = "0$expression"
            }
            
            return evaluate(expression)
        } catch (e: Exception) {
            return Double.NaN
        }
    }
    
    fun findMatchingParenthesis(expr: String, startIndex: Int): Int {
        var depth = 1
        var i = startIndex + 1
        while (i < expr.length && depth > 0) {
            when (expr[i]) {
                '(' -> depth++
                ')' -> depth--
            }
            if (depth == 0) return i
            i++
        }
        return -1
    }
    
    fun evaluateFunction(funcName: String, arg: String): Double {
        try {
            val value = evaluateArithmetic(arg.trim())
            if (value.isNaN() || value.isInfinite()) {
                return Double.NaN
            }
            return when (funcName) {
                "sinDeg" -> sin(Math.toRadians(value))
                "cosDeg" -> cos(Math.toRadians(value))
                "tanDeg" -> tan(Math.toRadians(value))
                "sin" -> sin(value)
                "cos" -> cos(value)
                "tan" -> tan(value)
                "ln" -> if (value > 0) ln(value) else Double.NaN
                "log" -> if (value > 0) log10(value) else Double.NaN
                "√" -> if (value >= 0) sqrt(value) else Double.NaN
                else -> Double.NaN
            }
        } catch (e: Exception) {
            return Double.NaN
        }
    }
    
    fun evaluateExpression(expr: String): Double {
        try {
            var processed = expr.trim()
            if (processed.isEmpty()) return 0.0
            
            // Replace constants first
            processed = processed.replace("π", Math.PI.toString())
            // Be careful with 'e' - only replace standalone 'e', not in numbers like 1e5 or 2e-3
            // Replace 'e' only if it's not part of scientific notation
            processed = processed.replace(Regex("(?<!\\d)(?<!\\.)e(?![-+]?\\d)(?!\\.)"), Math.E.toString())
            
            // Process functions from innermost to outermost by finding the rightmost function
            while (true) {
                var foundFunction = false
                var functionStart = -1
                var functionName = ""
                
                // Find the rightmost function call - check both regular and Deg versions
                val functionNames = listOf("sinDeg", "cosDeg", "tanDeg", "sin", "cos", "tan", "ln", "log", "√")
                for (name in functionNames) {
                    val index = processed.lastIndexOf(name + "(")
                    if (index > functionStart) {
                        functionStart = index
                        functionName = name
                        foundFunction = true
                    }
                }
                
                if (!foundFunction) break
                
                // Find matching closing parenthesis
                val parenStart = functionStart + functionName.length
                if (parenStart >= processed.length || processed[parenStart] != '(') {
                    break // Invalid function call format
                }
                val parenEnd = findMatchingParenthesis(processed, parenStart)
                if (parenEnd == -1) {
                    // Unmatched parenthesis - return error
                    return Double.NaN
                }
                
                // Extract argument
                val arg = processed.substring(parenStart + 1, parenEnd).trim()
                if (arg.isEmpty()) {
                    return Double.NaN // Empty argument
                }
                
                // Convert regular sin/cos/tan to Deg version if in degree mode
                val actualFunctionName = if (angleMode && functionName in listOf("sin", "cos", "tan")) {
                    functionName + "Deg"
                } else {
                    functionName
                }
                
                // Evaluate function
                val funcResult = evaluateFunction(actualFunctionName, arg)
                
                if (funcResult.isNaN() || funcResult.isInfinite()) {
                    return Double.NaN
                }
                
                // Replace function call with result
                processed = processed.substring(0, functionStart) + 
                           funcResult.toString() + 
                           processed.substring(parenEnd + 1)
            }
            
            // Replace operators
            processed = processed.replace("×", "*")
            processed = processed.replace("÷", "/")
            processed = processed.replace("^", "**")
            
            // Evaluate arithmetic expression
            return evaluateArithmetic(processed)
        } catch (e: Exception) {
            return Double.NaN
        }
    }
    
    fun calculate() {
        try {
            var expr = display.trim()
            
            if (expr.isEmpty() || expr == "Error") {
                display = "0"
                return
            }
            
            // Evaluate expression (angle mode is handled inside evaluateExpression)
            val result = evaluateExpression(expr)
            display = if (result.isNaN() || result.isInfinite()) "Error" else formatResult(result)
            expression = expr
        } catch (e: Exception) {
            display = "Error"
            expression = ""
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.scientific_calculator)) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Angle mode toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = angleMode,
                    onClick = { angleMode = true },
                    label = { Text("Deg", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = !angleMode,
                    onClick = { angleMode = false },
                    label = { Text("Rad", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Display (Windows 11 style - right aligned)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
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
                    text = display,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 36.sp),
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            }
            
            // Scientific functions
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Trigonometric functions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScientificCalcButton(
                        text = "sin",
                        onClick = { display = if (display == "0" || display == "Error") "sin(" else display + "sin(" },
                        modifier = Modifier.weight(1f),
                        backgroundColor = MaterialTheme.colorScheme.secondary,
                        textColor = MaterialTheme.colorScheme.onSecondary
                    )
                    ScientificCalcButton(
                        text = "cos",
                        onClick = { display = if (display == "0" || display == "Error") "cos(" else display + "cos(" },
                        modifier = Modifier.weight(1f),
                        backgroundColor = MaterialTheme.colorScheme.secondary,
                        textColor = MaterialTheme.colorScheme.onSecondary
                    )
                    ScientificCalcButton(
                        text = "tan",
                        onClick = { display = if (display == "0" || display == "Error") "tan(" else display + "tan(" },
                        modifier = Modifier.weight(1f),
                        backgroundColor = MaterialTheme.colorScheme.secondary,
                        textColor = MaterialTheme.colorScheme.onSecondary
                    )
                    ScientificCalcButton(
                        text = "1/x",
                        onClick = {
                            val value = display.toDoubleOrNull() ?: 0.0
                            if (value != 0.0) {
                                display = formatResult(1.0 / value)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        backgroundColor = MaterialTheme.colorScheme.secondary,
                        textColor = MaterialTheme.colorScheme.onSecondary
                    )
                }
                
                // Logarithmic and power functions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScientificCalcButton(
                        text = "ln",
                        onClick = { display = if (display == "0" || display == "Error") "ln(" else display + "ln(" },
                        modifier = Modifier.weight(1f),
                        backgroundColor = MaterialTheme.colorScheme.secondary,
                        textColor = MaterialTheme.colorScheme.onSecondary
                    )
                    ScientificCalcButton(
                        text = "log",
                        onClick = { display = if (display == "0" || display == "Error") "log(" else display + "log(" },
                        modifier = Modifier.weight(1f),
                        backgroundColor = MaterialTheme.colorScheme.secondary,
                        textColor = MaterialTheme.colorScheme.onSecondary
                    )
                    ScientificCalcButton(
                        text = "√",
                        onClick = { display = if (display == "0" || display == "Error") "√(" else display + "√(" },
                        modifier = Modifier.weight(1f),
                        backgroundColor = MaterialTheme.colorScheme.secondary,
                        textColor = MaterialTheme.colorScheme.onSecondary
                    )
                    ScientificCalcButton(
                        text = "x^y",
                        onClick = { inputOperator("^") },
                        modifier = Modifier.weight(1f),
                        backgroundColor = MaterialTheme.colorScheme.secondary,
                        textColor = MaterialTheme.colorScheme.onSecondary
                    )
                }
                
                // Constants and operations
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScientificCalcButton(
                        text = "π",
                        onClick = { display = if (display == "0" || display == "Error") Math.PI.toString() else display + Math.PI.toString() },
                        modifier = Modifier.weight(1f),
                        backgroundColor = MaterialTheme.colorScheme.tertiary,
                        textColor = MaterialTheme.colorScheme.onTertiary
                    )
                    ScientificCalcButton(
                        text = "e",
                        onClick = { display = if (display == "0" || display == "Error") Math.E.toString() else display + Math.E.toString() },
                        modifier = Modifier.weight(1f),
                        backgroundColor = MaterialTheme.colorScheme.tertiary,
                        textColor = MaterialTheme.colorScheme.onTertiary
                    )
                    ScientificCalcButton(
                        text = "x²",
                        onClick = {
                            val value = display.toDoubleOrNull() ?: 0.0
                            display = formatResult(value * value)
                        },
                        modifier = Modifier.weight(1f),
                        backgroundColor = MaterialTheme.colorScheme.tertiary,
                        textColor = MaterialTheme.colorScheme.onTertiary
                    )
                    ScientificCalcButton(
                        text = "1/x",
                        onClick = {
                            val value = display.toDoubleOrNull() ?: 0.0
                            if (value != 0.0) {
                                display = formatResult(1.0 / value)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        backgroundColor = MaterialTheme.colorScheme.tertiary,
                        textColor = MaterialTheme.colorScheme.onTertiary
                    )
                }
                
                // Basic operations row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScientificCalcButton(
                        text = "C",
                        onClick = { display = "0"; expression = "" },
                        modifier = Modifier.weight(1f),
                        backgroundColor = MaterialTheme.colorScheme.error,
                        textColor = MaterialTheme.colorScheme.onError
                    )
                    ScientificCalcButton(
                        text = "⌫",
                        onClick = {
                            if (display.isNotEmpty() && display != "Error") {
                                display = display.dropLast(1)
                                if (display.isEmpty()) display = "0"
                            }
                        },
                        modifier = Modifier.weight(1f),
                        backgroundColor = MaterialTheme.colorScheme.error,
                        textColor = MaterialTheme.colorScheme.onError
                    )
                    ScientificCalcButton(
                        text = "(",
                        onClick = { inputOperator("(") },
                        modifier = Modifier.weight(1f),
                        backgroundColor = MaterialTheme.colorScheme.tertiary,
                        textColor = MaterialTheme.colorScheme.onTertiary
                    )
                    ScientificCalcButton(
                        text = ")",
                        onClick = { inputOperator(")") },
                        modifier = Modifier.weight(1f),
                        backgroundColor = MaterialTheme.colorScheme.tertiary,
                        textColor = MaterialTheme.colorScheme.onTertiary
                    )
                }
                
                // Number pad and basic operations
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 7, 8, 9, ÷
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScientificCalcButton(text = "7", onClick = { inputNumber("7") }, modifier = Modifier.weight(1f))
                        ScientificCalcButton(text = "8", onClick = { inputNumber("8") }, modifier = Modifier.weight(1f))
                        ScientificCalcButton(text = "9", onClick = { inputNumber("9") }, modifier = Modifier.weight(1f))
                        ScientificCalcButton(
                            text = "÷",
                            onClick = { inputOperator("÷") },
                            modifier = Modifier.weight(1f),
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            textColor = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    
                    // 4, 5, 6, ×
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScientificCalcButton(text = "4", onClick = { inputNumber("4") }, modifier = Modifier.weight(1f))
                        ScientificCalcButton(text = "5", onClick = { inputNumber("5") }, modifier = Modifier.weight(1f))
                        ScientificCalcButton(text = "6", onClick = { inputNumber("6") }, modifier = Modifier.weight(1f))
                        ScientificCalcButton(
                            text = "×",
                            onClick = { inputOperator("×") },
                            modifier = Modifier.weight(1f),
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            textColor = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    
                    // 1, 2, 3, -
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScientificCalcButton(text = "1", onClick = { inputNumber("1") }, modifier = Modifier.weight(1f))
                        ScientificCalcButton(text = "2", onClick = { inputNumber("2") }, modifier = Modifier.weight(1f))
                        ScientificCalcButton(text = "3", onClick = { inputNumber("3") }, modifier = Modifier.weight(1f))
                        ScientificCalcButton(
                            text = "-",
                            onClick = { inputOperator("-") },
                            modifier = Modifier.weight(1f),
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            textColor = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    
                    // 0, ., +, =
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScientificCalcButton(
                            text = "0",
                            onClick = { inputNumber("0") },
                            modifier = Modifier.weight(2f)
                        )
                        ScientificCalcButton(
                            text = ".",
                            onClick = {
                                if (!display.contains(".")) {
                                    inputNumber(".")
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        ScientificCalcButton(
                            text = "+",
                            onClick = { inputOperator("+") },
                            modifier = Modifier.weight(1f),
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            textColor = MaterialTheme.colorScheme.onPrimary
                        )
                        ScientificCalcButton(
                            text = "=",
                            onClick = { calculate() },
                            modifier = Modifier.weight(1f),
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            textColor = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScientificCalcButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
            fontWeight = FontWeight.Normal,
            color = textColor
        )
    }
}
