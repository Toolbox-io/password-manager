package io.toolbox.passwdmanager.ui.tabs

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pr0gramm3r101.components.Category
import com.pr0gramm3r101.components.CategoryDefaults
import com.pr0gramm3r101.components.ListItem
import com.pr0gramm3r101.utils.AnimatedCrosslineIcon
import com.pr0gramm3r101.utils.TweakedOutlinedTextField
import com.pr0gramm3r101.utils.copy
import com.pr0gramm3r101.utils.platform
import com.pr0gramm3r101.utils.verticalScroll
import io.toolbox.passwdmanager.Res
import io.toolbox.passwdmanager.hidepw
import io.toolbox.passwdmanager.home
import io.toolbox.passwdmanager.showpw
import io.toolbox.passwdmanager.ui.components.PasswordStrengthMeter
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

        val sheetState = rememberModalBottomSheetState()
        val scope = rememberCoroutineScope()
        var showBottomSheet by remember { mutableStateOf(false) }

        fun hideBottomSheet() {
            scope
                .launch { sheetState.hide() }
                .invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        showBottomSheet = false
                    }
                }
        }

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
                    onClick = { showBottomSheet = true }
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

                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            showBottomSheet = false
                        },
                        sheetState = sheetState,
                        dragHandle = {
                            val alpha by animateFloatAsState(
                                if ((platform.osName != "iOS") && sheetState.targetValue == SheetValue.Expanded) 0f
                                else 1f
                            )

                            BottomSheetDefaults.DragHandle(Modifier.alpha(alpha))
                        }
                    ) {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Text(
                                            text = "Add password",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = Color.Transparent
                                    ),
                                    windowInsets = WindowInsets(0),
                                    navigationIcon = {
                                        IconButton(
                                            onClick = ::hideBottomSheet
                                        ) {
                                            Icon(Icons.Filled.Close, null)
                                        }
                                    }
                                )
                            },
                            floatingActionButton = {
                                ExtendedFloatingActionButton(
                                    text = { Text("Done") },
                                    icon = { Icon(Icons.Filled.Add, contentDescription = "") },
                                    onClick = {
                                        hideBottomSheet()
                                    }
                                )
                            },
                            containerColor = Color.Transparent
                        ) { innerPadding ->
                            Column(
                                Modifier
                                    .padding(innerPadding)
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 16.dp)
                                    .verticalScroll()
                            ) {
                                var login by remember { mutableStateOf("") }
                                var password by remember { mutableStateOf("") }
                                var passwordShown by remember { mutableStateOf(false) }

                                OutlinedTextField(
                                    value = login,
                                    onValueChange = { login = it },
                                    label = { Text("Login (optional)") },
                                    modifier = Modifier.width(300.dp)
                                )

                                TweakedOutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("Password") },
                                    visualTransformation =
                                        if (passwordShown)
                                            VisualTransformation.None
                                        else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                passwordShown = !passwordShown
                                            }
                                        ) {
                                            AnimatedCrosslineIcon(
                                                icon = Icons.Filled.Visibility,
                                                crossline = passwordShown,
                                                contentDescription = if (passwordShown) 
                                                    stringResource(Res.string.hidepw)
                                                else
                                                    stringResource(Res.string.showpw)
                                            )
                                        }
                                    },
                                    singleLine = true,
                                    modifier = Modifier.width(300.dp)
                                )

                                // Add password strength meter
                                PasswordStrengthMeter(
                                    password = password,
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .width(300.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}