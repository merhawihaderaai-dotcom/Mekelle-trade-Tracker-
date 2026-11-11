package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.data.CsvHelper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BackupHelper
import com.example.ui.TradeViewModel
import com.example.ui.theme.PrimaryGreenLight
import com.example.ui.theme.TabularMono

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    viewModel: TradeViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val appLanguage by viewModel.appLanguage.collectAsState()

    val inventoryItems by viewModel.allInventoryItems.collectAsState()
    val shipments by viewModel.allShipments.collectAsState()

    var showResetConfirm by remember { mutableStateOf(false) }
    var showSeedConfirm by remember { mutableStateOf(false) }
    
    var importJsonText by remember { mutableStateOf("") }
    var importError by remember { mutableStateOf<String?>(null) }
    var importSuccess by remember { mutableStateOf(false) }

    // Dynamically calculate the backup JSON
    val backupJson = remember(inventoryItems, shipments) {
        BackupHelper.exportBackup(inventoryItems, shipments)
    }

    val saveInventoryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                val csvText = CsvHelper.exportInventoryToCsv(inventoryItems)
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(csvText.toByteArray())
                }
                Toast.makeText(context, "Inventory exported to CSV!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val saveShipmentsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                val csvText = CsvHelper.exportShipmentsToCsv(shipments)
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(csvText.toByteArray())
                }
                Toast.makeText(context, "Shipments exported to CSV!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val openInventoryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val csvText = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                val items = CsvHelper.importInventoryFromCsv(csvText)
                if (items == null) {
                    Toast.makeText(context, "Invalid CSV format or missing headers", Toast.LENGTH_LONG).show()
                } else {
                    viewModel.importInventoryCsv(items) { success ->
                        if (success) {
                            Toast.makeText(context, "Inventory loaded successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to import inventory", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val openShipmentsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val csvText = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                val shipmentsList = CsvHelper.importShipmentsFromCsv(csvText)
                if (shipmentsList == null) {
                    Toast.makeText(context, "Invalid CSV format or missing headers", Toast.LENGTH_LONG).show()
                } else {
                    viewModel.importShipmentsCsv(shipmentsList) { success ->
                        if (success) {
                            Toast.makeText(context, "Shipments loaded successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to import shipments", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                com.example.utils.TranslationHelper.translate("System Settings", appLanguage),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: Theme Settings
                item {
                    val themeMode by viewModel.themeMode.collectAsState()
                    Text(
                        com.example.utils.TranslationHelper.translate("THEME SETTINGS", appLanguage),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            "Choose your preferred application visual theme.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val options = listOf(
                                Triple("light", "Light", "theme_light_btn"),
                                Triple("dark", "Dark", "theme_dark_btn"),
                                Triple("system", "System", "theme_system_btn")
                            )
                            options.forEach { (mode, label, tag) ->
                                val isSelected = themeMode == mode
                                if (isSelected) {
                                    Button(
                                        onClick = { viewModel.updateTheme(context, mode) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag(tag),
                                        contentPadding = PaddingValues(vertical = 8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    ) {
                                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { viewModel.updateTheme(context, mode) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag(tag),
                                        contentPadding = PaddingValues(vertical = 8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Section: Language Settings
                item {
                    Text(
                        com.example.utils.TranslationHelper.translate("LANGUAGE SETTINGS", appLanguage),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            com.example.utils.TranslationHelper.translate("Select Language", appLanguage),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val options = listOf(
                                Pair("en", "English"),
                                Pair("am", "አማርኛ")
                            )
                            options.forEach { (langCode, label) ->
                                val isSelected = appLanguage == langCode
                                if (isSelected) {
                                    Button(
                                        onClick = { viewModel.updateLanguage(context, langCode) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("lang_${langCode}_btn"),
                                        contentPadding = PaddingValues(vertical = 8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    ) {
                                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { viewModel.updateLanguage(context, langCode) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("lang_${langCode}_btn"),
                                        contentPadding = PaddingValues(vertical = 8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Section: Cloud Sync Settings
                item {
                    val syncPrefs = remember { context.getSharedPreferences("firebase_sync_prefs", android.content.Context.MODE_PRIVATE) }
                    var groupKey by remember { mutableStateOf(syncPrefs.getString("group_sync_key", "") ?: "") }
                    
                    var showCustomConfig by remember { mutableStateOf(false) }
                    var customProjId by remember { mutableStateOf(syncPrefs.getString("project_id", "") ?: "") }
                    var customApiKey by remember { mutableStateOf(syncPrefs.getString("api_key", "") ?: "") }
                    var customAppId by remember { mutableStateOf(syncPrefs.getString("app_id", "") ?: "") }

                    val isSyncing by viewModel.isSyncing.collectAsState()
                    val syncStatus by viewModel.syncStatus.collectAsState()
                    val cloudMetadata by viewModel.cloudMetadata.collectAsState()

                    LaunchedEffect(groupKey) {
                        if (groupKey.isNotEmpty()) {
                            viewModel.fetchCloudMetadata(context, groupKey)
                        }
                    }

                    Text(
                        com.example.utils.TranslationHelper.translate("CLOUD SYNC & BACKUP (FIRESTORE)", appLanguage),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            com.example.utils.TranslationHelper.translate("Securely sync your inventory and shipments to the cloud and access them across multiple devices using a Group Sync Key.", appLanguage),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Input for Group Sync Key
                        OutlinedTextField(
                            value = groupKey,
                            onValueChange = { newValue ->
                                groupKey = newValue
                                syncPrefs.edit().putString("group_sync_key", newValue).apply()
                            },
                            label = { Text(com.example.utils.TranslationHelper.translate("Group Sync Key", appLanguage)) },
                            placeholder = { Text("e.g. MyShopMekelle2026") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("sync_group_key_input"),
                            trailingIcon = {
                                Row {
                                    IconButton(
                                        onClick = {
                                            if (groupKey.isNotEmpty()) {
                                                clipboardManager.setText(AnnotatedString(groupKey))
                                                Toast.makeText(context, "Key copied to clipboard", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy key")
                                    }
                                    IconButton(
                                        onClick = {
                                            val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
                                            val randomKey = "MekelleSync-" + (1..8)
                                                .map { allowedChars.random() }
                                                .joinToString("")
                                            groupKey = randomKey
                                            syncPrefs.edit().putString("group_sync_key", randomKey).apply()
                                        }
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Generate Key")
                                    }
                                }
                            }
                        )

                        // Status of Cloud Backup
                        if (groupKey.isNotEmpty()) {
                            if (cloudMetadata != null) {
                                val meta = cloudMetadata!!
                                val lastSynced = meta["lastSyncedAt"] as? String ?: "N/A"
                                val invCount = meta["inventoryCount"]?.toString() ?: "0"
                                val shipCount = meta["shipmentCount"]?.toString() ?: "0"
                                val device = meta["deviceName"] as? String ?: "Unknown"

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(6.dp))
                                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        "Cloud Backup Detected:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text("Last Synced: $lastSynced", style = TabularMono.copy(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface))
                                    Text("Inventory Items: $invCount | Shipments: $shipCount", style = MaterialTheme.typography.bodySmall)
                                    Text("Device: $device", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                }
                            } else {
                                Text(
                                    "No cloud backup found for this key yet. Ready to create one.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }

                        // Sync Status message
                        if (syncStatus != null) {
                            Text(
                                text = syncStatus!!,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (syncStatus!!.contains("successful") || syncStatus!!.contains("Restore")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }

                        // Sync Control Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.uploadToCloud(context, groupKey) { success ->
                                        if (success) {
                                            Toast.makeText(context, "Data successfully backed up to cloud!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("cloud_upload_btn"),
                                enabled = !isSyncing && groupKey.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Upload", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.downloadFromCloud(context, groupKey) { success ->
                                        if (success) {
                                            Toast.makeText(context, "Data successfully restored from cloud!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("cloud_download_btn"),
                                enabled = !isSyncing && groupKey.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onSecondary, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Download", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Collapsible Custom Firestore Configuration Toggle
                        TextButton(
                            onClick = { showCustomConfig = !showCustomConfig },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                if (showCustomConfig) "Hide Custom Firestore Setup" else "Use Custom Firestore Setup",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (showCustomConfig) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(6.dp))
                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    "Firestore Custom Credentials",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Provide custom credentials to connect to your own Firebase project securely. If empty, the default Firebase configuration is used.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = customProjId,
                                    onValueChange = { customProjId = it },
                                    label = { Text("Firebase Project ID") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("custom_proj_id_input")
                                )

                                OutlinedTextField(
                                    value = customApiKey,
                                    onValueChange = { customApiKey = it },
                                    label = { Text("Web API Key") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("custom_api_key_input")
                                )

                                OutlinedTextField(
                                    value = customAppId,
                                    onValueChange = { customAppId = it },
                                    label = { Text("Application ID (App ID)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("custom_app_id_input")
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            customProjId = ""
                                            customApiKey = ""
                                            customAppId = ""
                                            com.example.utils.FirestoreSyncHelper.saveConfig(context, "", "", "")
                                            Toast.makeText(context, "Credentials cleared", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Text("Clear", color = MaterialTheme.colorScheme.error)
                                    }

                                    Button(
                                        onClick = {
                                            if (customProjId.isNotBlank() && customApiKey.isNotBlank() && customAppId.isNotBlank()) {
                                                com.example.utils.FirestoreSyncHelper.saveConfig(
                                                    context,
                                                    customProjId,
                                                    customApiKey,
                                                    customAppId
                                                )
                                                Toast.makeText(context, "Firestore config saved!", Toast.LENGTH_SHORT).show()
                                                if (groupKey.isNotEmpty()) {
                                                    viewModel.fetchCloudMetadata(context, groupKey)
                                                }
                                            } else {
                                                Toast.makeText(context, "Please fill in all fields or clear them.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    ) {
                                        Text("Save Config")
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 1: Data Reset
                item {
                    Text(
                        "DATABASE MAINTENANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            "Start fresh from zero or restore default sample data.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showResetConfirm = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("settings_reset_zero_btn"),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset to Zero", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { showSeedConfirm = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("settings_reset_seed_btn"),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Load Seed Data", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Section 2: Backup Export
                item {
                    Text(
                        "EXPORT BACKUP DATA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Copy this encoded backup state to save your progress offline.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(6.dp))
                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = backupJson,
                                style = TabularMono.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(backupJson))
                                Toast.makeText(context, "Backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_copy_backup_btn")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Backup String", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Section 3: Backup Import
                item {
                    Text(
                        "IMPORT BACKUP DATA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Paste an offline backup JSON string below to restore system state.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = importJsonText,
                            onValueChange = {
                                importJsonText = it
                                importError = null
                                importSuccess = false
                            },
                            placeholder = { Text("Paste JSON backup here...", fontSize = 11.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .testTag("settings_import_textfield"),
                            textStyle = TabularMono.copy(fontSize = 11.sp)
                        )

                        if (importError != null) {
                            Text(
                                text = importError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (importSuccess) {
                            Text(
                                text = "✓ Database imported successfully!",
                                color = PrimaryGreenLight,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                if (importJsonText.trim().isEmpty()) {
                                    importError = "Please paste a backup string first"
                                    return@Button
                                }
                                val backup = BackupHelper.importBackup(importJsonText)
                                if (backup == null) {
                                    importError = "Invalid backup data string"
                                    importSuccess = false
                                } else {
                                    viewModel.importBackup(backup) { success ->
                                        if (success) {
                                            importSuccess = true
                                            importError = null
                                            importJsonText = ""
                                            Toast.makeText(context, "Backup Restored!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            importError = "Failed to load database entries"
                                            importSuccess = false
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_import_btn")
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restore Database", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Section 4: CSV Backup & Restore (Inventory & Shipments)
                item {
                    Text(
                        "CSV BACKUP & RESTORE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Export or import data as spreadsheet-compatible CSV files.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Inventory CSV block
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "Inventory Data",
                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { saveInventoryLauncher.launch("inventory_backup.csv") },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("export_inventory_csv_btn"),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Export CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { openInventoryLauncher.launch(arrayOf("text/comma-separated-values", "text/csv", "application/octet-stream", "*/*")) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("import_inventory_csv_btn"),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Import CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)

                        // Shipments CSV block
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "Shipment Data",
                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { saveShipmentsLauncher.launch("shipments_backup.csv") },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("export_shipments_csv_btn"),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Export CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { openShipmentsLauncher.launch(arrayOf("text/comma-separated-values", "text/csv", "application/octet-stream", "*/*")) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("import_shipments_csv_btn"),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Import CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.testTag("settings_close_btn")
            ) {
                Text("Close Settings")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )

    // Inner confirmation dialog for Reset to Zero
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Clear All Data?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
            text = { Text("Warning: This action will completely empty the inventory database and delete all shipments, starting from zero. This cannot be undone.", color = MaterialTheme.colorScheme.onSurface) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showResetConfirm = false
                        Toast.makeText(context, "System started from zero!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Inner confirmation dialog for Reset to Seed Data
    if (showSeedConfirm) {
        AlertDialog(
            onDismissRequest = { showSeedConfirm = false },
            title = { Text("Restore Default Data?") },
            text = { Text("This will overwrite all current changes and restore the system to its initial default database (Berbere, Teff, etc.). Proceed?", color = MaterialTheme.colorScheme.onSurface) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetToSeedData()
                        showSeedConfirm = false
                        Toast.makeText(context, "Database seeded successfully!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Confirm Load")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSeedConfirm = false }) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
