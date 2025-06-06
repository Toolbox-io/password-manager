@file:OptIn(ExperimentalForeignApi::class)

package utils

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.BooleanVar
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.set
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileHandle
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileModificationDate
import platform.Foundation.NSFileSize
import platform.Foundation.NSNumber
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.closeFile
import platform.Foundation.create
import platform.Foundation.dataWithBytes
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.fileHandleForWritingAtPath
import platform.Foundation.seekToEndOfFile
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeData
import platform.Foundation.writeToFile

class IosFile(
    override val path: String
) : File {
    private val fileManager = NSFileManager.defaultManager
    private val url: NSURL
        get() = NSURL.fileURLWithPath(path)

    override var name: String
        get() = url.lastPathComponent?.toString() ?: path
        set(value) {
            val newUrl = url.URLByDeletingLastPathComponent?.URLByAppendingPathComponent(value)
                ?: throw FileNotFoundException("Could not create new URL for name: $value")
            try {
                fileManager.moveItemAtURL(url, newUrl, error = null)
            } catch (e: Exception) {
                throw FilePermissionException("Could not rename file: ${e.message}")
            }
        }

    override val parent: File?
        get() = url.URLByDeletingLastPathComponent?.path?.let { IosFile(it) }

    override suspend fun exists(): Boolean = withContext(Dispatchers.Default) {
        fileManager.fileExistsAtPath(path)
    }

    override suspend fun isDirectory(): Boolean = withContext(Dispatchers.Default) {
        val isDir = memScoped {
            alloc<BooleanVar>()
        }
        fileManager.fileExistsAtPath(path, isDirectory = isDir.ptr)
        isDir.value
    }

    override suspend fun isFile(): Boolean = withContext(Dispatchers.Default) {
        exists() && !isDirectory()
    }

    @OptIn(BetaInteropApi::class)
    override suspend fun readText(): String = withContext(Dispatchers.Default) {
        val data = NSData.dataWithContentsOfFile(path)
            ?: throw FileNotFoundException("File not found: $path")
        NSString.create(data, NSUTF8StringEncoding)?.toString()
            ?: throw FileNotFoundException("Could not read file: $path")
    }

    @OptIn(BetaInteropApi::class)
    override suspend fun writeText(text: String, append: Boolean) = withContext(Dispatchers.Default) {
        memScoped {
            val data = text.encodeToByteArray()
            val bytes = allocArray<ByteVar>(data.size)
            data.forEachIndexed { index, byte ->
                bytes[index] = byte
            }
            
            if (append) {
                val fileHandle = NSFileHandle.fileHandleForWritingAtPath(path)
                    ?: throw FileNotFoundException("Could not open file for writing: $path")
                fileHandle.seekToEndOfFile()
                fileHandle.writeData(NSData.dataWithBytes(bytes, data.size.toULong()))
                fileHandle.closeFile()
            } else {
                NSData.dataWithBytes(bytes, data.size.toULong()).writeToFile(path, atomically = true)
            }
        }
        Unit
    }

    override suspend fun delete(): Boolean = withContext(Dispatchers.Default) {
        try {
            fileManager.removeItemAtPath(path, error = null)
            true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun mkdirs(): Boolean = withContext(Dispatchers.Default) {
        try {
            fileManager.createDirectoryAtPath(
                path,
                withIntermediateDirectories = true,
                attributes = null,
                error = null
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun listFiles(): List<File> = withContext(Dispatchers.Default) {
        val contents = fileManager.contentsOfDirectoryAtPath(path, error = null)
            ?: throw FileNotFoundException("Directory not found: $path")
        contents.map { IosFile("$path/$it") }
    }

    override suspend fun length(): Long = withContext(Dispatchers.Default) {
        val attributes = fileManager.attributesOfItemAtPath(path, error = null)
            ?: throw FileNotFoundException("File not found: $path")
        val size = attributes[NSFileSize] as? NSNumber
            ?: throw FileNotFoundException("Could not get file size: $path")
        size.longLongValue
    }

    override suspend fun lastModified(): Long = withContext(Dispatchers.Default) {
        val attributes = fileManager.attributesOfItemAtPath(path, error = null)
            ?: throw FileNotFoundException("File not found: $path")
        val date = attributes[NSFileModificationDate] as? NSDate
            ?: throw FileNotFoundException("Could not get modification date: $path")
        (date.timeIntervalSince1970 * 1000).toLong()
    }

    override suspend fun moveTo(destination: File): Boolean = withContext(Dispatchers.Default) {
        if (destination !is IosFile) {
            throw IllegalArgumentException("Destination must be an IosFile instance")
        }
        try {
            fileManager.moveItemAtPath(path, destination.path, error = null)
            true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun copyTo(destination: File): Boolean = withContext(Dispatchers.Default) {
        if (destination !is IosFile) {
            throw IllegalArgumentException("Destination must be an IosFile instance")
        }
        try {
            fileManager.copyItemAtPath(path, destination.path, error = null)
            true
        } catch (_: Exception) {
            false
        }
    }
}

actual fun createFile(path: String): File = IosFile(path)

actual fun inDocuments(path: String): File {
    val documentsPath = NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory,
        NSUserDomainMask,
        true
    ).firstOrNull() ?: throw FileNotFoundException("Could not access Documents directory")
    return IosFile("$documentsPath/$path")
}

actual fun inCaches(path: String): File {
    val cachesPath = NSSearchPathForDirectoriesInDomains(
        NSCachesDirectory,
        NSUserDomainMask,
        true
    ).firstOrNull() ?: throw FileNotFoundException("Could not access Caches directory")
    return IosFile("$cachesPath/$path")
}