package io.toolbox.passwdmanager.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import io.toolbox.passwdmanager.data.PasswordStorage
import io.toolbox.passwdmanager.ui.screens.AboutScreen
import io.toolbox.passwdmanager.ui.screens.MainScreen
import io.toolbox.passwdmanager.ui.screens.PasswordDetailsScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

val LocalNavController: ProvidableCompositionLocal<NavController> = compositionLocalOf { error("") }

@Composable
@Preview
fun App() {
    AppTheme {
        val navController = rememberNavController()

        CompositionLocalProvider(LocalNavController provides navController) {
            NavHost(
                navController = navController,
                startDestination = "main",
                enterTransition = { scaleIn(initialScale = 0.9f) + fadeIn() },
                exitTransition = { scaleOut(targetScale = 0.9f) + fadeOut() }
            ) {
                composable("main") { MainScreen() }
                composable("about") { AboutScreen() }
                composable(
                    route = "password/{index}",
                    arguments = listOf(
                        navArgument("index") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val index = backStackEntry.arguments!!.read { getInt("index") }
                    PasswordDetailsScreen(PasswordStorage.passwords[index], index)
                }
            }
        }
    }
}