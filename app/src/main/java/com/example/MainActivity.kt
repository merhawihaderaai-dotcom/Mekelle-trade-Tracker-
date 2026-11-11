package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.example.data.AppDatabase
import com.example.data.TradeRepository
import com.example.ui.TradeViewModel
import com.example.ui.TradeViewModelFactory
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FinanceScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.ShipmentsScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Room database and repository initialized securely using lifecycleScope
        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = TradeRepository(database.tradeDao())
        
        val viewModel: TradeViewModel by viewModels { TradeViewModelFactory(repository) }
        viewModel.loadTheme(applicationContext)

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val systemTheme = androidx.compose.foundation.isSystemInDarkTheme()
            val useDarkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> systemTheme
            }
            MyApplicationTheme(darkTheme = useDarkTheme) {
                MainLayout(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(viewModel: TradeViewModel) {
    var activeTab by remember { mutableStateOf("Dashboard") }
    var showSettingsDialog by remember { mutableStateOf(false) }
    val appLanguage by viewModel.appLanguage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = com.example.utils.TranslationHelper.translate("Mekelle Trade Tracker", appLanguage),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("settings_top_bar_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = com.example.utils.TranslationHelper.translate("System Settings", appLanguage),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Avatar profile placeholder utilizing professional headshot placeholder
                    AsyncImage(
                        model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDEBRJun9D0KuNtcLvjQ3T5TXusqioFs_YBzrQiweb06-ilIsDRri5k_t2zrFis2xV6amTTTm2PKGahrpYUSyxxDntnbaAJYh8zcbHfC12nT3t2UsRPChPlAI6v_RzeKlxSQdILVUFMYO31K527ebFXMW0VknM3_EMkNKqWaUc699G7AwuAP0nmj9Tq9i8w73DGyzigcSf3cGf14_tcZby31EC5D0WeFPid4LRwUJSBEfRDpAL-Gs6Rpgvc6rpeXGFF3thI7ZRsmSI",
                        contentDescription = "User profile picture",
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_nav")
            ) {
                NavigationBarItem(
                    selected = activeTab == "Dashboard",
                    onClick = { activeTab = "Dashboard" },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text(com.example.utils.TranslationHelper.translate("Dashboard", appLanguage), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_Dashboard")
                )
                NavigationBarItem(
                    selected = activeTab == "Inventory",
                    onClick = { activeTab = "Inventory" },
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = "Inventory") },
                    label = { Text(com.example.utils.TranslationHelper.translate("Inventory", appLanguage), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_Inventory")
                )
                NavigationBarItem(
                    selected = activeTab == "Shipments",
                    onClick = { activeTab = "Shipments" },
                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = "Shipments") },
                    label = { Text(com.example.utils.TranslationHelper.translate("Shipments", appLanguage), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_Shipments")
                )
                NavigationBarItem(
                    selected = activeTab == "Finance",
                    onClick = { activeTab = "Finance" },
                    icon = { Icon(Icons.Default.Payments, contentDescription = "Finance") },
                    label = { Text(com.example.utils.TranslationHelper.translate("Finance", appLanguage), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_Finance")
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "Dashboard" -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToTab = { activeTab = it }
                )
                "Inventory" -> InventoryScreen(
                    viewModel = viewModel
                )
                "Shipments" -> ShipmentsScreen(
                    viewModel = viewModel
                )
                "Finance" -> FinanceScreen(
                    viewModel = viewModel
                )
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            viewModel = viewModel,
            onDismiss = { showSettingsDialog = false }
        )
    }
}
