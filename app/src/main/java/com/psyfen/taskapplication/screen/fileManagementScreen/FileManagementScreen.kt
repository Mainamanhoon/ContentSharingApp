package com.psyfen.taskapplication.com.psyfen.taskapplication.screen.fileManagementScreen


import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
 import androidx.compose.material.icons.Icons
 import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.psyfen.common.Resource
import com.psyfen.domain.model.FileItem
import com.psyfen.taskapplication.R
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagementScreen(
    viewModel: FileManagementViewModel = hiltViewModel()
) {
    val myFilesState by viewModel.myFilesState.collectAsState()
    val publicFilesState by viewModel.publicFilesState.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf<FileItem?>(null) }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        Log.d("FileManagement", "File picker returned: $uri")
        if (uri != null) {
            viewModel.setSelectedFile(uri)
            showUploadDialog = true
        } else {
            Log.d("FileManagement", "No file selected")
        }
    }

    // Show upload dialog when file is selected
    LaunchedEffect(uploadState.selectedUri) {
        if (uploadState.selectedUri != null && !uploadState.isUploading) {
            Log.d("FileManagement", "File selected, showing dialog: ${uploadState.fileName}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "File Management",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF101322),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    Log.d("FileManagement", "FAB clicked, launching file picker")
                    filePickerLauncher.launch("*/*")
                },
                containerColor = Color(0xFFfd511e)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Upload File",
                    tint = Color.White
                )
            }
        },
        containerColor = Color(0xFF101322)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1c213c),
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFFfd511e)
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("My Files") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Public Files") }
                )
            }

            // Content
            when (selectedTab) {
                0 -> {
                    when (val state = myFilesState) {
                        is Resource.Loading -> LoadingView()
                        is Resource.Success -> {
                            if (state.result.isEmpty()) {
                                EmptyFilesView("No files uploaded yet")
                            } else {
                                FilesList(
                                    files = state.result,
                                    isOwner = true,
                                    onDelete = { viewModel.deleteFile(it.id) },
                                    onShare = { showShareDialog = it },
                                    onDownload = { viewModel.downloadFile(it) }
                                )
                            }
                        }
                        is Resource.Failure -> ErrorView(
                            message = state.exception.message ?: "Unknown error",
                            onRetry = { viewModel.refreshFiles() }
                        )
                    }
                }
                1 -> {
                    when (val state = publicFilesState) {
                        is Resource.Loading -> LoadingView()
                        is Resource.Success -> {
                            if (state.result.isEmpty()) {
                                EmptyFilesView("No public files available")
                            } else {
                                FilesList(
                                    files = state.result,
                                    isOwner = false,
                                    onDelete = {},
                                    onShare = {},
                                    onDownload = { viewModel.downloadFile(it) }
                                )
                            }
                        }
                        is Resource.Failure -> ErrorView(
                            message = state.exception.message ?: "Unknown error",
                            onRetry = { viewModel.refreshFiles() }
                        )
                    }
                }
            }
        }

        // Upload Dialog
        if (showUploadDialog && uploadState.selectedUri != null) {
            UploadDialog(
                fileName = uploadState.fileName,
                isPublic = uploadState.isPublic,
                isUploading = uploadState.isUploading,
                error = uploadState.error,
                onFileNameChange = { viewModel.updateFileName(it) },
                onPublicChange = { viewModel.updateIsPublic(it) },
                onUpload = {
                    Log.d("FileManagement", "Upload button clicked")
                    viewModel.uploadFile()
                },
                onDismiss = {
                    showUploadDialog = false
                    viewModel.clearUpload()
                }
            )
        }

        // Share Dialog
        showShareDialog?.let { file ->
            ShareFileDialog(
                file = file,
                onShare = { phoneNumber ->
                    viewModel.shareFile(file.id, phoneNumber)
                    showShareDialog = null
                },
                onDismiss = { showShareDialog = null }
            )
        }
    }
}

// ... (Keep all the other composables: FilesList, FileItemCard, UploadDialog, etc. - same as before)

@Composable
fun UploadDialog(
    fileName: String,
    isPublic: Boolean,
    isUploading: Boolean,
    error: String?,
    onFileNameChange: (String) -> Unit,
    onPublicChange: (Boolean) -> Unit,
    onUpload: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        title = { Text("Upload File") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = fileName,
                    onValueChange = onFileNameChange,
                    label = { Text("File Name") },
                    enabled = !isUploading,
                    modifier = Modifier.fillMaxWidth(),
                    isError = fileName.isBlank()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isPublic,
                        onCheckedChange = onPublicChange,
                        enabled = !isUploading
                    )
                    Text("Make file public")
                }

                if (error != null) {
                    Text(
                        text = "Error: $error",
                        color = Color(0xFFFF3333),
                        fontSize = 14.sp
                    )
                }

                if (isUploading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFfd511e)
                    )
                    Text(
                        text = "Uploading...",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onUpload,
                enabled = !isUploading && fileName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFfd511e)
                )
            ) {
                Text(if (isUploading) "Uploading..." else "Upload")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isUploading
            ) {
                Text("Cancel")
            }
        }
    )
}




@Composable
fun FilesList(
    files: List<FileItem>,
    isOwner: Boolean,
    onDelete: (FileItem) -> Unit,
    onShare: (FileItem) -> Unit,
    onDownload: (FileItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(files) { file ->
            FileItemCard(
                file = file,
                isOwner = isOwner,
                onDelete = { onDelete(file) },
                onShare = { onShare(file) },
                onDownload = { onDownload(file) }
            )
        }
    }
}

@Composable
fun FileItemCard(
    file: FileItem,
    isOwner: Boolean,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1c213c)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File Icon
            Icon(
                imageVector = getFileIcon(file.fileType),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = Color(0xFFfd511e)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // File Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = file.fileName,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatFileSize(file.fileSize),
                        color = Color(0xFFC6CBC5),
                        fontSize = 12.sp
                    )

                    Text(
                        text = "•",
                        color = Color(0xFFC6CBC5),
                        fontSize = 12.sp
                    )

                    Text(
                        text = formatDate(file.uploadedAt),
                        color = Color(0xFFC6CBC5),
                        fontSize = 12.sp
                    )
                }

                if (!isOwner) {
                    Text(
                        text = "By: ${file.ownerName}",
                        color = Color(0xFFC6CBC5),
                        fontSize = 12.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }

            // Actions Menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = Color.White
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Download") },
                        onClick = {
                            onDownload()
                            showMenu = false
                        },
                        leadingIcon = {

                            Icon( ImageVector.vectorResource(R.drawable.outline_download_24), contentDescription = null)
                        }
                    )

                    if (isOwner) {
                        DropdownMenuItem(
                            text = { Text("Share") },
                            onClick = {
                                onShare()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Share, contentDescription = null)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Delete", color = Color(0xFFFF3333)) },
                            onClick = {
                                onDelete()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFFF3333)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UploadDialog(
    fileName: String,
    isPublic: Boolean,
    isUploading: Boolean,
    onFileNameChange: (String) -> Unit,
    onPublicChange: (Boolean) -> Unit,
    onUpload: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        title = { Text("Upload File") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = fileName,
                    onValueChange = onFileNameChange,
                    label = { Text("File Name") },
                    enabled = !isUploading,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isPublic,
                        onCheckedChange = onPublicChange,
                        enabled = !isUploading
                    )
                    Text("Make file public")
                }

                if (isUploading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFfd511e)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onUpload,
                enabled = !isUploading && fileName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFfd511e)
                )
            ) {
                Text(if (isUploading) "Uploading..." else "Upload")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isUploading
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ShareFileDialog(
    file: FileItem,
    onShare: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share File") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Share '${file.fileName}' with:")

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    placeholder = { Text("+91 98765 43210") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onShare(phoneNumber) },
                enabled = phoneNumber.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFfd511e)
                )
            ) {
                Text("Share")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFFfd511e)
        )
    }
}

@Composable
fun EmptyFilesView(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector =  ImageVector.vectorResource(R.drawable.outline_folder_open_24),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFFC6CBC5)
            )
            Text(
                text = message,
                color = Color(0xFFC6CBC5),
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Error: $message",
                color = Color(0xFFFF3333),
                fontSize = 16.sp
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFfd511e)
                )
            ) {
                Text("Retry")
            }
        }
    }
}

// Helper Functions
@Composable
private fun getFileIcon(fileType: String) = when {
    fileType.startsWith("image/") -> ImageVector.vectorResource( R.drawable.baseline_image_24)
    fileType.startsWith("video/") -> ImageVector.vectorResource(R.drawable.outline_video_library_24)
    fileType.startsWith("audio/") -> ImageVector.vectorResource(R.drawable.outline_audio_file_24)
    fileType == "application/pdf" ->  ImageVector.vectorResource(R.drawable.outline_picture_as_pdf_24)
    else -> ImageVector.vectorResource(R.drawable.baseline_insert_drive_file_24)
}

private fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "%.2f KB".format(kb)
    val mb = kb / 1024.0
    if (mb < 1024) return "%.2f MB".format(mb)
    val gb = mb / 1024.0
    return "%.2f GB".format(gb)
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}