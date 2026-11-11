package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeDao {
    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    fun getAllInventoryItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun getInventoryItemById(id: Int): InventoryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItem)

    @Update
    suspend fun updateInventoryItem(item: InventoryItem)

    @Update
    suspend fun updateInventoryItems(items: List<InventoryItem>)

    @Delete
    suspend fun deleteInventoryItem(item: InventoryItem)

    @Query("DELETE FROM inventory_items WHERE id = :id")
    suspend fun deleteInventoryItemById(id: Int)


    @Query("DELETE FROM inventory_items")
    suspend fun clearInventory()

    @Query("DELETE FROM shipments")
    suspend fun clearShipments()


    @Query("SELECT * FROM shipments ORDER BY id DESC")
    fun getAllShipments(): Flow<List<Shipment>>

    @Query("SELECT * FROM shipments WHERE id = :id")
    suspend fun getShipmentById(id: Int): Shipment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShipment(shipment: Shipment): Long

    @Update
    suspend fun updateShipment(shipment: Shipment)

    @Delete
    suspend fun deleteShipment(shipment: Shipment)
}
