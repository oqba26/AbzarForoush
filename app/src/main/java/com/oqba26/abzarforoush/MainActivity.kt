package com.oqba26.abzarforoush

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oqba26.abzarforoush.data.AppDatabase
import com.oqba26.abzarforoush.data.ProductRepository
import com.oqba26.abzarforoush.data.ProductViewModel
import com.oqba26.abzarforoush.data.ProductViewModelFactory
import com.oqba26.abzarforoush.data.SettingsManager
import com.oqba26.abzarforoush.ui.components.AppBottomBar
import com.oqba26.abzarforoush.ui.screens.AccountingScreen
import com.oqba26.abzarforoush.ui.screens.CashbookScreen
import com.oqba26.abzarforoush.ui.screens.ChequeScreen
import com.oqba26.abzarforoush.ui.screens.CustomerScreen
import com.oqba26.abzarforoush.ui.screens.SupplierScreen
import com.oqba26.abzarforoush.ui.screens.HistoryScreen
import com.oqba26.abzarforoush.ui.screens.LoginScreen
import com.oqba26.abzarforoush.ui.screens.ProductScreen
import com.oqba26.abzarforoush.ui.screens.ReportScreen
import com.oqba26.abzarforoush.ui.screens.SettingsScreen
import com.oqba26.abzarforoush.ui.theme.*
import com.oqba26.abzarforoush.util.SupabaseManager
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Supabase if enabled
        val tempSettings = SettingsManager(this)
        runBlocking {
            val url = tempSettings.supabaseUrl.first()
            val key = tempSettings.supabaseKey.first()
            val isEnabled = tempSettings.isSyncEnabled.first()
            if (isEnabled && url.isNotBlank() && key.isNotBlank()) {
                try {
                    SupabaseManager.init(url, key)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val settingsManager = remember { SettingsManager(context) }
            val selectedFontName by settingsManager.selectedFont.collectAsState(initial = "Vazirmatn")
            val selectedThemeName by settingsManager.selectedTheme.collectAsState(initial = "Purple")

            val fontFamily = when (selectedFontName) {
                "Sahel" -> Sahel
                "Estedad" -> Estedad
                "BYekan" -> BYekan
                "IranianSans" -> IranianSans
                else -> Vazirmatn
            }

            AbzarForoushTheme(fontFamily = fontFamily, themeName = selectedThemeName) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val database = AppDatabase.getDatabase(context)
                    val repository = ProductRepository(
                        database.productDao(), 
                        database.invoiceDao(),
                        database.customerDao(),
                        database.debtTransactionDao(),
                        database.bundleDao(),
                        database.expenseDao(),
                        database.supplierDao(),
                        database.chequeDao()
                    )
                    val factory = ProductViewModelFactory(repository)
                    val viewModel: ProductViewModel = viewModel(factory = factory)

                    val supabaseClient = SupabaseManager.getClient()
                    val initialScreen = if (supabaseClient?.auth?.currentSessionOrNull() != null) "products" else "login"
                    
                    var currentScreen by remember { mutableStateOf(initialScreen) }
                    var showExitDialog by remember { mutableStateOf(false) }
                    var showCartSheet by remember { mutableStateOf(false) }

                    val cartItems by viewModel.cartItems.collectAsState()

                    val filePickerLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.GetContent()
                    ) { uri ->
                        uri?.let { 
                            val contentResolver = context.contentResolver
                            val type = contentResolver.getType(it)
                            val fileName = it.path ?: ""
                            
                            if (type == "application/json" || fileName.endsWith(".json")) {
                                viewModel.importFullBackup(it, context)
                            } else if (type?.contains("spreadsheetml") == true || fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
                                viewModel.importFromExcel(it, context)
                            } else {
                                viewModel.importFromCsv(it, context)
                            }
                        }
                    }

                    if (showExitDialog) {
                        Dialog(onDismissRequest = { showExitDialog = false }) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                Surface(
                                    shape = MaterialTheme.shapes.extraLarge,
                                    tonalElevation = 6.dp,
                                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(24.dp)) {
                                        Text(
                                            text = "خروج از برنامه",
                                            style = MaterialTheme.typography.headlineSmall,
                                            modifier = Modifier.padding(bottom = 16.dp)
                                        )
                                        Text(
                                            text = "آیا مطمئن هستید که می‌خواهید از برنامه خارج شوید؟",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = { finish() },
                                                modifier = Modifier.weight(1f),
                                                shape = MaterialTheme.shapes.small,
                                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary
                                                )
                                            ) {
                                                Text("تایید", color = MaterialTheme.colorScheme.onPrimary)
                                            }
                                            Button(
                                                onClick = { showExitDialog = false },
                                                modifier = Modifier.weight(1f),
                                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.error
                                                ),
                                                shape = MaterialTheme.shapes.small
                                            ) {
                                                Text("انصراف", color = MaterialTheme.colorScheme.onError)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    BackHandler {
                        if (currentScreen != "products") {
                            currentScreen = "products"
                        } else {
                            showExitDialog = true
                        }
                    }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            if (currentScreen != "login") {
                                AppBottomBar(
                                    currentScreen = currentScreen,
                                    cartItemCount = cartItems.size,
                                    onNavigateToProducts = { currentScreen = "products" },
                                    onNavigateToSettings = { currentScreen = "settings" },
                                    onNavigateToHistory = { currentScreen = "history" },
                                    onNavigateToCustomers = { currentScreen = "customers" },
                                    onNavigateToReports = { currentScreen = "reports" },
                                    onNavigateToAccounting = { currentScreen = "accounting" },
                                    onShowCart = { 
                                        if (currentScreen == "products") {
                                            showCartSheet = true
                                        } else {
                                            currentScreen = "products"
                                            showCartSheet = true
                                        }
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (currentScreen) {
                                "login" -> LoginScreen(onLoginSuccess = { currentScreen = "products" })
                                "products" -> ProductScreen(
                                    viewModel = viewModel,
                                    externalShowCart = showCartSheet,
                                    onCloseCart = { showCartSheet = false },
                                    onImportCsv = { filePickerLauncher.launch("text/*") },
                                    onImportExcel = { filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") },
                                    onExportExcel = { viewModel.exportToExcel(context) },
                                    onImportBackup = { filePickerLauncher.launch("*/*") },
                                    onExportBackup = { viewModel.exportFullBackup(context) }
                                )
                                "settings" -> SettingsScreen(
                                    settingsManager = settingsManager,
                                    onNavigateBack = { currentScreen = "products" },
                                    onLogout = { currentScreen = "login" }
                                )
                                "history" -> HistoryScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { currentScreen = "products" }
                                )
                                "customers" -> CustomerScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { currentScreen = "products" }
                                )
                                "reports" -> ReportScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { currentScreen = "products" }
                                )
                                "accounting" -> AccountingScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { currentScreen = "products" },
                                    onNavigateToSuppliers = { currentScreen = "suppliers" },
                                    onNavigateToCheques = { currentScreen = "cheques" },
                                    onNavigateToCashbook = { currentScreen = "cashbook" }
                                )
                                "suppliers" -> SupplierScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { currentScreen = "accounting" }
                                )
                                "cheques" -> ChequeScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { currentScreen = "accounting" }
                                )
                                "cashbook" -> CashbookScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { currentScreen = "accounting" }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
