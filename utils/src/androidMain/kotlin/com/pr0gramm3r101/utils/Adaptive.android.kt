@file:Suppress("NOTHING_TO_INLINE")

package com.pr0gramm3r101.utils

import androidx.compose.runtime.Composable

@Composable
actual inline fun currentWindowSize() = androidx.compose.material3.adaptive.currentWindowSize()