package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.InventoryItem
import com.example.data.Shipment
import com.example.data.BackupData
import com.example.data.TradeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TradeViewModel(private val repository: TradeRepository) : ViewModel() {

    val allInventoryItems: StateFlow<List<InventoryItem>> = repository.allInventoryItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allShipments: StateFlow<List<Shipment>> = repository.allShipments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filter and search states
    val searchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow("All")

    // Theme state
    val themeMode = MutableStateFlow("dark")

    // Language state
    val appLanguage = MutableStateFlow("en")

    fun loadTheme(context: android.content.Context) {
        themeMode.value = com.example.utils.ThemePreferences.getThemeMode(context)
        appLanguage.value = com.example.utils.TranslationHelper.getLanguage(context)
    }

    fun updateTheme(context: android.content.Context, mode: String) {
        com.example.utils.ThemePreferences.setThemeMode(context, mode)
        themeMode.value = mode
    }

    fun updateLanguage(context: android.content.Context, lang: String) {
        com.example.utils.TranslationHelper.setLanguage(context, lang)
        appLanguage.value = lang
    }

    // Filtered inventory list
    val filteredInventoryItems: StateFlow<List<InventoryItem>> = combine(
        allInventoryItems,
        searchQuery,
        selectedCategoryFilter
    ) { items, query, category ->
        items.filter { item ->
            val matchesQuery = item.name.contains(query, ignoreCase = true) || 
                               item.category.contains(query, ignoreCase = true) ||
                               item.notes.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || item.category.lowercase() == category.lowercase()
            matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Finance & Summary computations (computed reactively)
    val totalStockValue: StateFlow<Double> = allInventoryItems
        .combine(MutableStateFlow(0)) { items, _ ->
            items.sumOf { it.quantity * it.buyPrice }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val totalCommissionEarned: StateFlow<Double> = allShipments
        .combine(MutableStateFlow(0)) { shipments, _ ->
            shipments.sumOf { it.commission }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val totalTransportCost: StateFlow<Double> = allShipments
        .combine(MutableStateFlow(0)) { shipments, _ ->
            shipments.sumOf { it.transportCost }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val netProfit: StateFlow<Double> = combine(
        totalCommissionEarned,
        totalTransportCost
    ) { commission, transport ->
        commission - transport
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val roiPercentage: StateFlow<Double> = combine(
        netProfit,
        totalStockValue
    ) { profit, stockValue ->
        if (stockValue == 0.0) 0.0 else (profit / stockValue) * 100.0
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    // Database Actions
    fun addInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            repository.insertInventoryItem(item)
        }
    }

    fun updateInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            repository.updateInventoryItem(item)
        }
    }

    fun updateInventoryItems(items: List<InventoryItem>, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateInventoryItems(items)
            onComplete()
        }
    }

    fun deleteInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            repository.deleteInventoryItem(item)
        }
    }

    fun addShipment(shipment: Shipment, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insertShipment(shipment)
            onComplete()
        }
    }

    fun updateShipment(shipment: Shipment) {
        viewModelScope.launch {
            repository.updateShipment(shipment)
        }
    }

    fun deleteShipment(shipment: Shipment) {
        viewModelScope.launch {
            repository.deleteShipment(shipment)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    fun resetToSeedData() {
        viewModelScope.launch {
            repository.resetToSeedData()
        }
    }

    fun importBackup(backup: BackupData, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repository.importBackup(backup)
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    fun importInventoryCsv(items: List<InventoryItem>, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repository.importInventoryCsv(items)
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    fun importShipmentsCsv(shipments: List<Shipment>, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repository.importShipmentsCsv(shipments)
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    // --- Cloud Sync States & Actions ---
    val isSyncing = MutableStateFlow(false)
    val syncStatus = MutableStateFlow<String?>(null)
    val cloudMetadata = MutableStateFlow<Map<String, Any>?>(null)

    fun fetchCloudMetadata(context: android.content.Context, groupKey: String) {
        if (groupKey.isBlank()) return
        viewModelScope.launch {
            val meta = com.example.utils.FirestoreSyncHelper.getSyncMetadata(context, groupKey)
            cloudMetadata.value = meta
        }
    }

    fun uploadToCloud(context: android.content.Context, groupKey: String, onComplete: (Boolean) -> Unit) {
        if (groupKey.isBlank()) {
            syncStatus.value = "Group Sync Key cannot be empty"
            onComplete(false)
            return
        }
        viewModelScope.launch {
            isSyncing.value = true
            syncStatus.value = "Uploading local data to cloud..."
            val inventory = allInventoryItems.value
            val shipments = allShipments.value
            val success = com.example.utils.FirestoreSyncHelper.uploadSync(context, groupKey, inventory, shipments)
            if (success) {
                syncStatus.value = "Sync successful!"
                fetchCloudMetadata(context, groupKey)
            } else {
                syncStatus.value = "Sync failed. Check connection or Firestore config."
            }
            isSyncing.value = false
            onComplete(success)
        }
    }

    fun downloadFromCloud(context: android.content.Context, groupKey: String, onComplete: (Boolean) -> Unit) {
        if (groupKey.isBlank()) {
            syncStatus.value = "Group Sync Key cannot be empty"
            onComplete(false)
            return
        }
        viewModelScope.launch {
            isSyncing.value = true
            syncStatus.value = "Downloading data from cloud..."
            val backupData = com.example.utils.FirestoreSyncHelper.downloadSync(context, groupKey)
            if (backupData != null) {
                try {
                    repository.importBackup(backupData)
                    syncStatus.value = "Restore successful!"
                    fetchCloudMetadata(context, groupKey)
                    onComplete(true)
                } catch (e: Exception) {
                    syncStatus.value = "Failed to import downloaded cloud data."
                    onComplete(false)
                }
            } else {
                syncStatus.value = "No backup found in cloud for this key, or sync failed."
                onComplete(false)
            }
            isSyncing.value = false
        }
    }
}

class TradeViewModelFactory(private val repository: TradeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TradeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TradeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
