package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TradeRepository(private val tradeDao: TradeDao) {
    val allInventoryItems: Flow<List<InventoryItem>> = tradeDao.getAllInventoryItems()
    val allShipments: Flow<List<Shipment>> = tradeDao.getAllShipments()

    suspend fun insertInventoryItem(item: InventoryItem) {
        tradeDao.insertInventoryItem(item)
    }

    suspend fun updateInventoryItem(item: InventoryItem) {
        tradeDao.updateInventoryItem(item)
    }

    suspend fun updateInventoryItems(items: List<InventoryItem>) {
        tradeDao.updateInventoryItems(items)
    }

    suspend fun deleteInventoryItem(item: InventoryItem) {
        tradeDao.deleteInventoryItem(item)
    }

    suspend fun deleteInventoryItemById(id: Int) {
        tradeDao.deleteInventoryItemById(id)
    }

    suspend fun insertShipment(shipment: Shipment): Long {
        // Insert the shipment
        val id = tradeDao.insertShipment(shipment)
        
        // Deduct quantities from inventory items based on name matching
        val currentInventory = tradeDao.getAllInventoryItems().first()
        for (shipmentItem in shipment.items) {
            val matchingItem = currentInventory.find { 
                it.name.trim().lowercase() == shipmentItem.itemName.trim().lowercase() 
            }
            if (matchingItem != null) {
                val newQuantity = maxOf(0.0, matchingItem.quantity - shipmentItem.quantity)
                tradeDao.updateInventoryItem(matchingItem.copy(quantity = newQuantity))
            }
        }
        
        return id
    }

    suspend fun updateShipment(shipment: Shipment) {
        // If status changes to Delivered or something, we don't double-deduct because we deduct on creation.
        tradeDao.updateShipment(shipment)
    }

    suspend fun deleteShipment(shipment: Shipment) {
        tradeDao.deleteShipment(shipment)
    }

    suspend fun clearAllData() {
        tradeDao.clearInventory()
        tradeDao.clearShipments()
    }

    suspend fun resetToSeedData() {
        tradeDao.clearInventory()
        tradeDao.clearShipments()

        val seedItems = listOf(
            InventoryItem(name = "Berbere", category = "Spices", unit = "kg", buyPrice = 180.0, quantity = 50.0, dateAdded = "2026-06-27", notes = "Aromatic blend"),
            InventoryItem(name = "Mitmita", category = "Spices", unit = "kg", buyPrice = 220.0, quantity = 30.0, dateAdded = "2026-06-27", notes = "Spicy blend"),
            InventoryItem(name = "Korarima", category = "Spices", unit = "kg", buyPrice = 850.0, quantity = 10.0, dateAdded = "2026-06-27", notes = "Premium quality"),
            InventoryItem(name = "Turmeric", category = "Spices", unit = "kg", buyPrice = 120.0, quantity = 40.0, dateAdded = "2026-06-27", notes = "Golden powder"),
            InventoryItem(name = "Cotton Fabric (White)", category = "Cloth", unit = "meter", buyPrice = 45.0, quantity = 80.0, dateAdded = "2026-06-27", notes = "Pure white fabric"),
            InventoryItem(name = "Cotton Fabric (Color)", category = "Cloth", unit = "meter", buyPrice = 55.0, quantity = 60.0, dateAdded = "2026-06-27", notes = "Colored patterns"),
            InventoryItem(name = "Traditional Kemis", category = "Garments", unit = "piece", buyPrice = 350.0, quantity = 20.0, dateAdded = "2026-06-27", notes = "Woven borders"),
            InventoryItem(name = "Teff", category = "Grains", unit = "kg", buyPrice = 95.0, quantity = 100.0, dateAdded = "2026-06-27", notes = "Red and white mix")
        )
        for (item in seedItems) {
            tradeDao.insertInventoryItem(item)
        }

        val shipmentItems = listOf(
            ShipmentItem(itemName = "Berbere", quantity = 20.0, unit = "kg", sellPrice = 220.0),
            ShipmentItem(itemName = "Mitmita", quantity = 10.0, unit = "kg", sellPrice = 280.0),
            ShipmentItem(itemName = "Cotton Fabric (White)", quantity = 15.0, unit = "meter", sellPrice = 70.0)
        )
        val shipment1 = Shipment(
            date = "27 Jun 2026",
            status = "Delivered",
            items = shipmentItems,
            transportCost = 500.0,
            commission = 1200.0,
            buyerName = "Mekelle Buyer 1",
            notes = "First bulk shipment complete"
        )
        tradeDao.insertShipment(shipment1)
    }

    suspend fun importBackup(backup: BackupData) {
        tradeDao.clearInventory()
        tradeDao.clearShipments()
        for (item in backup.inventory) {
            // Re-insert with id=0 to avoid pre-existing key conflicts, or insert directly
            tradeDao.insertInventoryItem(item.copy(id = 0))
        }
        for (shipment in backup.shipments) {
            tradeDao.insertShipment(shipment.copy(id = 0))
        }
    }

    suspend fun importInventoryCsv(items: List<InventoryItem>) {
        tradeDao.clearInventory()
        for (item in items) {
            tradeDao.insertInventoryItem(item.copy(id = 0))
        }
    }

    suspend fun importShipmentsCsv(shipments: List<Shipment>) {
        tradeDao.clearShipments()
        for (shipment in shipments) {
            tradeDao.insertShipment(shipment.copy(id = 0))
        }
    }
}
