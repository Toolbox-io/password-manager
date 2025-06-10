package io.toolbox.passwdmanager.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import utils.FileNotFoundException
import utils.inDocuments
import kotlin.coroutines.cancellation.CancellationException

object PasswordStorage {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    private val storageFile = inDocuments("passwords.json")
    
    suspend fun savePassword(entry: PasswordEntry) {
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw RuntimeException("Failed to save password", e)
        }
    }

    suspend fun loadPasswords(): List<PasswordEntry> {
        return try {
            val store = json.decodeFromString<PasswordStore>(storageFile.readText())
            store.entries
        } catch (_: FileNotFoundException) {
            emptyList()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw RuntimeException("Failed to load passwords", e)
        }
    }

    suspend fun deletePassword(entry: PasswordEntry) {
        try {
            val currentStore = try {
                json.decodeFromString<PasswordStore>(storageFile.readText())
            } catch (_: FileNotFoundException) {
                return
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                return
            }
            
            val updatedStore = currentStore.copy(
                entries = currentStore.entries.filter { it != entry }
            )
            
            storageFile.writeText(json.encodeToString(serializer<PasswordStore>(), updatedStore))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw RuntimeException("Failed to delete password", e)
        }
    }
} 