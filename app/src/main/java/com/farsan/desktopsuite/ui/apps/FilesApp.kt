package com.farsan.desktopsuite.ui.apps

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.contextMenu
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ripple.rippleColor
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.handleSelection
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Dialog
import androidx.compose.ui.dialog.DialogProperties
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.farsan.desktopsuite.ui.files.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun shortPath(p: String, max: Int = 40): String =
    if (p.length <= max) p else "..." + p.takeLast(max - 3)

private val windows11NeutralLight = Color(0xFFF8F9FA)
private val windows11NeutralDark = Color(0xFF212529)
private val windows11AccentBlue = Color(0xFF0D6EFD)
private val windows11SubtleGray = Color(0xFF6C757D)
private val windows11CardSurface = Color(0xFFFFFFFF)

@Composable
fun FilesApp(path: String, onPathChange: (String) -> Unit) {
    val context = LocalContext.current
    var hasAccess by remember { mutableStateOf(hasStorageAccess(context)) }
    var denied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var entries by remember { mutableStateOf<List<FileEntry>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var newFolderPath by remember { mutableStateOf<String?>(null) }
    var renamePath by remember { mutableStateOf<FileEntry?>(null) }
    var deleteConfirmed by remember { mutableStateOf(false) }
    var refreshCounter by remember { mutableStateOf(0) }
    var renameText by remember { mutableStateOf("") }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasAccess = hasStorageAccess(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
            Text(
                text = "This lets the Files app show the folders on your phone. Nothing is uploaded.",
                color = Color(0xFFB0B0C0), fontSize = 10.sp
            )
            Button(onClick = {
                if (Build.VERSION.SDK_INT >= 30) {
                    openAllFilesAccessSettings(context)
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    permissionsLauncher.launch(
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    )
                }
            }, modifier = Modifier.padding(top = 8.dp)) {
                Text("Allow access")
            }
            if (denied) {
                Text(
                    text = "Access was not granted.",
                    color = Color(0xFFFF6B6B), fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        return
    }

    LaunchedEffect(refreshCounter) {
        error = null
        val res = listDirectory(path)
        res.onSuccess { entries = it }.onFailure { error = it.message }
    }

    // Top app bar
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = {
            if (!isRoot) {
                val parent = File(path).parentFile?.absolutePath
                if (parent != null && parent.startsWith(root)) {
                    onPathChange(parent)
                }
            }
        }) {
            Text("Up", contentDescription = "Up one level")
        }
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Search") },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .roundedCornerShape(8.dp)
                .padding(horizontal = 8.dp),
            singleLine = true
        )
        Spacer(Modifier.width(8.dp))
        Button(onClick = {
            newFolderPath = path
        }) {
            Text("New folder", contentDescription = "Create new folder")
        }
    }

    // New folder dialog
    var newFolderName by remember { mutableStateOf("") }
    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .visible(newFolderPath != null)
            .pointerInput(Unit) {
                detectTapGestures(onClick = { newFolderPath = null })
            }
    ) {
        if (newFolderPath != null) {
            AlertDialog(
                onDismissRequest = {},
                properties = DialogProperties(Alignment.Center)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        label = { Text("Folder name") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(onClick = { newFolderPath = null; newFolderName = "" }) {
                            Text("Cancel", contentDescription = "Cancel")
                        }
                        Button(
                            onClick = {
                                val result = FileOps.createFolder(newFolderPath!!, newFolderName)
                                when (result) {
                                    is Result.Success -> {
                                        refreshCounter++
                                        newFolderPath = null
                                        newFolderName = ""
                                        Toast.makeText(context, "Folder created", Toast.LENGTH_SHORT).show()
                                    }
                                    is Result.Failure -> {
                                        error = result.exception?.message ?: "Could not create folder"
                                        refreshCounter++
                                        newFolderPath = null
                                        newFolderName = ""
                                    }
                                }
                            }
                        ) {
                            Text("Create", contentDescription = "Create folder")
                        }
                    }
                }
            }
        }
    }

    // File list
    if (entries == null && error == null) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp))
    } else if (error != null) {
        Text(text = error ?: "", color = Color(0xFFFF6B6B), fontSize = 11.sp)
    } else {
        val list = entries ?: emptyList()
        if (list.isEmpty()) {
            Text("This folder is empty", color = Color(0xFFB0B0C0), fontSize = 11.sp)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalScroll = rememberScrollState(),
                contentPadding = PaddingValues(8.dp, 4.dp, 8.dp, 4.dp),
                Arrangement.spacedBy(4.dp)
            ) {
                items(list) { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .clickable(enabled = entry.isDirectory) {
                                if (entry.isDirectory) onPathChange(entry.path)
                            }
                            .semantics { selectable = entry.isDirectory }
                    ) {
                        Text(
                            text = if (entry.isDirectory) "[DIR]" else "[FILE]",
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.width(56.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entry.name, color = Color.White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (!entry.isDirectory) {
                                Text(
                                    "${formatSize(entry.sizeBytes)}  ${formatDate(entry.modifiedMillis)}",
                                    color = Color(0xFFB0B0C0), fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
