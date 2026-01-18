package com.kholhang.fynical.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kholhang.fynical.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings)) },
                            onClick = {
                                showMenu = false
                                navController.navigate("settings")
                            },
                            leadingIcon = { Icon(Icons.Default.Settings, null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.about)) },
                            onClick = {
                                showMenu = false
                                navController.navigate("about")
                            },
                            leadingIcon = { Icon(Icons.Default.Info, null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.feedback)) },
                            onClick = {
                                showMenu = false
                                navController.navigate("feedback")
                            },
                            leadingIcon = { Icon(Icons.Default.Email, null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.youtube_tutorial)) },
                            onClick = {
                                showMenu = false
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/channel/YOUR_CHANNEL_ID"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Handle error - could show snackbar
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.PlayArrow, null) }
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
        ) {
            // Tabs for categories
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.basic_calculators), style = MaterialTheme.typography.labelSmall) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.loan_tools), style = MaterialTheme.typography.labelSmall) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(stringResource(R.string.tax_pricing), style = MaterialTheme.typography.labelSmall) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text(stringResource(R.string.utilities), style = MaterialTheme.typography.labelSmall) }
                )
            }
            
            // Content based on selected tab
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    0 -> BasicCalculatorsSection(navController)
                    1 -> LoanToolsSection(navController)
                    2 -> TaxPricingSection(navController)
                    3 -> UtilitiesSection(navController)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun BasicCalculatorsSection(navController: NavController) {
    // Investment Calculators
    CalculatorCard(
        title = stringResource(R.string.fd_calculator),
        description = stringResource(R.string.fd_calculator_desc),
        icon = null,
        onClick = { navController.navigate("fd") },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
    
    CalculatorCard(
        title = stringResource(R.string.rd_calculator),
        description = stringResource(R.string.rd_calculator_desc),
        icon = null,
        onClick = { navController.navigate("rd") },
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    )
    
    CalculatorCard(
        title = stringResource(R.string.sip_calculator),
        description = stringResource(R.string.sip_calculator_desc),
        icon = null,
        onClick = { navController.navigate("sip") },
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
    )
    
    CalculatorCard(
        title = stringResource(R.string.ppf_calculator),
        description = stringResource(R.string.ppf_calculator_desc),
        icon = null,
        onClick = { navController.navigate("ppf") },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
    
    CalculatorCard(
        title = stringResource(R.string.lumpsum_calculator),
        description = stringResource(R.string.lumpsum_calculator_desc),
        icon = null,
        onClick = { navController.navigate("lumpsum") },
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    )
    
    // Interest Calculators
    CalculatorCard(
        title = stringResource(R.string.simple_interest_calculator),
        description = stringResource(R.string.simple_interest_desc),
        icon = null,
        onClick = { navController.navigate("simple_interest") },
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
    )
    
    CalculatorCard(
        title = stringResource(R.string.compound_interest_calculator),
        description = stringResource(R.string.compound_interest_desc),
        icon = null,
        onClick = { navController.navigate("compound_interest") },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
    
    // Utility Calculators
    CalculatorCard(
        title = stringResource(R.string.amount_to_words),
        description = stringResource(R.string.amount_to_words_desc),
        icon = null,
        onClick = { navController.navigate("amount_to_words") },
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    )
    
    CalculatorCard(
        title = stringResource(R.string.inflation_calculator),
        description = stringResource(R.string.inflation_calculator_desc),
        icon = null,
        onClick = { navController.navigate("inflation") },
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
    )
    
    CalculatorCard(
        title = "APY Calculator",
        description = "Calculate Annual Percentage Yield with reverse calculations",
        icon = null,
        onClick = { navController.navigate("apy") },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
}

@Composable
fun LoanToolsSection(navController: NavController) {
    CalculatorCard(
        title = stringResource(R.string.emi_calculator),
        description = stringResource(R.string.calculate_your_loan_emi),
        icon = null,
        onClick = { navController.navigate("emi") },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
    
    CalculatorCard(
        title = stringResource(R.string.emi_quick_calculator),
        description = stringResource(R.string.emi_quick_desc),
        icon = null,
        onClick = { navController.navigate("emi_quick") },
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    )
    
    CalculatorCard(
        title = stringResource(R.string.advanced_emi),
        description = stringResource(R.string.advanced_emi_desc),
        icon = null,
        onClick = { navController.navigate("advanced_emi") },
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
    )
    
    CalculatorCard(
        title = "Amortization Schedule",
        description = "View detailed loan repayment schedule",
        icon = null,
        onClick = { navController.navigate("amortization") },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
    
    CalculatorCard(
        title = "Daily Interest Calculator",
        description = "Calculate interest day-by-day for cash credit & Mudra loans",
        icon = null,
        onClick = { navController.navigate("daily_interest") },
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    )
    
    CalculatorCard(
        title = stringResource(R.string.loan_profile_manager),
        description = stringResource(R.string.loan_profile_desc),
        icon = null,
        onClick = { navController.navigate("loan_profile") },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
    
    CalculatorCard(
        title = stringResource(R.string.loan_eligibility),
        description = stringResource(R.string.loan_eligibility_desc),
        icon = null,
        onClick = { navController.navigate("loan_eligibility") },
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    )
    
    CalculatorCard(
        title = stringResource(R.string.loan_comparison),
        description = stringResource(R.string.loan_comparison_desc),
        icon = null,
        onClick = { navController.navigate("loan_comparison") },
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
    )
    
    CalculatorCard(
        title = stringResource(R.string.prepayment_calculator),
        description = stringResource(R.string.prepayment_calculator_desc),
        icon = null,
        onClick = { navController.navigate("prepayment") },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
    
    CalculatorCard(
        title = stringResource(R.string.roi_change_calculator),
        description = stringResource(R.string.roi_change_desc),
        icon = null,
        onClick = { navController.navigate("roi_change") },
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    )
    
    CalculatorCard(
        title = stringResource(R.string.moratorium_calculator),
        description = stringResource(R.string.moratorium_calculator_desc),
        icon = null,
        onClick = { navController.navigate("moratorium") },
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
    )
}

@Composable
fun TaxPricingSection(navController: NavController) {
    CalculatorCard(
        title = stringResource(R.string.gst_calculator),
        description = stringResource(R.string.gst_calculator_desc),
        icon = null,
        onClick = { navController.navigate("gst") },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
    
    CalculatorCard(
        title = stringResource(R.string.vat_calculator),
        description = stringResource(R.string.vat_calculator_desc),
        icon = null,
        onClick = { navController.navigate("vat") },
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    )
    
    CalculatorCard(
        title = stringResource(R.string.income_tax_calculator),
        description = stringResource(R.string.income_tax_calculator_desc),
        icon = null,
        onClick = { navController.navigate("income_tax") },
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
    )
    
    CalculatorCard(
        title = stringResource(R.string.discount_calculator),
        description = stringResource(R.string.discount_calculator_desc),
        icon = null,
        onClick = { navController.navigate("discount") },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
}

@Composable
fun UtilitiesSection(navController: NavController) {
    val context = LocalContext.current
    CalculatorCard(
        title = stringResource(R.string.denomination_manager),
        description = stringResource(R.string.manage_rupees_and_coins),
        icon = null,
        onClick = { navController.navigate("denomination") },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
    
    CalculatorCard(
        title = stringResource(R.string.currency_converter),
        description = stringResource(R.string.currency_converter_desc),
        icon = null,
        onClick = { navController.navigate("currency_converter") },
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    )
    
    CalculatorCard(
        title = stringResource(R.string.simple_calculator),
        description = stringResource(R.string.simple_calculator_desc),
        icon = null,
        onClick = { navController.navigate("simple_calculator") },
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
    )
    
    CalculatorCard(
        title = stringResource(R.string.scientific_calculator),
        description = stringResource(R.string.scientific_calculator_desc),
        icon = null,
        onClick = { navController.navigate("scientific_calculator") },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
    
    CalculatorCard(
        title = stringResource(R.string.gratuity_calculator),
        description = stringResource(R.string.gratuity_calculator_desc),
        icon = null,
        onClick = { navController.navigate("gratuity") },
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    )
    
    CalculatorCard(
        title = stringResource(R.string.epf_calculator),
        description = stringResource(R.string.epf_calculator_desc),
        icon = null,
        onClick = { navController.navigate("epf") },
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
    )
    
    // Additional Pages
    Divider(modifier = Modifier.padding(vertical = 8.dp))
    
    CalculatorCard(
        title = stringResource(R.string.settings),
        description = "Configure app preferences and display settings",
        icon = null,
        onClick = { navController.navigate("settings") },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
    
    CalculatorCard(
        title = stringResource(R.string.about),
        description = "Learn more about Fincal app",
        icon = null,
        onClick = { navController.navigate("about") },
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    )
    
    CalculatorCard(
        title = stringResource(R.string.feedback),
        description = "Share your feedback and suggestions",
        icon = null,
        onClick = { navController.navigate("feedback") },
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
    )
    
    CalculatorCard(
        title = stringResource(R.string.youtube_tutorial),
        description = stringResource(R.string.tutorial_description),
        icon = null,
        onClick = {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/channel/YOUR_CHANNEL_ID"))
                context.startActivity(intent)
            } catch (e: Exception) {
                // Handle error
            }
        },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
}


