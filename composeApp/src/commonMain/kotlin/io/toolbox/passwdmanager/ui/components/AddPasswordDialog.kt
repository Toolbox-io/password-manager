package io.toolbox.passwdmanager.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.pr0gramm3r101.utils.AnimatedCrosslineIcon
import com.pr0gramm3r101.utils.TweakedOutlinedTextField
import com.pr0gramm3r101.utils.clearsFocus
import com.pr0gramm3r101.utils.verticalScroll
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import io.toolbox.passwdmanager.Res
import io.toolbox.passwdmanager.hidepw
import io.toolbox.passwdmanager.showpw
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun AddPasswordDialog(
    onDismissRequest: () -> Unit,
    onAddPassword: (login: String, password: String) -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp)
                        .clearsFocus()
                        .verticalScroll(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                            IconButton(onClick = onDismissRequest) {
                                Icon(Icons.Filled.Close, null)
                            }
                        }
                    )

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
                        visualTransformation = if (passwordShown)
                            VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(
                                onClick = { passwordShown = !passwordShown }
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
                        modifier = Modifier.width(300.dp),
                        isError = password.isBlank() || ' ' in password
                    )

                    PasswordStrengthMeter(
                        password = password,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .width(300.dp)
                    )

                    ExtendedFloatingActionButton(
                        onClick = { onAddPassword(login, password) },
                        icon = { Icon(Icons.Filled.Check, null) },
                        text = { Text("Add") },
                        expanded = !(password.isBlank() || ' ' in password),
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 16.dp)
                            .align(Alignment.End)
                    )
                }
            }
        }
    }
} 