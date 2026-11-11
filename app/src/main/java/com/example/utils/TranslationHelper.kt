package com.example.utils

import android.content.Context

object TranslationHelper {
    private const val PREFS_NAME = "language_prefs"
    private const val KEY_LANG = "selected_language"

    // Supported languages: "en" (English), "am" (Amharic)
    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANG, "en") ?: "en"
    }

    fun setLanguage(context: Context, lang: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, lang).apply()
    }

    private val translations = mapOf(
        "Mekelle Trade Tracker" to mapOf("en" to "Mekelle Trade Tracker", "am" to "መቀሌ የንግድ መከታተያ"),
        "Dashboard" to mapOf("en" to "Dashboard", "am" to "ዳሽቦርድ"),
        "Inventory" to mapOf("en" to "Inventory", "am" to "ዕቃዎች ዝርዝር"),
        "Shipments" to mapOf("en" to "Shipments", "am" to "ጭነቶች"),
        "Finance" to mapOf("en" to "Finance", "am" to "ፋይናንስ"),
        "System Settings" to mapOf("en" to "System Settings", "am" to "የስርዓት ቅንብሮች"),
        "System Settings & Backup" to mapOf("en" to "System Settings & Backup", "am" to "የስርዓት ቅንብሮች እና ባክአፕ"),
        "THEME SETTINGS" to mapOf("en" to "THEME SETTINGS", "am" to "የገጽታ ቅንብሮች"),
        "Choose your preferred application visual theme." to mapOf("en" to "Choose your preferred application visual theme.", "am" to "የሚመርጡትን የመተግበሪያ ገጽታ ይምረጡ።"),
        "SYSTEM" to mapOf("en" to "SYSTEM", "am" to "ስርዓት"),
        "LIGHT" to mapOf("en" to "LIGHT", "am" to "ብርሃን"),
        "DARK" to mapOf("en" to "DARK", "am" to "ጨለማ"),
        "LANGUAGE SETTINGS" to mapOf("en" to "LANGUAGE SETTINGS", "am" to "የቋንቋ ቅንብሮች"),
        "Select Language" to mapOf("en" to "Select Language", "am" to "ቋንቋ ይምረጡ"),
        "English" to mapOf("en" to "English", "am" to "English (እንግሊዝኛ)"),
        "Amharic" to mapOf("en" to "Amharic", "am" to "አማርኛ"),
        "CLOUD SYNC & BACKUP (FIRESTORE)" to mapOf("en" to "CLOUD SYNC & BACKUP (FIRESTORE)", "am" to "የክላውድ ማሳመሪያ እና ባክአፕ (ፋየርስተር)"),
        "Securely sync your inventory and shipments to the cloud and access them across multiple devices using a Group Sync Key." to mapOf(
            "en" to "Securely sync your inventory and shipments to the cloud and access them across multiple devices using a Group Sync Key.",
            "am" to "የቡድን ማሳመሪያ ቁልፍን በመጠቀም የዕቃዎችዎን እና የጭነቶችዎን መረጃ ከክላውድ ጋር በቀላሉ ያሳምሩ።"
        ),
        "Group Sync Key" to mapOf("en" to "Group Sync Key", "am" to "የቡድን ማሳመሪያ ቁልፍ"),
        "Upload" to mapOf("en" to "Upload", "am" to "ጫን (Upload)"),
        "Download" to mapOf("en" to "Download", "am" to "አውርድ (Download)"),
        "Sync successful!" to mapOf("en" to "Sync successful!", "am" to "ማሳመር ተሳክቷል!"),
        "Restore successful!" to mapOf("en" to "Restore successful!", "am" to "መረጃ መመለስ ተሳክቷል!"),
        
        // Dashboard
        "Addis Spice Hub Dashboard" to mapOf("en" to "Addis Spice Hub Dashboard", "am" to "አዲስ የቅመም ማዕከል ዳሽቦርድ"),
        "Region: Mekelle & Addis Ababa Ecosystem" to mapOf("en" to "Region: Mekelle & Addis Ababa Ecosystem", "am" to "ክልል: መቀሌ እና አዲስ አበባ ንግድ"),
        "Financial Summary" to mapOf("en" to "Financial Summary", "am" to "የፋይናንስ ማጠቃለያ"),
        "Total Stock Value" to mapOf("en" to "Total Stock Value", "am" to "አጠቃላይ የክምችት ዋጋ"),
        "Total Shipments" to mapOf("en" to "Total Shipments", "am" to "አጠቃላይ ጭነቶች"),
        "Net Profit" to mapOf("en" to "Net Profit", "am" to "የተጣራ ትርፍ"),
        "Total Commission" to mapOf("en" to "Total Commission", "am" to "አጠቃላይ ኮሚሽን"),
        "Total Transport Cost" to mapOf("en" to "Total Transport Cost", "am" to "አጠቃላይ የትራንስፖርት ወጪ"),
        "Stock Highlights by Category" to mapOf("en" to "Stock Highlights by Category", "am" to "የክምችት ድምቀቶች በምድብ"),
        "No items in stock. Add some in the Inventory tab." to mapOf("en" to "No items in stock. Add some in the Inventory tab.", "am" to "ክምችት ውስጥ ምንም ዕቃ የለም። እባክዎ በዕቃዎች ዝርዝር ገጽ ላይ ይጨምሩ።"),
        
        // Database maintenance
        "DATABASE MAINTENANCE" to mapOf("en" to "DATABASE MAINTENANCE", "am" to "የመረጃ ቋት ጥገና"),
        "Start fresh from zero or restore default sample data." to mapOf("en" to "Start fresh from zero or restore default sample data.", "am" to "ከዜሮ ይጀምሩ ወይም ናሙና መረጃዎችን ይመልሱ።"),
        "Reset to Zero" to mapOf("en" to "Reset to Zero", "am" to "ከዜሮ ጀምር (Reset)"),
        "Load Seed Data" to mapOf("en" to "Load Seed Data", "am" to "ናሙና መረጃ ጫን (Seed)"),
        "EXPORT BACKUP DATA" to mapOf("en" to "EXPORT BACKUP DATA", "am" to "መረጃ ወደ ውጪ ላክ (Export)"),
        "IMPORT BACKUP DATA" to mapOf("en" to "IMPORT BACKUP DATA", "am" to "መረጃ ወደ ውስጥ አስገባ (Import)"),
        
        "Add New Item" to mapOf("en" to "Add New Item", "am" to "አዲስ ዕቃ አክል"),
        "Add Item" to mapOf("en" to "Add Item", "am" to "ዕቃ አክል"),
        "Save" to mapOf("en" to "Save", "am" to "አስቀምጥ"),
        "Cancel" to mapOf("en" to "Cancel", "am" to "ሰርዝ"),
        "Product Name" to mapOf("en" to "Product Name", "am" to "የዕቃው ስም"),
        "Category" to mapOf("en" to "Category", "am" to "ምድብ"),
        "Unit" to mapOf("en" to "Unit", "am" to "መለኪያ"),
        "Buying Price" to mapOf("en" to "Buying Price", "am" to "የመግዣ ዋጋ"),
        "Quantity" to mapOf("en" to "Quantity", "am" to "ብዛት"),
        "Notes" to mapOf("en" to "Notes", "am" to "ማስታወሻዎች"),
        "Take Photo" to mapOf("en" to "Take Photo", "am" to "ፎቶ አንሳ"),
        "Retake Photo" to mapOf("en" to "Retake Photo", "am" to "ፎቶ ድገም"),
        "Remove Photo" to mapOf("en" to "Remove Photo", "am" to "ፎቶ አስወግድ"),
        "Product Photo" to mapOf("en" to "Product Photo", "am" to "የዕቃው ፎቶ"),
        "No Photo Available" to mapOf("en" to "No Photo Available", "am" to "ፎቶ አልተገኘም"),
        "Last Synced: " to mapOf("en" to "Last Synced: ", "am" to "ለመጨረሻ ጊዜ የተመሳሰለው: "),
        "Cloud Backup Detected:" to mapOf("en" to "Cloud Backup Detected:", "am" to "የክላውድ ባክአፕ ተገኝቷል:"),
        "Inventory Items: " to mapOf("en" to "Inventory Items: ", "am" to "የዕቃዎች ብዛት: "),
        "Shipments: " to mapOf("en" to "Shipments: ", "am" to "የጭነቶች ብዛት: "),
        "Device: " to mapOf("en" to "Device: ", "am" to "መሳሪያ: ")
    )

    fun translate(key: String, lang: String): String {
        val entry = translations[key] ?: return key
        return entry[lang] ?: entry["en"] ?: key
    }
}
