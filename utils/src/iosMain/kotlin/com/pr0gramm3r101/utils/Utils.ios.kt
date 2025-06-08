@file:OptIn(ExperimentalNativeApi::class)
@file:Suppress("NOTHING_TO_INLINE")

package com.pr0gramm3r101.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIUserInterfaceIdiom
import platform.posix._OSSwapInt16
import platform.posix._OSSwapInt32
import platform.posix._OSSwapInt64
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform.isLittleEndian
import platform.UIKit.*
import platform.Foundation.NSProcessInfo

private class IOSPlatform : Platform {
    private val device = UIDevice.currentDevice
    private val processInfo = NSProcessInfo.processInfo

    override val model get() = device.model
    override val osName get() = "iOS"
    override val osVersion get() = device.systemVersion
    override val isTablet get() = device.userInterfaceIdiom == UIUserInterfaceIdiomPad
    override val isPhone get() = device.userInterfaceIdiom == UIUserInterfaceIdiomPhone
    override val isDesktop get() = false
    override val hasDisplayCutout get() = isPhone && model.matches(Regex("iPhone\\d+,.*"))
}

actual val platform: Platform = IOSPlatform()

actual fun Modifier.clearFocusOnKeyboardDismiss() = this

@Composable
actual fun ToggleNavScrimEffect(enabled: Boolean) {}
actual val materialYouAvailable get() = false

inline fun htons(short: UShort) = if (isLittleEndian) _OSSwapInt16(short) else short
inline fun htonl(data: UInt) = if (isLittleEndian) _OSSwapInt32(data) else data
inline fun htonll(data: ULong) = if (isLittleEndian) _OSSwapInt64(data) else data
inline fun ntohs(data: UShort) = if (isLittleEndian) _OSSwapInt16(data) else data
inline fun ntohl(data: UInt) = if (isLittleEndian) _OSSwapInt32(data) else data
inline fun ntohll(data: ULong) = if (isLittleEndian) _OSSwapInt64(data) else data

@OptIn(ExperimentalForeignApi::class)
actual fun hasDisplayCutout(): Boolean = platform.hasDisplayCutout