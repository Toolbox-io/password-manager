package io.toolbox.passwdmanager.data

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import utils.FileNotFoundException
import utils.inDocuments
import kotlin.coroutines.cancellation.CancellationException

object PasswordStorage {
    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
    }

    private var _passwords: SnapshotStateList<PasswordEntry>? = null

    private suspend fun update() {
        if (_passwords != null) {
            _passwords!!.clear()
            _passwords!!.addAll(load().toMutableStateList())
        }
    }

    val passwords: SnapshotStateList<PasswordEntry> get() {
        if (_passwords == null) {
            _passwords = runBlocking { load().toMutableStateList() }
        }
        return _passwords!!
    }
    
    private val storageFile = inDocuments("passwords.json")
    
    suspend fun add(entry: PasswordEntry) {
        try {
            val currentStore = try {
                json.decodeFromString<PasswordStore>(storageFile.readText())
            } catch (_: FileNotFoundException) {
                PasswordStore()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                PasswordStore()
            }
            
            val updatedStore = currentStore.copy(
                entries = currentStore.entries + entry
            )
            
            storageFile.writeText(json.encodeToString(serializer<PasswordStore>(), updatedStore))
            update()
        } catch (e: Exception) {
            throw RuntimeException("Failed to save password", e)
        }
    }

    suspend fun load(): List<PasswordEntry> {
        return try {
            val store = json.decodeFromString<PasswordStore>(storageFile.readText())
            store.entries
        } catch (_: FileNotFoundException) {
            emptyList()
        } catch (e: Exception) {
            throw RuntimeException("Failed to load passwords", e)
        }
    }

    suspend fun delete(entry: PasswordEntry) {
        try {
            val currentStore = try {
                json.decodeFromString<PasswordStore>(storageFile.readText())
            } catch (_: FileNotFoundException) {
                return
            } catch (_: Exception) {
                return
            }
            
            val updatedStore = currentStore.copy(
                entries = currentStore.entries.filter { it != entry }
            )
            
            storageFile.writeText(json.encodeToString(serializer<PasswordStore>(), updatedStore))
            update()
        } catch (e: Exception) {
            throw RuntimeException("Failed to delete password", e)
        }
    }
} 