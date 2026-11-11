package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [InventoryItem::class, Shipment::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tradeDao(): TradeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mekelle_trade_tracker_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.tradeDao())
                }
            }
        }

        suspend fun populateDatabase(dao: TradeDao) {
            // Seed Inventory items
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
                dao.insertInventoryItem(item)
            }

            // Seed Shipment #1
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
            dao.insertShipment(shipment1)
        }
    }
}
