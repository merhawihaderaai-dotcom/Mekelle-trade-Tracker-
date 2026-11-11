package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

data class BackupData(
    val inventory: List<InventoryItem>,
    val shipments: List<Shipment>
)

object BackupHelper {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
        
    private val adapter = moshi.adapter(BackupData::class.java)

    fun exportBackup(inventory: List<InventoryItem>, shipments: List<Shipment>): String {
        val backup = BackupData(inventory, shipments)
        return adapter.toJson(backup)
    }

    fun importBackup(json: String): BackupData? {
        return try {
            adapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }
}
