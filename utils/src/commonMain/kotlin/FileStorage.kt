package utils

/**
 * Represents a file or directory in the file system.
 * This interface provides a common API for file operations across different platforms.
 */
interface File {
    /**
     * The path of this file/directory
     */
    val path: String

    /**
     * The name of this file/directory
     */
    var name: String

    /**
     * The parent directory of this file/directory
     */
    val parent: File?

    /**
     * Whether this file/directory exists
     */
    suspend fun exists(): Boolean

    /**
     * Whether this is a directory
     */
    suspend fun isDirectory(): Boolean

    /**
     * Whether this is a file
     */
    suspend fun isFile(): Boolean

    /**
     * Reads the contents of this file
     * @return The contents of the file as a String
     * @throws FileNotFoundException if the file doesn't exist
     */
    suspend fun readText(): String

    /**
     * Writes text content to this file
     * @param text The content to write
     * @param append Whether to append to existing content or overwrite
     */
    suspend fun writeText(text: String, append: Boolean = false)

    /**
     * Deletes this file/directory
     * @return true if the file/directory was deleted successfully, false otherwise
     */
    suspend fun delete(): Boolean

    /**
     * Creates this directory and any necessary parent directories
     * @return true if the directory was created successfully, false otherwise
     */
    suspend fun mkdirs(): Boolean

    /**
     * Lists the contents of this directory
     * @return A list of File objects in this directory
     * @throws FileNotFoundException if this is not a directory
     */
    suspend fun listFiles(): List<File>

    /**
     * Gets the size of this file in bytes
     * @return The size of the file in bytes
     * @throws FileNotFoundException if this is not a file
     */
    suspend fun length(): Long

    /**
     * Gets the last modified time of this file/directory
     * @return The last modified time in milliseconds since epoch
     */
    suspend fun lastModified(): Long

    /**
     * Moves this file/directory to the specified destination
     * @param destination The destination File
     * @return true if the move was successful, false otherwise
     */
    suspend fun moveTo(destination: File): Boolean

    /**
     * Copies this file/directory to the specified destination
     * @param destination The destination File
     * @return true if the copy was successful, false otherwise
     */
    suspend fun copyTo(destination: File): Boolean
}

expect fun createFile(path: String): File
expect fun inDocuments(path: String): File
expect fun inCaches(path: String): File

/**
 * Exception thrown when a file operation fails because the file doesn't exist
 */
class FileNotFoundException(message: String) : Exception(message)

/**
 * Exception thrown when a file operation fails due to permission issues
 */
class FilePermissionException(message: String) : Exception(message) 