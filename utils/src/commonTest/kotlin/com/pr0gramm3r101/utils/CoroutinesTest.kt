package com.pr0gramm3r101.utils

import kotlinx.coroutines.delay
import kotlin.test.Test
import kotlin.test.assertEquals

class CoroutinesTest {
    @Test
    fun testRunBlocking() {
        val result = runBlocking {
            delay(100) // Test that we can use suspend functions
            "Hello, Coroutines!"
        }
        assertEquals("Hello, Coroutines!", result)
    }

    @Test
    fun testRunBlockingWithContext() {
        val result = runBlocking(kotlinx.coroutines.Dispatchers.Default) {
            delay(100)
            "Hello from Default Dispatcher!"
        }
        assertEquals("Hello from Default Dispatcher!", result)
    }

    @Test
    fun testRunBlockingWithException() {
        val exception = try {
            runBlocking {
                delay(100)
                throw RuntimeException("Test exception")
            }
            null
        } catch (e: RuntimeException) {
            e
        }
        assertEquals("Test exception", exception?.message)
    }
} 