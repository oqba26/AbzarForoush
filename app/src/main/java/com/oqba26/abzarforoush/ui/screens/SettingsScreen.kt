package com.oqba26.abzarforoush.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oqba26.abzarforoush.data.ProductViewModel
import com.oqba26.abzarforoush.data.SettingsManager
import com.oqba26.abzarforoush.util.SupabaseManager
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ProductViewModel,
    settingsManager: SettingsManager, 
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val selectedFont by settingsManager.selectedFont.collectAsState(initial = "Vazirmatn")
    val selectedTheme by settingsManager.selectedTheme.collectAsState(initial = "Purple")
    val isSyncEnabled by settingsManager.isSyncEnabled.collectAsState(initial = true)
    val shopNamePersistent by settingsManager.shopName.collectAsState(initial = "")
    val shopPhonePersistent by settingsManager.shopPhone.collectAsState(initial = "")
    val shopAddressPersistent by settingsManager.shopAddress.collectAsState(initial = "")
    val shopTaxIdPersistent by settingsManager.shopTaxId.collectAsState(initial = "")

    var localShopName by remember(shopNamePersistent) { mutableStateOf(shopNamePersistent) }
    var localShopPhone by remember(shopPhonePersistent) { mutableStateOf(shopPhonePersistent) }
    var localShopAddress by remember(shopAddressPersistent) { mutableStateOf(shopAddressPersistent) }
    var localShopTaxId by remember(shopTaxIdPersistent) { mutableStateOf(shopTaxIdPersistent) }

    val scope = rememberCoroutineScope()
    var fontExpanded by remember { mutableStateOf(false) }
    var themeExpanded by remember { mutableStateOf(false) }
    
    val fonts = listOf("Vazirmatn", "Sahel", "Estedad", "BYekan", "IranianSans")
    val themes = listOf("Purple", "Blue", "Green")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "تنظیمات فروشگاه",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = localShopName,
                onValueChange = { localShopName = it },
                label = { Text("نام فروشگاه") },
                placeholder = { Text("مثلاً: ابزارآلات خالقی") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = localShopPhone,
                onValueChange = { localShopPhone = it },
                label = { Text("شماره تماس فروشگاه") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = localShopAddress,
                onValueChange = { localShopAddress = it },
                label = { Text("آدرس فروشگاه") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                maxLines = 2
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = localShopTaxId,
                onValueChange = { localShopTaxId = it },
                label = { Text("شناسه اقتصادی / کد ملی") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        settingsManager.saveShopInfo(
                            localShopName,
                            localShopPhone,
                            localShopAddress,
                            localShopTaxId
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("ذخیره مشخصات فروشگاه")
            }

            Spacer(Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(Modifier.height(32.dp))

            Text(
                text = "تنظیمات ظاهری",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "انتخاب فونت برنامه:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ExposedDropdownMenuBox(
                expanded = fontExpanded,
                onExpandedChange = { fontExpanded = !fontExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedFont,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fontExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
                ExposedDropdownMenu(
                    expanded = fontExpanded,
                    onDismissRequest = { fontExpanded = false }
                ) {
                    fonts.forEach { font ->
                        DropdownMenuItem(
                            text = { Text(text = font) },
                            onClick = {
                                scope.launch {
                                    settingsManager.saveFont(font)
                                    fontExpanded = false
                                }
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "انتخاب تم رنگی:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ExposedDropdownMenuBox(
                expanded = themeExpanded,
                onExpandedChange = { themeExpanded = !themeExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = when(selectedTheme) {
                        "Blue" -> "آبی کلاسیک"
                        "Green" -> "سبز جنگلی"
                        else -> "بنفش پیش‌فرض"
                    },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
                ExposedDropdownMenu(
                    expanded = themeExpanded,
                    onDismissRequest = { themeExpanded = false }
                ) {
                    themes.forEach { theme ->
                        DropdownMenuItem(
                            text = { 
                                Text(text = when(theme) {
                                    "Blue" -> "آبی کلاسیک"
                                    "Green" -> "سبز جنگلی"
                                    else -> "بنفش پیش‌فرض"
                                }) 
                            },
                            onClick = {
                                scope.launch {
                                    settingsManager.saveTheme(theme)
                                    themeExpanded = false
                                }
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(Modifier.height(32.dp))

            Text(
                text = "تنظیمات حساب و همگام‌سازی",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "همگام‌سازی خودکار", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "اطلاعات به طور خودکار در فضای ابری ذخیره می‌شوند.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Switch(
                    checked = isSyncEnabled,
                    onCheckedChange = { 
                        scope.launch {
                            settingsManager.setSyncEnabled(it)
                        }
                    }
                )
            }

            if (isSyncEnabled) {
                Spacer(Modifier.height(16.dp))
                
                var isSyncing by remember { mutableStateOf(false) }
                
                OutlinedButton(
                    onClick = {
                        isSyncing = true
                        viewModel.syncWithSupabase {
                            isSyncing = false
                            Toast.makeText(context, "اطلاعات با موفقیت به‌روزرسانی شد", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    enabled = !isSyncing
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("در حال دریافت...")
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("همین حالا همگام‌سازی کن")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        settingsManager.setLoggedIn(false)
                        SupabaseManager.getClient()?.auth?.signOut()
                        onLogout()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("خروج از حساب کاربری")
            }
            
            Spacer(Modifier.height(8.dp))
            Text(
                text = "برای تغییر فروشگاه، ابتدا خارج شوید.",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
