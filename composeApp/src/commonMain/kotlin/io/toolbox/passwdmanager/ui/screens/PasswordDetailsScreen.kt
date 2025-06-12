package io.toolbox.passwdmanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pr0gramm3r101.components.Button
import com.pr0gramm3r101.utils.TweakedOutlinedTextField
import com.pr0gramm3r101.utils.invoke
import io.toolbox.passwdmanager.Res
import io.toolbox.passwdmanager.back
import io.toolbox.passwdmanager.data.PasswordEntry
import io.toolbox.passwdmanager.data.PasswordStorage
import io.toolbox.passwdmanager.hidepw
import io.toolbox.passwdmanager.showpw
import io.toolbox.passwdmanager.ui.LocalNavController
import io.toolbox.passwdmanager.ui.components.PasswordStrengthMeter
import io.toolbox.passwdmanager.utils.PasswordUtils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordDetailsScreen(entry: PasswordEntry, index: Int) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val navController = LocalNavController()
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf(entry.password) }
    var login by remember { mutableStateOf(entry.login) }
    var passwordVisible by remember { mutableStateOf(false) }
    var changed by remember { mutableStateOf(false) }

    fun updateChanged() {
        changed = password != entry.password || login != entry.login
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        "Password info",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navController::navigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete password"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            if (changed) {
                ExtendedFloatingActionButton(
                    onClick = {
                        scope.launch {
                            PasswordStorage.edit(
                                index,
                                PasswordEntry(
                                    login = login,
                                    password = password,
                                    createdAt = entry.createdAt
                                )
                            )
                            changed = false
                        }
                    },
                    icon = { Icon(Icons.Default.Check, contentDescription = null) },
                    text = { Text("Save") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Login info if present
            if (entry.login.isNotBlank()) {
                Text(
                    text = "Login: ${entry.login}",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                )
            }

            TweakedOutlinedTextField(
                value = login,
                onValueChange = {
                    login = it
                    updateChanged()
                },
                label = { Text("Login") },
                placeholder = {
                    Text("No login")
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Password field
            TweakedOutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    updateChanged()
                },
                label = { Text("Password") },
                visualTransformation = if (passwordVisible) 
                    VisualTransformation.None 
                else 
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) 
                                Icons.Default.VisibilityOff 
                            else 
                                Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) 
                                stringResource(Res.string.hidepw) 
                            else 
                                stringResource(Res.string.showpw)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Row {
                Button(
                    onClick = {
                        password = PasswordUtils.improve(password)
                        updateChanged()
                    },
                    icon = {
                        Icon(Icons.Filled.AutoFixHigh, null)
                    },
                    enabled = password.isNotBlank() && password.length < 12
                ) {
                    Text("Improve")
                }
            }

            // Password strength meter
            PasswordStrengthMeter(
                password = password,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Password") },
            text = { Text("Are you sure you want to delete this password? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        navController.navigateUp()
                        MainScope().launch {
                            delay(1000)
                            PasswordStorage.delete(entry)
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}