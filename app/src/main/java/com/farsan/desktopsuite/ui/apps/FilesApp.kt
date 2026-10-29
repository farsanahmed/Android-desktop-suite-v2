package com.farsan.desktopsuite.ui.apps

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.farsan.desktopsuite.ui.files.*
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun FilesApp(path: String, onPathChange: (String) -> Unit) {
    val context = LocalContext.current
    var hasAccess by remember { mutableStateOf(hasStorageAccess(context)) }
    var denied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var entries by remember { mutableStateOf<List<FileEntry>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var showNewFolder by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf<String?>(null) }
    var reload by remember { mutableStateOf(0) }
    var showRename by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<String?>(null) }
    var renameName by remember { mutableStateOf("") }
    var renameFeedback by remember { mutableStateOf<String?>(null) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        hasAccess = hasStorageAccess(context)
    }

    val root = Environment.getExternalStorageDirectory().absolutePath
    val isRoot = path == root

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasAccess = hasStorageAccess(context)
        denied = !granted
    }
    val permissionsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        hasAccess = hasStorageAccess(context)
        denied = result.values.any { !it }
    }

    if (!hasAccess) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Allow Android Desktop Suite to access your files?", color = Color.White, fontSize = 12.sp)
            Text("This lets the Files app show the folders on your phone. Nothing is uploaded.", color = Color(0xFFB0B0C0), fontSize = 10.sp)
            Button(onClick = {
                if (Build.VERSION.SDK_INT >= 30) {
                    openAllFilesAccessSettings(context)
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    permissionsLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                }
            }, modifier = Modifier.padding(top = 8.dp)) {
                Text("Allow access")
            }
            if (denied) {
                Text("Access was not granted.", color = Color(0xFFFF6B6B), fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }
        return
    }

    LaunchedEffect(path, reload) {
        entries = null
        error = null
        val res = listDirectory(path)
        res.onSuccess { entries = it }.onFailure { error = it.message }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = {
                if (!isRoot) {
                    val parent = File(path).parentFile?.absolutePath
                    if (parent != null && parent.startsWith(root)) {
                        onPathChange(parent)
                    }
                }
            }, enabled = !isRoot) {
                Text("Up")
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { showNewFolder = true }) {
                Text("New folder")
            }
            Text(
                text = path,
                color = Color.White,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        if (entries == null && error == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp))
        } else if (error != null) {
            Text(text = error ?: "", color = Color(0xFFFF6B6B), fontSize = 11.sp)
        } else {
            val list = entries ?: emptyList()
            if (list.isEmpty()) {
                Text("This folder is empty", color = Color(0xFFB0B0C0), fontSize = 11.sp)
            } else {
                LazyColumn {
                    items(list) { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = entry.isDirectory) {
                                    if (entry.isDirectory) onPathChange(entry.path)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (entry.isDirectory) "[DIR]" else "[FILE]",
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.width(60.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.name, color = Color.White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (!entry.isDirectory) {
                                    Text("${formatSize(entry.sizeBytes)}  ${formatDate(entry.modifiedMillis)}", color = Color(0xFFB0B0C0), fontSize = 10.sp)
                                }
                            }
                            Button(onClick = {
                                renameTarget = entry.path
                                renameName = entry.name
                                renameFeedback = null
                                showRename = true
                            }) { Text("Rename") }
                        }
                    }
                }
            }
        }
    }

    if (showNewFolder) {
        AlertDialog(
            onDismissRequest = { showNewFolder = false },
            title = { Text("New folder") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        label = { Text("Folder name") },
                        singleLine = true
                    )
                    feedback?.let { Text(it, color = Color(0xFFFF6B6B), fontSize = 10.sp) }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val err = validateFolderName(newFolderName)
                    if (err != null) {
                        feedback = err
                        return@Button
                    }
                    val res = createFolder(path, newFolderName)
                    res.onSuccess {
                        feedback = null
                        showNewFolder = false
                        newFolderName = ""
                        reload++
                        entries = null
                        // trigger reload by re-running LaunchedEffect? Use direct
                    }.onFailure {
                        feedback = it.message ?: "Failed"
                    }
                }) { Text("Create") }
            },
            dismissButton = {
                Button(onClick = { showNewFolder = false; feedback = null; newFolderName = "" }) { Text("Cancel") }
            }
        )
    }

    if (showRename) {
        AlertDialog(
            onDismissRequest = { showRename = false },
            title = { Text("Rename") },
            text = {
                Column {
                    OutlinedTextField(
                        value = renameName,
                        onValueChange = { renameName = it },
                        label = { Text("New name") },
                        singleLine = true
                    )
                    renameFeedback?.let { Text(it, color = Color(0xFFFF6B6B), fontSize = 10.sp) }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val target = renameTarget
                    if (target == null) { showRename = false; return@Button }
                    val err = validateFolderName(renameName)
                    if (err != null) { renameFeedback = err; return@Button }
                    val res = renameItem(target, renameName)
                    res.onSuccess {
                        renameFeedback = null
                        showRename = false
                        renameTarget = null
                        renameName = ""
                        reload++
                    }.onFailure {
                        renameFeedback = it.message ?: "Failed"
                    }
                }) { Text("Rename") }
            },
            dismissButton = {
                Button(onClick = { showRename = false; renameFeedback = null; renameName = ""; renameTarget = null }) { Text("Cancel") }
            }
        )
    }
}
