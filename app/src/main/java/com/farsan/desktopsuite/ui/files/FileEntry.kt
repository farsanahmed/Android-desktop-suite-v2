package com.farsan.desktopsuite.ui.files

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FileEntry(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val modifiedMillis: Long
)

suspend fun listDirectory(path: String): Result<List<FileEntry>> = withContext(Dispatchers.IO) {
    try {
        val dir = File(path)
        if (!dir.exists() || !dir.isDirectory || !dir.canRead()) {
            return@withContext Result.failure(Exception("Cannot read this folder"))
        }
        val files = dir.listFiles()
        if (files == null) {
            return@withContext Result.failure(Exception("Cannot read this folder"))
        }
        val entries = files
            .filter { !it.name.startsWith(".") }
            .map { f ->
                FileEntry(
                    name = f.name,
                    path = f.absolutePath,
                    isDirectory = f.isDirectory,
                    sizeBytes = if (f.isFile) f.length() else 0L,
                    modifiedMillis = f.lastModified()
                )
            }
            .sortedWith(compareByDescending<FileEntry> { it.isDirectory }.thenBy { it.name.lowercase(Locale.getDefault()) })
        Result.success(entries)
    } catch (e: SecurityException) {
        Result.failure(Exception("Cannot read this folder"))
    } catch (e: Exception) {
        Result.failure(Exception(e.message ?: "Cannot read this folder"))
    }
}

fun formatSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format(Locale.getDefault(), "%.1f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format(Locale.getDefault(), "%.1f MB", mb)
    val gb = mb / 1024.0
    return String.format(Locale.getDefault(), "%.1f GB", gb)
}

fun formatDate(millis: Long): String {
    if (millis <= 0) return ""
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}
