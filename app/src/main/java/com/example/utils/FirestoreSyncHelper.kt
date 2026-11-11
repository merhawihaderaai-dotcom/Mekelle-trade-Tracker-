package com.example.utils

import android.content.Context
import android.util.Log
import com.example.data.BackupData
import com.example.data.BackupHelper
import com.example.data.InventoryItem
import com.example.data.Shipment
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

object FirestoreSyncHelper {
    private const val TAG = "FirestoreSyncHelper"

    // Helper to convert Google Tasks API to suspend coroutines
    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                continuation.resume(task.result)
            } else {
                continuation.resumeWithException(task.exception ?: RuntimeException("Firebase Task failed"))
            }
        }
    }

    // Retrieve Firestore instance dynamically based on configured settings
    fun getFirestore(context: Context): FirebaseFirestore? {
        return try {
            // Check if default FirebaseApp is initialized
            val app = try {
                FirebaseApp.getInstance()
            } catch (e: IllegalStateException) {
                // Not initialized. Try to initialize from shared preferences if custom options exist
                val (projId, apiKey, appId) = getConfig(context)
                if (projId.isNotEmpty() && apiKey.isNotEmpty() && appId.isNotEmpty()) {
                    val options = FirebaseOptions.Builder()
                        .setProjectId(projId)
                        .setApiKey(apiKey)
                        .setApplicationId(appId)
                        .build()
                    FirebaseApp.initializeApp(context, options)
                } else {
                    null
                }
            }
            if (app != null) {
                FirebaseFirestore.getInstance()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing or retrieving Firestore", e)
            null
        }
    }

    // Save configuration settings
    fun saveConfig(context: Context, projectId: String, apiKey: String, appId: String) {
        context.getSharedPreferences("firebase_sync_prefs", Context.MODE_PRIVATE).edit()
            .putString("project_id", projectId.trim())
            .putString("api_key", apiKey.trim())
            .putString("app_id", appId.trim())
            .apply()
    }

    fun getConfig(context: Context): Triple<String, String, String> {
        val prefs = context.getSharedPreferences("firebase_sync_prefs", Context.MODE_PRIVATE)
        return Triple(
            prefs.getString("project_id", "") ?: "",
            prefs.getString("api_key", "") ?: "",
            prefs.getString("app_id", "") ?: ""
        )
    }

    fun hasCustomConfig(context: Context): Boolean {
        val (projId, apiKey, appId) = getConfig(context)
        return projId.isNotEmpty() && apiKey.isNotEmpty() && appId.isNotEmpty()
    }

    fun isFirebaseAvailable(context: Context): Boolean {
        return getFirestore(context) != null
    }

    suspend fun uploadSync(
        context: Context,
        groupKey: String,
        inventory: List<InventoryItem>,
        shipments: List<Shipment>
    ): Boolean {
        val firestore = getFirestore(context) ?: return false
        return try {
            val backupJson = BackupHelper.exportBackup(inventory, shipments)
            val data = mapOf(
                "backupJson" to backupJson,
                "lastSyncedAt" to java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
                "inventoryCount" to inventory.size,
                "shipmentCount" to shipments.size,
                "deviceName" to android.os.Build.MODEL
            )
            firestore.collection("sync_groups")
                .document(groupKey.trim())
                .set(data)
                .awaitTask()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Sync upload failed", e)
            false
        }
    }

    suspend fun downloadSync(
        context: Context,
        groupKey: String
    ): BackupData? {
        val firestore = getFirestore(context) ?: return null
        return try {
            val document = firestore.collection("sync_groups")
                .document(groupKey.trim())
                .get()
                .awaitTask()

            if (document.exists()) {
                val json = document.getString("backupJson") ?: return null
                BackupHelper.importBackup(json)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync download failed", e)
            null
        }
    }

    suspend fun getSyncMetadata(
        context: Context,
        groupKey: String
    ): Map<String, Any>? {
        val firestore = getFirestore(context) ?: return null
        return try {
            val document = firestore.collection("sync_groups")
                .document(groupKey.trim())
                .get()
                .awaitTask()
            if (document.exists()) {
                document.data
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get sync metadata", e)
            null
        }
    }
}
