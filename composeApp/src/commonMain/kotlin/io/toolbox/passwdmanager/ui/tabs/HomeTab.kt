package io.toolbox.passwdmanager.ui.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pr0gramm3r101.components.Category
import com.pr0gramm3r101.components.CategoryDefaults
import com.pr0gramm3r101.components.ListItem
import com.pr0gramm3r101.utils.copy
import com.pr0gramm3r101.utils.invoke
import io.toolbox.passwdmanager.Res
import io.toolbox.passwdmanager.data.PasswordEntry
import io.toolbox.passwdmanager.data.PasswordStorage
import io.toolbox.passwdmanager.home
import io.toolbox.passwdmanager.ui.LocalNavController
import io.toolbox.passwdmanager.ui.components.AddPasswordDialog
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun HomeTab() {
    TabBase {
        val scrollBehavior =
            TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
        val navController = LocalNavController()

        var showDialog by remember { mutableStateOf(false) }
        var passwords = PasswordStorage.passwords
        val scope = rememberCoroutineScope()

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                MediumTopAppBar(
                    title = {
                        Text(
                            text = stringResource(Res.string.home),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = { Text("New") },
                    icon = { Icon(Icons.Filled.Add, contentDescription = "") },
                    onClick = { showDialog = true }
                )
            }
        ) { innerPadding ->
            Column(
                Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                @Composable
                fun PasswordCard(entry: PasswordEntry) {
                    Category(padding = CategoryDefaults.padding.copy(vertical = 5.dp)) {
                        ListItem(
                            headline = entry.login.ifEmpty { "No login" },
                            supportingText = entry.password.replace(".".toRegex(), "*"),
                            onClick = {
                                navController.navigate("password/${passwords.indexOf(entry)}")
                            }
                        )
                    }
                }

                passwords.forEach { entry ->
                    PasswordCard(entry)
                }

                if (showDialog) {
                    AddPasswordDialog(
                        onDismissRequest = { showDialog = false },
                        onAddPassword = { login, password ->
                            scope.launch {
                                PasswordStorage.add(PasswordEntry(login, password))
                                showDialog = false
                            }
                        }
                    )
                }
            }
        }
    }
}