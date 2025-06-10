package utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File as JavaFile

class DesktopFile(
    override val path: String
) : File {
    private val file = JavaFile(path)

    override var name: String
        get() = file.name
        set(value) {
            val newFile = JavaFile(file.parent, value)
            if (!file.renameTo(newFile)) {
                throw FilePermissionException("Could not rename file: $path")
            }
        }

    override val parent: File?
        get() = file.parent?.let { DesktopFile(it) }

    override suspend fun exists(): Boolean = withContext(Dispatchers.IO) {
        file.exists()
    }

    override suspend fun isDirectory(): Boolean = withContext(Dispatchers.IO) {
        file.isDirectory
    }

    override suspend fun isFile(): Boolean = withContext(Dispatchers.IO) {
        file.isFile
    }

    override suspend fun readText(): String = withContext(Dispatchers.IO) {
        if (!file.exists()) {
            throw FileNotFoundException("File not found: $path")
        }
        file.readText()
    }

    override suspend fun writeText(text: String, append: Boolean) = withContext(Dispatchers.IO) {
        if (append) {
            file.appendText(text)
        } else {
            file.writeText(text)
        }
    }

    override suspend fun delete(): Boolean = withContext(Dispatchers.IO) {
        file.delete()
    }

    override suspend fun mkdirs(): Boolean = withContext(Dispatchers.IO) {
        file.mkdirs()
    }

    override suspend fun listFiles(): List<File> = withContext(Dispatchers.IO) {
        if (!file.exists() || !file.isDirectory) {
            throw FileNotFoundException("Directory not found: $path")
        }
        file.listFiles()?.map { DesktopFile(it.absolutePath) } ?: emptyList()
    }

    override suspend fun length(): Long = withContext(Dispatchers.IO) {
        if (!file.exists()) {
            throw FileNotFoundException("File not found: $path")
        }
        file.length()
    }

    override suspend fun lastModified(): Long = withContext(Dispatchers.IO) {
        if (!file.exists()) {
            throw FileNotFoundException("File not found: $path")
        }
        file.lastModified()
    }

    override suspend fun moveTo(destination: File): Boolean = withContext(Dispatchers.IO) {
        if (destination !is DesktopFile) {
            throw IllegalArgumentException("Destination must be a DesktopFile instance")
        }
        file.renameTo(JavaFile(destination.path))
    }

    override suspend fun copyTo(destination: File): Boolean = withContext(Dispatchers.IO) {
        if (destination !is DesktopFile) {
            throw IllegalArgumentException("Destination must be a DesktopFile instance")
        }
        try {
            file.copyTo(JavaFile(destination.path), overwrite = true)
            true
        } catch (_: Exception) {
            false
        }
    }
}

actual fun createFile(path: String): File = DesktopFile(path)

actual fun inDocuments(path: String): File {
    val userHome = System.getProperty("user.home")
    val documentsPath = JavaFile(userHome, "Documents").absolutePath
    return DesktopFile("$documentsPath/$path")
}

actual fun inCaches(path: String): File {
    val userHome = System.getProperty("user.home")
    val cachePath = JavaFile(userHome, ".cache").absolutePath
    return DesktopFile("$cachePath/$path")
} 