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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pr0gramm3r101.components.Category
import com.pr0gramm3r101.components.CategoryDefaults
import com.pr0gramm3r101.components.ListItem
import com.pr0gramm3r101.utils.copy
import io.toolbox.passwdmanager.Res
import io.toolbox.passwdmanager.home
import io.toolbox.passwdmanager.ui.components.AddPasswordDialog
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun HomeTab() {
    TabBase {
        val scrollBehavior =
            TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

        var showDialog by remember { mutableStateOf(false) }

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
                fun PasswordCard(login: String, password: String) {
                    Category(padding = CategoryDefaults.padding.copy(vertical = 5.dp)) {
                        ListItem(
                            headline = login,
                            supportingText = password
                        )
                    }
                }

                PasswordCard("test@gmail.com", "test123")
                PasswordCard("test@gmail.com", "test123")
                PasswordCard("test@gmail.com", "test123")
                PasswordCard("test@gmail.com", "test123")
                PasswordCard("test@gmail.com", "test123")

                if (showDialog) {
                    AddPasswordDialog(
                        onDismissRequest = { showDialog = false },
                        onAddPassword = { login, password ->
                            // TODO: Handle adding password
                            showDialog = false
                        }
                    )
                }
            }
        }
    }
}