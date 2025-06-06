package io.toolbox.passwdmanager.utils

import com.pr0gramm3r101.utils.assert
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import utils.createFile
import utils.inCaches
import utils.inDocuments
import kotlin.test.Test

class FileApiTest {
    @Test
    fun testBasicFileOperations() = runTest {
        // Test creating a file
        val testFile = createFile("test.txt")
        assert(testFile.exists()) { "File should exist after creation" }

        // Test writing and reading
        testFile.writeText("Hello, File API!")
        val content = testFile.readText()
        assert(content == "Hello, File API!") { "Content mismatch" }

        // Cleanup
        testFile.delete()
        assert(!testFile.exists()) { "File should be deleted" }
    }

    @Test
    fun testFileProperties() = runTest {
        val testFile = createFile("test.txt")
        try {
            testFile.writeText("Test content")

            assert(testFile.name == "test.txt") { "File name should be 'test.txt'" }
            assert(testFile.isFile()) { "Should be a file" }
            assert(testFile.length() > 0) { "File should have content" }
        } finally {
            testFile.delete()
        }
    }

    @Test
    fun testDocumentsDirectory() = runTest {
        val docFile = inDocuments("test_doc.txt")
        try {
            docFile.writeText("Test document content")
            assert(docFile.exists()) { "Document file should exist" }
            assert(docFile.readText() == "Test document content") { "Document content mismatch" }
        } finally {
            docFile.delete()
            assert(!docFile.exists()) { "Document file should be deleted" }
        }
    }

    @Test
    fun testCachesDirectory() = runTest {
        val cacheFile = inCaches("test_cache.txt")
        try {
            cacheFile.writeText("Test cache content")
            assert(cacheFile.exists()) { "Cache file should exist" }
            assert(cacheFile.readText() == "Test cache content") { "Cache content mismatch" }
        } finally {
            cacheFile.delete()
            assert(!cacheFile.exists()) { "Cache file should be deleted" }
        }
    }

    @Test
    fun testFileAppend() = runTest {
        val testFile = createFile("append_test.txt")
        try {
            // Initial write
            testFile.writeText("Initial content")
            assert(testFile.readText() == "Initial content") { "Initial content mismatch" }

            // Append content
            testFile.writeText("Appended content", append = true)
            assert(testFile.readText() == "Initial contentAppended content") { "Appended content mismatch" }
        } finally {
            testFile.delete()
        }
    }

    @Test
    fun testFileRename() = runTest {
        val originalFile = createFile("original.txt")
        try {
            originalFile.writeText("Test content")
            val originalContent = originalFile.readText()

            // Rename the file
            originalFile.name = "renamed.txt"
            assert(originalFile.name == "renamed.txt") { "File name should be updated" }
            assert(originalFile.exists()) { "File should still exist after rename" }
            assert(originalFile.readText() == originalContent) { "Content should be preserved after rename" }
            assert(!createFile("original.txt").exists()) { "Old file must be deleted" }
        } finally {
            originalFile.delete()
        }
    }

    @Test
    fun testLastModified() = runTest {
        val testFile = createFile("modified_test.txt")
        try {
            // Initial write
            testFile.writeText("Initial content")
            val initialModified = testFile.lastModified()

            // Wait a bit
            kotlinx.coroutines.delay(1000)

            // Modify file
            testFile.writeText("Updated content")
            val updatedModified = testFile.lastModified()

            assert(updatedModified > initialModified) { "Last modified time should be updated" }
        } finally {
            testFile.delete()
        }
    }

    @Test
    fun testConcurrentAccess() = runTest {
        val testFile = createFile("concurrent_test.txt")
        try {
            // Launch multiple coroutines to write to the file
            val jobs = List(5) { index ->
                launch {
                    testFile.writeText("Content from coroutine $index", append = true)
                }
            }

            // Wait for all writes to complete
            jobs.forEach { it.join() }

            // Verify the content
            val content = testFile.readText()
            assert(content.isNotEmpty()) { "File should have content from all coroutines" }
        } finally {
            testFile.delete()
        }
    }

    @Test
    fun testLargeFileOperations() = runTest {
        val testFile = createFile("large_test.txt")
        try {
            // Create a large string (1MB)
            val largeContent = "x".repeat(1024 * 1024)

            // Write large content
            testFile.writeText(largeContent)
            assert(testFile.length() == largeContent.length.toLong()) { "File size should match content length" }

            // Read and verify
            val readContent = testFile.readText()
            assert(readContent == largeContent) { "Large content should be preserved" }
        } finally {
            testFile.delete()
        }
    }

    @Test
    fun testFileOperationsInDifferentDirectories() = runTest {
        val docFile = inDocuments("test_doc.txt")
        val cacheFile = inCaches("test_cache.txt")

        try {
            // Write to both files
            docFile.writeText("Document content")
            cacheFile.writeText("Cache content")

            // Verify both files exist and have correct content
            assert(docFile.exists()) { "Document file should exist" }
            assert(cacheFile.exists()) { "Cache file should exist" }
            assert(docFile.readText() == "Document content") { "Document content mismatch" }
            assert(cacheFile.readText() == "Cache content") { "Cache content mismatch" }

            // Verify file paths are different
            assert(docFile.path != cacheFile.path) { "Files should be in different directories" }
        } finally {
            docFile.delete()
            cacheFile.delete()
        }
    }
} 