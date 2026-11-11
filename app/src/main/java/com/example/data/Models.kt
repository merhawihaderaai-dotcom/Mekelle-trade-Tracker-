package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

data class ShipmentItem(
    val itemName: String,
    val quantity: Double,
    val unit: String,
    val sellPrice: Double
) {
    val totalValue: Double get() = quantity * sellPrice
}

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // Spices, Cloth, Garments, Grains, Electronics, Cosmetics, Oils, Other
    val unit: String, // kg, g, meter, piece, bag, bundle, liter, box, roll
    val buyPrice: Double, // ETB per unit
    val quantity: Double,
    val dateAdded: String,
    val notes: String = "",
    val imageUri: String? = null
) {
    val totalValue: Double get() = quantity * buyPrice
}

@Entity(tableName = "shipments")
data class Shipment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // format e.g. "27 Jun 2026"
    val status: String, // Pending, In Transit, Delivered
    val items: List<ShipmentItem>,
    val transportCost: Double,
    val commission: Double,
    val buyerName: String,
    val notes: String = ""
) {
    val netProfit: Double get() = commission - transportCost
    val itemCount: Int get() = items.size
}

class Converters {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, ShipmentItem::class.java)
    private val adapter = moshi.adapter<List<ShipmentItem>>(listType)

    @TypeConverter
    fun fromShipmentItemList(value: List<ShipmentItem>?): String {
        return adapter.toJson(value ?: emptyList())
    }

    @TypeConverter
    fun toShipmentItemList(value: String?): List<ShipmentItem> {
        if (value.isNullOrEmpty()) return emptyList()
        return adapter.fromJson(value) ?: emptyList()
    }
}
