package com.kholhang.fynical.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.kholhang.fynical.R
import com.kholhang.fynical.utils.AmortizationCalculator
import com.kholhang.fynical.utils.CurrencyFormatter
import com.kholhang.fynical.utils.showToast
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmortizationScreen(navController: NavController? = null) {
    var principal by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("") }
    var tenureMonths by remember { mutableStateOf("") }
    var showYearlyView by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val schedule = remember(principal, interestRate, tenureMonths) {
        try {
            if (principal.isBlank() || interestRate.isBlank() || tenureMonths.isBlank()) {
                return@remember null
            }
            
            val p = principal.toDoubleOrNull()
            val r = interestRate.toDoubleOrNull()
            val t = tenureMonths.toIntOrNull()
            
            if (p == null || p <= 0) {
                return@remember null
            }
            if (r == null || r < 0 || r > 100) {
                return@remember null
            }
            if (t == null || t <= 0) {
                return@remember null
            }
            
            AmortizationCalculator.calculateSchedule(p, r, t)
        } catch (e: Exception) {
            null
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Amortization Schedule") },
                navigationIcon = {
                    if (navController != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    }
                },
                actions = {
                    if (schedule != null) {
                        IconButton(onClick = { showShareDialog = true }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Input fields
            OutlinedTextField(
                value = principal,
                onValueChange = { principal = it },
                label = { Text("Loan Amount") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Default.AccountCircle, null) },
                singleLine = true
            )
            
            OutlinedTextField(
                value = interestRate,
                onValueChange = { interestRate = it },
                label = { Text("Interest Rate (Annual %)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Default.AccountCircle, null) },
                suffix = { Text("%") },
                singleLine = true
            )
            
            OutlinedTextField(
                value = tenureMonths,
                onValueChange = { tenureMonths = it },
                label = { Text("Tenure (Months)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Icon(Icons.Default.Settings, null) },
                suffix = { Text("months") },
                singleLine = true
            )
            
            Button(
                onClick = {
                    if (principal.isBlank()) {
                        showToast(context, "Please enter loan amount")
                        return@Button
                    }
                    if (interestRate.isBlank()) {
                        showToast(context, "Please enter interest rate")
                        return@Button
                    }
                    if (tenureMonths.isBlank()) {
                        showToast(context, "Please enter tenure")
                        return@Button
                    }
                    
                    val p = principal.toDoubleOrNull()
                    val r = interestRate.toDoubleOrNull()
                    val t = tenureMonths.toIntOrNull()
                    
                    if (p == null || p <= 0) {
                        showToast(context, "Please enter a valid loan amount")
                        return@Button
                    }
                    if (r == null || r < 0 || r > 100) {
                        showToast(context, "Please enter a valid interest rate (0-100%)")
                        return@Button
                    }
                    if (t == null || t <= 0) {
                        showToast(context, "Please enter a valid tenure (in months)")
                        return@Button
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Calculate Schedule")
            }
            
            // Summary Card
            if (schedule != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Loan Summary",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Divider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("EMI:")
                            Text(
                                CurrencyFormatter.format(schedule.emi),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Amount:")
                            Text(CurrencyFormatter.format(schedule.totalAmount))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Interest:")
                            Text(
                                CurrencyFormatter.format(schedule.totalInterest),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                
                // Toggle view
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !showYearlyView,
                        onClick = { showYearlyView = false },
                        label = { Text("Monthly") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = showYearlyView,
                        onClick = { showYearlyView = true },
                        label = { Text("Yearly") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Schedule Table
                if (showYearlyView) {
                    YearlyScheduleView(schedule)
                } else {
                    MonthlyScheduleView(schedule)
                }
            }
        }
    }
    
    // Share Dialog
    if (showShareDialog && schedule != null) {
        ShareDialog(
            onDismiss = { showShareDialog = false },
            onSharePDF = {
                scope.launch {
                    shareAsPDF(context, schedule)
                    showShareDialog = false
                }
            },
            onShareImage = {
                scope.launch {
                    shareAsImage(context, schedule)
                    showShareDialog = false
                }
            }
        )
    }
}

@Composable
fun MonthlyScheduleView(schedule: AmortizationCalculator.AmortizationSchedule) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Month", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Principal", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Interest", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Balance", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            }
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            
            // Show first 12 months and last 12 months, with option to show all
            val entries = schedule.entries
            val showAll = entries.size <= 24
            
            if (showAll) {
                entries.forEach { entry ->
                    ScheduleRow(entry)
                }
            } else {
                // First 12 months
                entries.take(12).forEach { entry ->
                    ScheduleRow(entry)
                }
                Text("... (${entries.size - 24} more months) ...", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(8.dp))
                // Last 12 months
                entries.takeLast(12).forEach { entry ->
                    ScheduleRow(entry)
                }
            }
        }
    }
}

@Composable
fun YearlyScheduleView(schedule: AmortizationCalculator.AmortizationSchedule) {
    val yearlySummary = remember(schedule) {
        AmortizationCalculator.getYearlySummary(schedule)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Year", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Principal", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Interest", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Total", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Balance", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            }
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            
            yearlySummary.forEach { summary ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${summary.year}", modifier = Modifier.weight(1f))
                    Text(CurrencyFormatter.format(summary.totalPrincipalPaid), modifier = Modifier.weight(1f))
                    Text(CurrencyFormatter.format(summary.totalInterestPaid), modifier = Modifier.weight(1f))
                    Text(CurrencyFormatter.format(summary.totalPaid), modifier = Modifier.weight(1f))
                    Text(CurrencyFormatter.format(summary.remainingBalance), modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ScheduleRow(entry: AmortizationCalculator.AmortizationEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("${entry.month}", modifier = Modifier.weight(1f))
        Text(CurrencyFormatter.format(entry.principalPayment), modifier = Modifier.weight(1f))
        Text(CurrencyFormatter.format(entry.interestPayment), modifier = Modifier.weight(1f))
        Text(CurrencyFormatter.format(entry.endingBalance), modifier = Modifier.weight(1f))
    }
}

@Composable
fun ShareDialog(
    onDismiss: () -> Unit,
    onSharePDF: () -> Unit,
    onShareImage: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share Amortization Schedule") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Choose format to share:")
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onSharePDF) {
                    Text("PDF")
                }
                Button(onClick = onShareImage) {
                    Text("Image")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

suspend fun shareAsPDF(context: Context, schedule: AmortizationCalculator.AmortizationSchedule) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        
        var y = 50f
        val paint = android.graphics.Paint()
        paint.textSize = 12f
        
        // Title
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Amortization Schedule", 50f, y, paint)
        y += 30f
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        
        // Summary
        canvas.drawText("Loan Amount: ${CurrencyFormatter.format(schedule.principal)}", 50f, y, paint)
        y += 20f
        canvas.drawText("Interest Rate: ${schedule.annualRate}%", 50f, y, paint)
        y += 20f
        canvas.drawText("Tenure: ${schedule.tenureMonths} months", 50f, y, paint)
        y += 20f
        canvas.drawText("EMI: ${CurrencyFormatter.format(schedule.emi)}", 50f, y, paint)
        y += 20f
        canvas.drawText("Total Amount: ${CurrencyFormatter.format(schedule.totalAmount)}", 50f, y, paint)
        y += 20f
        canvas.drawText("Total Interest: ${CurrencyFormatter.format(schedule.totalInterest)}", 50f, y, paint)
        y += 30f
        
        // Table header
        paint.isFakeBoldText = true
        canvas.drawText("Month", 50f, y, paint)
        canvas.drawText("Principal", 150f, y, paint)
        canvas.drawText("Interest", 250f, y, paint)
        canvas.drawText("Balance", 350f, y, paint)
        y += 20f
        paint.isFakeBoldText = false
        
        // Table rows (first 50 entries to fit on page)
        var currentCanvas = canvas
        var currentPage = page
        schedule.entries.take(50).forEach { entry ->
            if (y > 800f) {
                pdfDocument.finishPage(currentPage)
                currentPage = pdfDocument.startPage(pageInfo)
                currentCanvas = currentPage.canvas
                y = 50f
            }
            currentCanvas.drawText("${entry.month}", 50f, y, paint)
            currentCanvas.drawText(CurrencyFormatter.format(entry.principalPayment), 150f, y, paint)
            currentCanvas.drawText(CurrencyFormatter.format(entry.interestPayment), 250f, y, paint)
            currentCanvas.drawText(CurrencyFormatter.format(entry.endingBalance), 350f, y, paint)
            y += 15f
        }
        
        pdfDocument.finishPage(currentPage)
        
        // Save to file
        val file = File(context.getExternalFilesDir(null), "amortization_schedule_${System.currentTimeMillis()}.pdf")
        val fileOutputStream = FileOutputStream(file)
        pdfDocument.writeTo(fileOutputStream)
        pdfDocument.close()
        fileOutputStream.close()
        
        // Share
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } else {
            Uri.fromFile(file)
        }
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(shareIntent, "Share PDF"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

suspend fun shareAsImage(context: Context, schedule: AmortizationCalculator.AmortizationSchedule) {
    try {
        // Create a simple text representation as image
        val width = 800
        val height = 1200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        
        val paint = android.graphics.Paint()
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 24f
        paint.isFakeBoldText = true
        
        var y = 50f
        
        // Title
        canvas.drawText("Amortization Schedule", 50f, y, paint)
        y += 40f
        
        paint.textSize = 16f
        paint.isFakeBoldText = false
        
        // Summary
        canvas.drawText("Loan Amount: ${CurrencyFormatter.format(schedule.principal)}", 50f, y, paint)
        y += 30f
        canvas.drawText("Interest Rate: ${schedule.annualRate}%", 50f, y, paint)
        y += 30f
        canvas.drawText("Tenure: ${schedule.tenureMonths} months", 50f, y, paint)
        y += 30f
        canvas.drawText("EMI: ${CurrencyFormatter.format(schedule.emi)}", 50f, y, paint)
        y += 30f
        canvas.drawText("Total Amount: ${CurrencyFormatter.format(schedule.totalAmount)}", 50f, y, paint)
        y += 30f
        canvas.drawText("Total Interest: ${CurrencyFormatter.format(schedule.totalInterest)}", 50f, y, paint)
        y += 40f
        
        // Table header
        paint.isFakeBoldText = true
        paint.textSize = 14f
        canvas.drawText("Month", 50f, y, paint)
        canvas.drawText("Principal", 200f, y, paint)
        canvas.drawText("Interest", 350f, y, paint)
        canvas.drawText("Balance", 500f, y, paint)
        y += 30f
        paint.isFakeBoldText = false
        paint.textSize = 12f
        
        // Table rows (first 40 entries)
        schedule.entries.take(40).forEach { entry ->
            canvas.drawText("${entry.month}", 50f, y, paint)
            canvas.drawText(CurrencyFormatter.format(entry.principalPayment), 200f, y, paint)
            canvas.drawText(CurrencyFormatter.format(entry.interestPayment), 350f, y, paint)
            canvas.drawText(CurrencyFormatter.format(entry.endingBalance), 500f, y, paint)
            y += 25f
        }
        
        // Save to file
        val file = File(context.getExternalFilesDir(null), "amortization_schedule_${System.currentTimeMillis()}.png")
        val fileOutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.close()
        
        // Share
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } else {
            Uri.fromFile(file)
        }
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

