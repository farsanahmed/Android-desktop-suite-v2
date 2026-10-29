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

fun createFolder(parentPath: String, name: String): Result<Unit> {
    val error = validateFolderName(name)
    if (error != null) return Result.failure(IllegalArgumentException(error))
    val parent = File(parentPath)
    if (!parent.exists() || !parent.isDirectory) return Result.failure(IllegalStateException("Parent not found"))
    val target = File(parent, name.trim())
    if (target.exists()) return Result.failure(IllegalStateException("Already exists"))
    return try {
        if (target.mkdirs()) Result.success(Unit)
        else Result.failure(IOException("Could not create folder"))
    } catch (e: SecurityException) {
        Result.failure(e)
    } catch (e: IOException) {
        Result.failure(e)
    }
}
