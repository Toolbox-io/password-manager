package io.toolbox.passwdmanager.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class PasswordEntry(
    val login: String,
    val password: String,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds()
)

@Serializable
data class PasswordStore(
    val entries: List<PasswordEntry> = emptyList()
)