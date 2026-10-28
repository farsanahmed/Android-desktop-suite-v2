package com.farsan.desktopsuite.ui.files

import android.app.AlertDialog
import android.content.Context
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.AlertDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.dialog.Dialog
import androidx.compose.ui.dialog.DialogProperties
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Result
import kotlinx.coroutines.withContext

fun validateName(name: String): String {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return "Invalid name"
    if (trimmed.contains('/') || trimmed.contains("\0")) return "Invalid name"
    if (trimmed.equals(".", ignoreCase = true) || trimmed.equals("..", ignoreCase = true))
        return "Invalid name"
    return trimmed
}

fun createFolder(parentPath: String, name: String): Result<Unit> {
    val validated = validateName(name)
    if (validated != name) return Result.failure(Exception(validated))
    val root = Environment.getExternalStorageDirectory().absolutePath
    val canonicalRoot = java.io.File(root).canonicalPath
    val target = java.io.File(parentPath).canonicalPath
    if (!canonicalRoot.startsWith(canonicalRoot + java.io.File.separator)) {
        return Result.failure(Exception("Outside storage"))
    }
    val newDir = java.io.File(parentPath, validated)
    if (newDir.exists()) return Result.failure(Exception("Already exists"))
    try {
        if (newDir.mkdirs()) Result.success(Unit) else Result.failure(Exception("Could not create"))
    } catch (e: SecurityException) {
        Result.failure(Exception("Security error"))
    } catch (e: IOException) {
        Result.failure(Exception("IO error"))
    }
}

fun renameItem(path: String, newName: String): Result<Unit> {
    val validated = validateName(newName)
    if (validated != newName) return Result.failure(Exception(validated))
    val root = Environment.getExternalStorageDirectory().absolutePath
    val canonicalRoot = java.io.File(root).canonicalPath
    val canonicalPath = java.io.File(path).canonicalPath
    if (!canonicalPath.startsWith(canonicalRoot + java.io.File.separator)) {
        return Result.failure(Exception("Outside storage"))
    }
    val file = java.io.File(path)
    val newPath = file.parentFile?.path + java.io.File.separator + validated
    if (newPath == null || java.io.File(newPath).exists()) return Result.failure(Exception("Already exists"))
    try {
        if (file.renameTo(java.io.File(newPath))) Result.success(Unit) else Result.failure(Exception("Rename failed"))
    } catch (e: SecurityException) {
        Result.failure(Exception("Security error"))
    } catch (e: IOException) {
        Result.failure(Exception("IO error"))
    }
}

fun deleteItem(path: String): Result<Unit> {
    val root = Environment.getExternalStorageDirectory().absolutePath
    val canonicalRoot = java.io.File(root).canonicalPath
    val canonicalPath = java.io.File(path).canonicalPath
    if (!canonicalPath.startsWith(canonicalRoot + java.io.File.separator)) {
        return Result.failure(Exception("Outside storage"))
    }
    val file = java.io.File(path)
    if (!file.exists()) return Result.failure(Exception("Does not exist"))
    try {
        if (file.isDirectory) {
            val deleted = deleteRecursively(file)
            if (deleted) Result.success(Unit) else Result.failure(Exception("Could not delete folder"))
        } else {
            if (file.delete()) Result.success(Unit) else Result.failure(Exception("Could not delete"))
        }
    } catch (e: SecurityException) {
        Result.failure(Exception("Security error"))
    } catch (e: IOException) {
        Result.failure(Exception("IO error"))
    }
}

fun deleteRecursively(dir: java.io.File): Boolean {
    val result = java.io.File(dir, ".deleted")
    if (dir.delete()) {
        val children = dir.listFiles()
        if (children != null) {
            for (child in children) {
                if (child.isDirectory) deleteRecursively(child)
                else child.delete()
            }
        }
        dir.delete()
        return true
    }
    return false
}
