package com.pr0gramm3r101.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.lang.System.getProperty
import java.lang.System.getenv

private class DesktopPlatform : Platform {
    override val model get() = "Desktop"
    override val osName get() = getProperty("os.name")
    override val osVersion get() = getProperty("os.version")
    override val isTablet get() = false
    override val isPhone get() = false
    override val isDesktop get() = true
    override val hasDisplayCutout get() = false
}

actual val platform: Platform = DesktopPlatform()

actual fun Modifier.clearFocusOnKeyboardDismiss() = this

@Composable
actual fun ToggleNavScrimEffect(enabled: Boolean) {}
actual val materialYouAvailable get() = false

actual fun hasDisplayCutout(): Boolean = false 