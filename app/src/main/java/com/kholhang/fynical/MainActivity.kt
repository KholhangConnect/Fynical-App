package com.kholhang.fynical

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kholhang.fynical.data.SettingsRepository
import com.kholhang.fynical.ui.screens.*
import com.kholhang.fynical.ui.theme.FincalTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val settingsRepository = remember { SettingsRepository(context) }
            
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
            
            LaunchedEffect(settings.keepScreenOn) {
                if (settings.keepScreenOn) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
            
            FincalTheme(darkTheme = settings.isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(navController = navController)
                        }
                        composable("emi") {
                            EMIScreen(navController = navController)
                        }
                        composable("denomination") {
                            DenominationScreen(navController = navController)
                        }
                        composable("settings") {
                            SettingsScreen(navController = navController)
                        }
                        // Basic Calculators
                        composable("fd") {
                            FDCalculatorScreen(navController = navController)
                        }
                        composable("rd") {
                            RDCalculatorScreen(navController = navController)
                        }
                        composable("simple_interest") {
                            SimpleInterestScreen(navController = navController)
                        }
                        composable("compound_interest") {
                            CompoundInterestScreen(navController = navController)
                        }
                        composable("amount_to_words") {
                            AmountToWordsScreen(navController = navController)
                        }
                        composable("discount") {
                            DiscountCalculatorScreen(navController = navController)
                        }
                        // Tax Calculators
                        composable("gst") {
                            GSTCalculatorScreen(navController = navController)
                        }
                        composable("vat") {
                            VATCalculatorScreen(navController = navController)
                        }
                        // Utilities
                        composable("simple_calculator") {
                            SimpleCalculatorScreen(navController = navController)
                        }
                        composable("scientific_calculator") {
                            ScientificCalculatorScreen(navController = navController)
                        }
                        // Loan Tools
                        composable("loan_eligibility") {
                            LoanEligibilityScreen(navController = navController)
                        }
                        composable("prepayment") {
                            PrepaymentCalculatorScreen(navController = navController)
                        }
                        composable("roi_change") {
                            ROIChangeScreen(navController = navController)
                        }
                        composable("moratorium") {
                            MoratoriumCalculatorScreen(navController = navController)
                        }
                        composable("loan_profile") {
                            LoanProfileScreen(navController = navController)
                        }
                        composable("loan_comparison") {
                            LoanComparisonScreen(navController = navController)
                        }
                        composable("advanced_emi") {
                            AdvancedEMIScreen(navController = navController)
                        }
                        composable("emi_quick") {
                            EMIQuickCalculatorScreen(navController = navController)
                        }
                        composable("currency_converter") {
                            CurrencyConverterScreen(navController = navController)
                        }
                        // Additional Financial Calculators
                        composable("sip") {
                            SIPCalculatorScreen(navController = navController)
                        }
                        composable("ppf") {
                            PPFCalculatorScreen(navController = navController)
                        }
                        composable("income_tax") {
                            IncomeTaxCalculatorScreen(navController = navController)
                        }
                        composable("gratuity") {
                            GratuityCalculatorScreen(navController = navController)
                        }
                        composable("epf") {
                            EPFCalculatorScreen(navController = navController)
                        }
                        composable("lumpsum") {
                            LumpsumCalculatorScreen(navController = navController)
                        }
                        composable("inflation") {
                            InflationCalculatorScreen(navController = navController)
                        }
                        composable("amortization") {
                            AmortizationScreen(navController = navController)
                        }
                        composable("about") {
                            AboutScreen(navController = navController)
                        }
                        composable("feedback") {
                            FeedbackScreen(navController = navController)
                        }
                        composable("daily_interest") {
                            DailyInterestCalculatorScreen(navController = navController)
                        }
                        composable("apy") {
                            APYCalculatorScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}
