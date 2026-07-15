package com.oqba26.abzarforoush

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import android.os.Build
import android.provider.Settings
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import java.io.File
import androidx.core.net.toUri
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import io.github.jan.supabase.auth.status.SessionStatus
import com.oqba26.abzarforoush.util.UpdateManager
import com.oqba26.abzarforoush.util.UpdateInfo
import com.oqba26.abzarforoush.ui.components.UpdateDialog
import com.oqba26.abzarforoush.util.RealTimeProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        cleanupInstaller()

        val tempSettings = SettingsManager(this)
        var isAlreadyLoggedIn = false
        var syncEnabled = true
        var initialFont = "Estedad"
        var initialTheme = "Purple"
        runBlocking {
            initialFont = tempSettings.selectedFont.first()
            initialTheme = tempSettings.selectedTheme.first()
            val url = tempSettings.supabaseUrl.first()
            val key = tempSettings.supabaseKey.first()
            val isEnabled = tempSettings.isSyncEnabled.first()
            syncEnabled = isEnabled
            isAlreadyLoggedIn = tempSettings.isLoggedIn.first()
            if (isEnabled && url.isNotBlank() && key.isNotBlank()) {
                try {
                    SupabaseManager.init(url, key)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        enableEdgeToEdge()
        
        com.oqba26.abzarforoush.util.NotificationHelper.createNotificationChannel(this)
        scheduleInstallmentReminders(this)

        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val lifecycleOwner = LocalLifecycleOwner.current
            
            var showCleanupDialog by remember { mutableStateOf(value = false) }
            var showForcedExitDialog by remember { mutableStateOf(value = false) }
            var isCleanupChecked by remember { mutableStateOf(value = false) }
            var showSessionExpiredDialog by remember { mutableStateOf(false) }
            var showPermissionsDialog by remember { mutableStateOf(false) }
            
            val requiredPermissions = mutableListOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    add(Manifest.permission.READ_EXTERNAL_STORAGE)
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }.toTypedArray()

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val allGranted = permissions.entries.all { it.value }
                if (allGranted) {
                    showPermissionsDialog = false
                }
            }

            val settingsManager = remember { SettingsManager(context) }
            val selectedFontName by settingsManager.selectedFont.collectAsState(initial = initialFont)
            val selectedThemeName by settingsManager.selectedTheme.collectAsState(initial = initialTheme)

            val updateManager = remember { UpdateManager(context) }
            var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
            var downloadProgress by remember { mutableFloatStateOf(0f) }
            var isDownloading by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000.milliseconds)
                updateInfo = updateManager.checkForUpdate()
            }

            val fontFamily = when (selectedFontName) {
                "Sahel" -> Sahel
                "Estedad" -> Estedad
                "BYekan" -> BYekan
                "IranianSans" -> IranianSans
                else -> Vazirmatn
            }

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        if (isCleanupChecked) {
                            cleanupInstaller()
                        }
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            AbzarForoushTheme(fontFamily = fontFamily, themeName = selectedThemeName) {
                updateInfo?.let { info ->
                    UpdateDialog(
                        updateInfo = info,
                        onDismiss = { updateInfo = null },
                        onConfirm = {
                            val id = updateManager.downloadAndInstall(info.url, "AbzarForoush_v${info.versionName}.apk")
                            if (id != -1L) {
                                isDownloading = true
                                scope.launch {
                                    updateManager.getDownloadProgress(id).collect { progress ->
                                        downloadProgress = progress
                                        if (progress >= 1f) {
                                            isDownloading = false
                                        }
                                    }
                                }
                                updateInfo = null // فقط وقتی دانلود شروع شد دیالوگ را ببند
                            }
                            // اگر id == -1 بود، یعنی کاربر به تنظیمات رفته و دیالوگ باز می‌ماند تا برگردد
                        }
                    )
                }

                if (isDownloading) {
                    Dialog(onDismissRequest = { }) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            Surface(
                                shape = MaterialTheme.shapes.extraLarge,
                                tonalElevation = 6.dp,
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                    Text(
                                        text = "در حال دریافت به‌روزرسانی",
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                    LinearProgressIndicator(
                                        progress = { downloadProgress },
                                        modifier = Modifier.fillMaxWidth().height(8.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${(downloadProgress * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                if (showCleanupDialog) {
                    Dialog(
                        onDismissRequest = { 
                            showCleanupDialog = false
                            showForcedExitDialog = true
                        },
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            Surface(
                                shape = MaterialTheme.shapes.extraLarge,
                                tonalElevation = 6.dp,
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    Text(
                                        text = "دسترسی مدیریت فایل‌ها",
                                        style = MaterialTheme.typography.headlineSmall,
                                        modifier = Modifier.padding(bottom = 16.dp),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = "برای انجام عملیات پشتیبان‌گیری، بازگردانی اطلاعات و مدیریت فایل‌های نصب، برنامه به دسترسی «مدیریت تمامی فایل‌ها» نیاز دارد.\n\nبدون این دسترسی، امکان ذخیره اطلاعات شما وجود نخواهد داشت. لطفاً در صفحه بعد این دسترسی را فعال کنید.",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                showCleanupDialog = false
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                                                    intent.data = "package:${context.packageName}".toUri()
                                                    context.startActivity(intent)
                                                }
                                            },
                                            modifier = Modifier.weight(1f).height(48.dp),
                                            shape = MaterialTheme.shapes.medium,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Text("تنظیمات", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge)
                                        }
                                        Button(
                                            onClick = { 
                                                showCleanupDialog = false
                                                showForcedExitDialog = true 
                                            },
                                            modifier = Modifier.weight(1f).height(48.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            shape = MaterialTheme.shapes.medium
                                        ) {
                                            Text("خروج", style = MaterialTheme.typography.labelLarge)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (showForcedExitDialog) {
                    Dialog(onDismissRequest = { finish() }) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            Surface(
                                shape = MaterialTheme.shapes.extraLarge,
                                tonalElevation = 6.dp,
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    Text(
                                        text = "عدم دسترسی",
                                        style = MaterialTheme.typography.headlineSmall,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                    Text(
                                        text = "متاسفانه شما دسترسی لازم رو به برنامه نداده اید و برنامه بسته خواهد شد.",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(
                                        onClick = { finish() },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = MaterialTheme.shapes.small,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Text("تایید و خروج", color = MaterialTheme.colorScheme.onPrimary)
                                    }
                                }
                            }
                        }
                    }
                }

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val database = remember { AppDatabase.getDatabase(context) }
                    val repository = remember(database) {
                        ProductRepository(
                            database.productDao(), 
                            database.invoiceDao(),
                            database.customerDao(),
                            database.debtTransactionDao(),
                            database.bundleDao(),
                            database.expenseDao(),
                            database.supplierDao(),
                            database.chequeDao(),
                            database.pendingDeletionDao()
                        )
                    }
                    val factory = remember(repository) { ProductViewModelFactory(repository, RealTimeProvider()) }
                    val viewModel: ProductViewModel = viewModel(factory = factory)

                    LaunchedEffect(Unit) {
                        val client = SupabaseManager.getClient()
                        if (isAlreadyLoggedIn && syncEnabled && client != null) {
                            client.auth.sessionStatus.collect { status ->
                                if (status is SessionStatus.NotAuthenticated) {
                                    showSessionExpiredDialog = true
                                }
                            }
                        }
                    }

                    val initialScreen = if (!syncEnabled || isAlreadyLoggedIn) "products" else "login"
                    var currentScreen by remember { mutableStateOf(initialScreen) }
                    var selectedDetailCustomerId by remember { mutableStateOf<Long?>(null) }

                    val mainTabs = listOf("products", "customers", "reports", "accounting", "history", "settings")
                    val pagerState = rememberPagerState(
                        initialPage = mainTabs.indexOf(initialScreen).coerceAtLeast(0),
                        pageCount = { mainTabs.size }
                    )

                    LaunchedEffect(currentScreen) {
                        val index = mainTabs.indexOf(currentScreen)
                        if (index != -1 && pagerState.currentPage != index) {
                            pagerState.animateScrollToPage(index)
                        }
                    }

                    LaunchedEffect(pagerState) {
                        androidx.compose.runtime.snapshotFlow { pagerState.settledPage }.collect { page ->
                            val screen = mainTabs[page]
                            if (currentScreen != screen && currentScreen in mainTabs) {
                                currentScreen = screen
                            }
                        }
                    }

                    var hasCheckedPermissionsThisSession by remember { mutableStateOf(value = false) }

                    LaunchedEffect(currentScreen, showSessionExpiredDialog) {
                        val isMainAppScreen = currentScreen in mainTabs && currentScreen != "login"
                        
                        if (isMainAppScreen && !showSessionExpiredDialog && !hasCheckedPermissionsThisSession) {
                            val allGranted = requiredPermissions.all {
                                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                            }
                            if (!allGranted) {
                                showPermissionsDialog = true
                            }
                            hasCheckedPermissionsThisSession = true
                        }

                        if ((currentScreen == "products") && !isCleanupChecked && !showSessionExpiredDialog) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    if (!Environment.isExternalStorageManager()) {
                                        showCleanupDialog = true
                                    } else {
                                        cleanupInstaller()
                                    }
                                } else {
                                    cleanupInstaller()
                                }
                                isCleanupChecked = true
                            }
                        }
                    
                    var showExitDialog by remember { mutableStateOf(value = false) }
                    var showCartSheet by remember { mutableStateOf(value = false) }

                    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
                    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

                    val filePickerLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.GetContent()
                    ) { uri ->
                        uri?.let { 
                            val contentResolver = context.contentResolver
                            val type = contentResolver.getType(it)
                            val fileName = it.path ?: ""
                            
                            if ((type == "application/json") || fileName.endsWith(".json")) {
                                viewModel.importFullBackup(it, context)
                            } else if ((type?.contains("spreadsheetml") == true) || fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
                                viewModel.importFromExcel(it, context)
                            } else {
                                viewModel.importFromCsv(it, context)
                            }
                        }
                    }

                    if (showPermissionsDialog) {
                        Dialog(onDismissRequest = { }) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                Surface(
                                    shape = MaterialTheme.shapes.extraLarge,
                                    tonalElevation = 6.dp,
                                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                                ) {
                                    @Suppress("SpellCheckingInspection")
                                    Column(modifier = Modifier.padding(24.dp)) {
                                        Text(
                                            text = "دسترسی‌های الزامی",
                                            style = MaterialTheme.typography.headlineSmall,
                                            modifier = Modifier.padding(bottom = 16.dp),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Text(
                                            text = "کاربر گرامی، برای استفاده از برنامه باید دسترسی‌های زیر را تایید کنید:\n\n" +
                                                    "۱. مخاطبین: برای پیدا کردن خودکار شماره مشتری\n" +
                                                    "۲. دوربین: برای اسکن بارکد کالاها\n" +
                                                    "۳. حافظه: برای ذخیره فاکتورها و فایل‌های پشتیبان\n\n" +
                                                    "توجه: در صورت عدم تایید، برنامه به هیچ عنوان کار نخواهد کرد و بسته خواهد شد.",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Button(
                                            onClick = {
                                                permissionLauncher.launch(requiredPermissions)
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = MaterialTheme.shapes.small,
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text("متوجه شدم؛ اعطای دسترسی", color = MaterialTheme.colorScheme.onPrimary)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        @Suppress("SpellCheckingInspection")
                                        TextButton(
                                            onClick = { finish() },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("خروج", color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (showSessionExpiredDialog) {
                        Dialog(onDismissRequest = { }) {
                            @Suppress("SpellCheckingInspection")
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                Surface(
                                    shape = MaterialTheme.shapes.extraLarge,
                                    tonalElevation = 6.dp,
                                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(24.dp)) {
                                        Text(
                                            text = "کاربر گرامی",
                                            style = MaterialTheme.typography.headlineSmall,
                                            modifier = Modifier.padding(bottom = 16.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "زمان ورود قبلی شما به برنامه منقضی شده و باید مجدد وارد شوید. این کار فقط برای حفظ امنیت اطلاعات خودتان ضروری است.\n\nالان به صفحه ورود منتقل می‌شوید؛ لطفاً نام کاربری و کلمه عبوری که قبلاً با آن در برنامه ثبت‌نام کرده و وارد شده‌اید را مجدداً وارد نمایید. با تشکر",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Button(
                                            onClick = {
                                                showSessionExpiredDialog = false
                                                currentScreen = "login"
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = MaterialTheme.shapes.small,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Text("متوجه شدم؛ ورود مجدد", color = MaterialTheme.colorScheme.onPrimary)
                                        }
                                    }
                                }
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
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                        Button(
                                            onClick = { finish() },
                                            modifier = Modifier.weight(1f).height(48.dp),
                                            shape = MaterialTheme.shapes.medium,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.error
                                            )
                                        ) {
                                            Text("خروج", color = MaterialTheme.colorScheme.onError, style = MaterialTheme.typography.labelLarge)
                                        }
                                        Button(
                                            onClick = { showExitDialog = false },
                                            modifier = Modifier.weight(1f).height(48.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            shape = MaterialTheme.shapes.medium
                                        ) {
                                            Text("انصراف", style = MaterialTheme.typography.labelLarge)
                                        }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    BackHandler {
                        if (currentScreen == "customer_detail") {
                            currentScreen = "customers"
                        } else if (currentScreen != "products") {
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
                                    onNavigateToAccounting = { currentScreen = "accounting" }
                                ) { 
                                    if (currentScreen == "products") {
                                        showCartSheet = true
                                    } else {
                                        currentScreen = "products"
                                        showCartSheet = true
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Column(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                            if (isSyncing) {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth().height(2.dp),
                                    color = MaterialTheme.colorScheme.tertiary,
                                    trackColor = Color.Transparent
                                )
                            }
                            Box(modifier = Modifier.fillMaxSize()) {
                                when (currentScreen) {
                                    "login" -> {
                                        LoginScreen { 
                                            scope.launch { 
                                                settingsManager.setLoggedIn(loggedIn = true)
                                                // بلافاصله بعد از لاگین، همگام‌سازی را شروع کن
                                                viewModel.syncWithSupabase()
                                            }
                                            currentScreen = "products" 
                                        }
                                    }
                                    in mainTabs -> {
                                        HorizontalPager(
                                            state = pagerState,
                                            modifier = Modifier.fillMaxSize(),
                                            userScrollEnabled = true,
                                            beyondViewportPageCount = 1
                                        ) { page ->
                                            when (mainTabs[page]) {
                                                "products" -> ProductScreen(
                                                    viewModel = viewModel,
                                                    externalShowCart = showCartSheet,
                                                    onCloseCart = { showCartSheet = false },
                                                    onImportCsv = { filePickerLauncher.launch("text/*") },
                                                    onImportExcel = { filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") },
                                                    onExportExcel = { viewModel.exportToExcel(context) },
                                                    onImportBackup = { filePickerLauncher.launch("*/*") }
                                                ) { viewModel.exportFullBackup(context) }
                                                "customers" -> CustomerScreen(
                                                    viewModel = viewModel,
                                                    onNavigateBack = { currentScreen = "products" },
                                                    onNavigateToDetail = { id ->
                                                        selectedDetailCustomerId = id
                                                        currentScreen = "customer_detail"
                                                    }
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
                                                "history" -> HistoryScreen(
                                                    viewModel = viewModel,
                                                    onNavigateBack = { currentScreen = "products" }
                                                )
                                                "settings" -> SettingsScreen(
                                                    viewModel = viewModel,
                                                    settingsManager = settingsManager,
                                                    onNavigateBack = { currentScreen = "products" },
                                                    onLogout = { currentScreen = "login" }
                                                )
                                            }
                                        }
                                    }
                                    else -> {
                                        when (currentScreen) {
                                            "customer_detail" -> {
                                                selectedDetailCustomerId?.let { id ->
                                                    com.oqba26.abzarforoush.ui.screens.CustomerDetailScreen(
                                                        customerId = id,
                                                        viewModel = viewModel,
                                                        onNavigateBack = { currentScreen = "customers" }
                                                    )
                                                }
                                            }
                                            "suppliers" -> SupplierScreen(
                                                viewModel = viewModel,
                                                onNavigateBack = { currentScreen = "accounting" },
                                                onStartPurchase = {
                                                    currentScreen = "products"
                                                    showCartSheet = true
                                                }
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
        }
    }

    private fun cleanupInstaller() {
        try {
            val publicDownloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            deleteApksInDir(publicDownloadDir)
            val privateDownloadDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            deleteApksInDir(privateDownloadDir)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteApksInDir(directory: File?) {
        if (directory != null && directory.exists() && directory.isDirectory) {
            val files = directory.listFiles()
            files?.forEach { file ->
                if (file.isFile && file.extension.equals("apk", ignoreCase = true)) {
                    val name = file.name.lowercase()
                    if (name.contains("abzar") || name.contains("foroush") || name.contains("app-debug") || name.contains("app-release")) {
                        if (file.delete()) {
                            android.util.Log.d("Cleanup", "Deleted installer: ${file.name}")
                        }
                    }
                }
            }
        }
    }

    private fun scheduleInstallmentReminders(context: Context) {
        val workRequest = androidx.work.PeriodicWorkRequestBuilder<com.oqba26.abzarforoush.util.InstallmentReminderWorker>(
            1, java.util.concurrent.TimeUnit.DAYS
        ).build()

        androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "InstallmentReminders",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
