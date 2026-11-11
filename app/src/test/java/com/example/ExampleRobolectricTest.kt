package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Mekelle Trade Tracker", appName)
  }

  @Test
  fun `export and import backup`() {
    val items = listOf(
      com.example.data.InventoryItem(
        id = 1,
        name = "Test Spice",
        category = "Spices",
        unit = "kg",
        buyPrice = 100.0,
        quantity = 10.0,
        dateAdded = "2026-06-29"
      )
    )
    val shipments = listOf(
      com.example.data.Shipment(
        id = 1,
        date = "29 Jun 2026",
        status = "Pending",
        items = listOf(com.example.data.ShipmentItem("Test Spice", 5.0, "kg", 150.0)),
        transportCost = 100.0,
        commission = 200.0,
        buyerName = "Buyer"
      )
    )
    
    val json = com.example.data.BackupHelper.exportBackup(items, shipments)
    val restored = com.example.data.BackupHelper.importBackup(json)
    
    org.junit.Assert.assertNotNull(restored)
    assertEquals(1, restored!!.inventory.size)
    assertEquals("Test Spice", restored.inventory[0].name)
    assertEquals(1, restored.shipments.size)
    assertEquals("Buyer", restored.shipments[0].buyerName)
  }

  @Test
  fun `export and import inventory CSV`() {
    val items = listOf(
      com.example.data.InventoryItem(
        id = 1,
        name = "Special Berbere, Extra Spicy",
        category = "Spices",
        unit = "kg",
        buyPrice = 180.0,
        quantity = 50.0,
        dateAdded = "2026-06-27",
        notes = "With \"Korerima\" seeds"
      ),
      com.example.data.InventoryItem(
        id = 2,
        name = "Teff",
        category = "Grains",
        unit = "kg",
        buyPrice = 90.0,
        quantity = 100.0,
        dateAdded = "2026-06-27",
        notes = "Standard"
      )
    )

    val csv = com.example.data.CsvHelper.exportInventoryToCsv(items)
    val restored = com.example.data.CsvHelper.importInventoryFromCsv(csv)

    org.junit.Assert.assertNotNull(restored)
    assertEquals(2, restored!!.size)
    assertEquals("Special Berbere, Extra Spicy", restored[0].name)
    assertEquals("Spices", restored[0].category)
    assertEquals("With \"Korerima\" seeds", restored[0].notes)
    assertEquals("Teff", restored[1].name)
  }

  @Test
  fun `export and import shipments CSV`() {
    val shipments = listOf(
      com.example.data.Shipment(
        id = 1,
        date = "27 Jun 2026",
        status = "Delivered",
        items = listOf(
          com.example.data.ShipmentItem("Berbere", 20.0, "kg", 220.0),
          com.example.data.ShipmentItem("Mitmita", 10.0, "kg", 280.0)
        ),
        transportCost = 500.0,
        commission = 1200.0,
        buyerName = "Mekelle Buyer 1",
        notes = "Bulk discount applied"
      )
    )

    val csv = com.example.data.CsvHelper.exportShipmentsToCsv(shipments)
    val restored = com.example.data.CsvHelper.importShipmentsFromCsv(csv)

    org.junit.Assert.assertNotNull(restored)
    assertEquals(1, restored!!.size)
    assertEquals("Mekelle Buyer 1", restored[0].buyerName)
    assertEquals(2, restored[0].items.size)
    assertEquals("Berbere", restored[0].items[0].itemName)
    assertEquals(20.0, restored[0].items[0].quantity, 0.0)
    assertEquals("Mitmita", restored[0].items[1].itemName)
    assertEquals(280.0, restored[0].items[1].sellPrice, 0.0)
  }

  @Test
  fun `test getCategoryColor handles case insensitive inputs`() {
    val spicesColor = com.example.ui.screens.getCategoryColor("SpIcEs")
    val defaultColor = com.example.ui.screens.getCategoryColor("nonexistent-category")
    
    assertEquals(com.example.ui.theme.CategorySpices, spicesColor)
    assertEquals(com.example.ui.theme.CategoryOther, defaultColor)
  }

  @Test
  fun `test batch update price and category mapping`() {
    val items = listOf(
      com.example.data.InventoryItem(
        id = 1,
        name = "Berbere",
        category = "Spices",
        unit = "kg",
        buyPrice = 180.0,
        quantity = 50.0,
        dateAdded = "2026-06-27"
      ),
      com.example.data.InventoryItem(
        id = 2,
        name = "Mitmita",
        category = "Spices",
        unit = "kg",
        buyPrice = 220.0,
        quantity = 30.0,
        dateAdded = "2026-06-27"
      )
    )

    val updatedPrice = 250.0
    val itemsWithUpdatedPrice = items.map { it.copy(buyPrice = updatedPrice) }
    assertEquals(2, itemsWithUpdatedPrice.size)
    assertEquals(250.0, itemsWithUpdatedPrice[0].buyPrice, 0.0)
    assertEquals(250.0, itemsWithUpdatedPrice[1].buyPrice, 0.0)

    val updatedCategory = "Oils"
    val itemsWithUpdatedCategory = items.map { it.copy(category = updatedCategory) }
    assertEquals(2, itemsWithUpdatedCategory.size)
    assertEquals("Oils", itemsWithUpdatedCategory[0].category)
    assertEquals("Oils", itemsWithUpdatedCategory[1].category)
  }

  @Test
  fun `test receipt pdf generator writes file to cache`() {
    val context = org.robolectric.RuntimeEnvironment.getApplication()
    val shipment = com.example.data.Shipment(
      id = 42,
      date = "2026-06-29",
      status = "Delivered",
      buyerName = "Abebe Kebede",
      items = listOf(
        com.example.data.ShipmentItem(
          itemName = "Berbere",
          quantity = 10.0,
          unit = "kg",
          sellPrice = 150.0
        )
      ),
      commission = 150.0,
      transportCost = 50.0,
      notes = "Deliver before sunset"
    )

    val generatedFile = com.example.utils.ReceiptPdfGenerator.generateReceiptPdf(context, shipment)
    
    org.junit.Assert.assertNotNull(generatedFile)
    org.junit.Assert.assertTrue(generatedFile!!.exists())
    org.junit.Assert.assertTrue(generatedFile.length() > 0)
    assertEquals("AST_Shipment_Receipt_42.pdf", generatedFile.name)
  }

  @Test
  fun `test theme preferences can save and load mode`() {
    val context = org.robolectric.RuntimeEnvironment.getApplication()
    
    // Default theme should be "dark"
    assertEquals("dark", com.example.utils.ThemePreferences.getThemeMode(context))
    
    // Save "light"
    com.example.utils.ThemePreferences.setThemeMode(context, "light")
    assertEquals("light", com.example.utils.ThemePreferences.getThemeMode(context))
    
    // Save "system"
    com.example.utils.ThemePreferences.setThemeMode(context, "system")
    assertEquals("system", com.example.utils.ThemePreferences.getThemeMode(context))
  }

  @Test
  fun `test trade viewmodel theme integration`() {
    val context = org.robolectric.RuntimeEnvironment.getApplication()
    val db = com.example.data.AppDatabase.getDatabase(context, kotlinx.coroutines.GlobalScope)
    val repo = com.example.data.TradeRepository(db.tradeDao())
    val viewModel = com.example.ui.TradeViewModel(repo)
    
    viewModel.loadTheme(context)
    assertEquals("dark", viewModel.themeMode.value)
    
    viewModel.updateTheme(context, "light")
    assertEquals("light", viewModel.themeMode.value)
    assertEquals("light", com.example.utils.ThemePreferences.getThemeMode(context))
  }

  @Test
  fun `test inventory item stores and updates image uri`() {
    val context = org.robolectric.RuntimeEnvironment.getApplication()
    val db = com.example.data.AppDatabase.getDatabase(context, kotlinx.coroutines.GlobalScope)
    val repo = com.example.data.TradeRepository(db.tradeDao())
    val viewModel = com.example.ui.TradeViewModel(repo)

    val item = com.example.data.InventoryItem(
      id = 123,
      name = "Test Product",
      category = "Spices",
      unit = "kg",
      buyPrice = 100.0,
      quantity = 10.0,
      dateAdded = "2026-06-29",
      imageUri = "content://com.example.fileprovider/product_images/test.jpg"
    )

    assertEquals("content://com.example.fileprovider/product_images/test.jpg", item.imageUri)
    
    val updatedItem = item.copy(imageUri = "content://com.example.fileprovider/product_images/test_updated.jpg")
    assertEquals("content://com.example.fileprovider/product_images/test_updated.jpg", updatedItem.imageUri)
  }

  @Test
  fun `test custom firestore sync helper configuration`() {
    val context = org.robolectric.RuntimeEnvironment.getApplication()
    
    // Check initial state
    assertEquals(false, com.example.utils.FirestoreSyncHelper.hasCustomConfig(context))
    
    // Save config
    com.example.utils.FirestoreSyncHelper.saveConfig(context, "my-project-123", "api-key-xyz", "app-id-abc")
    
    // Verify stored configuration
    assertEquals(true, com.example.utils.FirestoreSyncHelper.hasCustomConfig(context))
    val (projId, apiKey, appId) = com.example.utils.FirestoreSyncHelper.getConfig(context)
    assertEquals("my-project-123", projId)
    assertEquals("api-key-xyz", apiKey)
    assertEquals("app-id-abc", appId)
  }
}
