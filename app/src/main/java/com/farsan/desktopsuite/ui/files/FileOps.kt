package com.farsan.desktopsuite.ui.files

import java.io.File
import java.io.IOException

fun validateFolderName(name: String): String? {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return "Name cannot be empty"
    if (trimmed.contains('/') || trimmed.contains('\\') || trimmed.contains('\u0000')) return "Invalid name"
    if (trimmed == "." || trimmed == "..") return "Invalid name"
    return null
}

fun deleteItem(path: String): Result<Unit> {
    val file = File(path)
    if (!file.exists()) return Result.failure(IllegalStateException("Does not exist"))
    if (file.isDirectory) {
        val children = file.listFiles()
        if (children != null && children.isNotEmpty()) {
            return Result.failure(IllegalStateException("Folder not empty"))
        }
    }
    return try {
        if (file.delete()) Result.success(Unit)
        else Result.failure(IOException("Delete failed"))
    } catch (e: SecurityException) {
        Result.failure(e)
    } catch (e: IOException) {
        Result.failure(e)
    }
}

fun renameItem(path: String, newName: String): Result<Unit> {
    val error = validateFolderName(newName)
    if (error != null) return Result.failure(IllegalArgumentException(error))
    val file = File(path)
    if (!file.exists()) return Result.failure(IllegalStateException("Does not exist"))
    val target = File(file.parentFile, newName.trim())
    if (target.exists()) return Result.failure(IllegalStateException("Already exists"))
    return try {
        if (file.renameTo(target)) Result.success(Unit)
        else Result.failure(IOException("Rename failed"))
    } catch (e: SecurityException) {
        Result.failure(e)
    } catch (e: IOException) {
        Result.failure(e)
    }
}

fun extractApk(apkPath: String): Result<String> {
    val apk = File(apkPath)
    if (!apk.exists() || !apk.isFile) return Result.failure(IllegalStateException("APK not found"))
    val destName = apk.nameWithoutExtension + "_extracted"
    val dest = File(apk.parentFile, destName)
    if (dest.exists()) {
        // reuse if exists? Reject to avoid overwrite
        return Result.failure(IllegalStateException("Destination already exists: ${dest.absolutePath}"))
    }
    dest.mkdirs()
    return try {
        java.util.zip.ZipInputStream(java.io.FileInputStream(apk)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val outPath = File(dest, entry.name)
                // path traversal guard
                val canonicalDest = dest.canonicalPath
                val canonicalOut = outPath.canonicalPath
                if (!canonicalOut.startsWith(canonicalDest + File.separator)) {
                    return Result.failure(SecurityException("Unsafe entry: ${entry.name}"))
                }
                if (entry.isDirectory) {
                    outPath.mkdirs()
                } else {
                    outPath.parentFile?.mkdirs()
                    java.io.FileOutputStream(outPath).use { fos ->
                        val buf = ByteArray(8192)
                        var n: Int
                        while (zis.read(buf).also { n = it } > 0) fos.write(buf, 0, n)
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        Result.success(dest.absolutePath)
    } catch (e: Exception) {
        // cleanup partial
        dest.deleteRecursively()
        Result.failure(e)
    }
}

    val error = validateFolderName(name)
    if (error != null) return Result.failure(IllegalArgumentException(error))
    val parent = File(parentPath)
    if (!parent.exists() || !parent.isDirectory) return Result.failure(IllegalStateException("Parent not found: $parentPath"))
    if (!parent.canWrite()) return Result.failure(IllegalStateException("Directory not writable"))
    val target = File(parent, name.trim())
    if (target.exists()) return Result.failure(IllegalStateException("Already exists"))
    return try {
        val ok = target.mkdir()
        if (ok) Result.success(Unit)
        else {
            // retry once with mkdirs
            if (target.mkdirs()) Result.success(Unit)
            else Result.failure(IOException("Could not create folder: ${target.absolutePath} (parent writable=${parent.canWrite()})"))
        }
    } catch (e: SecurityException) {
        Result.failure(IOException("Permission denied: ${e.message}", e))
    } catch (e: IOException) {
        Result.failure(e)
    }
}
