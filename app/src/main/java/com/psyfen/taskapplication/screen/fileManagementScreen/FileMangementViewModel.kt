package com.psyfen.taskapplication.com.psyfen.taskapplication.screen.fileManagementScreen


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.psyfen.common.AppViewModel
import com.psyfen.common.Resource
import com.psyfen.domain.model.FileItem
import com.psyfen.domain.use_cases.DeleteFileUseCase
import com.psyfen.domain.use_cases.GetMyFilesUseCase
import com.psyfen.domain.use_cases.GetPublicFilesUseCase
import com.psyfen.domain.use_cases.ShareFileUseCase
import com.psyfen.domain.use_cases.UploadFileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UploadState(
    val selectedUri: Uri? = null,
    val fileName: String = "",
    val isPublic: Boolean = false,
    val isUploading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FileManagementViewModel @Inject constructor(
    private val uploadFileUseCase: UploadFileUseCase,
    private val getMyFilesUseCase: GetMyFilesUseCase,
    private val getPublicFilesUseCase: GetPublicFilesUseCase,
    private val deleteFileUseCase: DeleteFileUseCase,
    private val shareFileUseCase: ShareFileUseCase,
    @ApplicationContext private val context: Context
) : AppViewModel() {

    private val _myFilesState = MutableStateFlow<Resource<List<FileItem>>>(Resource.Loading)
    val myFilesState: StateFlow<Resource<List<FileItem>>> = _myFilesState.asStateFlow()

    private val _publicFilesState = MutableStateFlow<Resource<List<FileItem>>>(Resource.Loading)
    val publicFilesState: StateFlow<Resource<List<FileItem>>> = _publicFilesState.asStateFlow()

    private val _uploadState = MutableStateFlow(UploadState())
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    init {
        Log.d("FileViewModel", "ViewModel initialized")
        loadFiles()
    }

    private fun loadFiles() {
        Log.d("FileViewModel", "Loading files")
        loadMyFiles()
        loadPublicFiles()
    }

    private fun loadMyFiles() {
        viewModelScope.launch {
            Log.d("FileViewModel", "Starting to load my files")
            getMyFilesUseCase().collect { resource ->
                Log.d("FileViewModel", "My files resource: ${resource::class.simpleName}")
                _myFilesState.value = resource
                if (resource is Resource.Success) {
                    Log.d("FileViewModel", "My files loaded: ${resource.result.size} files")
                } else if (resource is Resource.Failure) {
                    Log.e("FileViewModel", "Error loading my files", resource.exception)
                }
            }
        }
    }

    private fun loadPublicFiles() {
        viewModelScope.launch {
            Log.d("FileViewModel", "Starting to load public files")
            getPublicFilesUseCase().collect { resource ->
                Log.d("FileViewModel", "Public files resource: ${resource::class.simpleName}")
                _publicFilesState.value = resource
                if (resource is Resource.Success) {
                    Log.d("FileViewModel", "Public files loaded: ${resource.result.size} files")
                } else if (resource is Resource.Failure) {
                    Log.e("FileViewModel", "Error loading public files", resource.exception)
                }
            }
        }
    }

    fun setSelectedFile(uri: Uri) {
        val fileName = getFileName(uri)
        Log.d("FileViewModel", "File selected: $fileName, URI: $uri")
        _uploadState.value = _uploadState.value.copy(
            selectedUri = uri,
            fileName = fileName,
            error = null
        )
    }

    fun updateFileName(name: String) {
        Log.d("FileViewModel", "Filename updated: $name")
        _uploadState.value = _uploadState.value.copy(fileName = name)
    }

    fun updateIsPublic(isPublic: Boolean) {
        Log.d("FileViewModel", "IsPublic updated: $isPublic")
        _uploadState.value = _uploadState.value.copy(isPublic = isPublic)
    }

    fun uploadFile() {
        val state = _uploadState.value
        val uri = state.selectedUri

        Log.d("FileViewModel", "Upload initiated")
        Log.d("FileViewModel", "URI: $uri")
        Log.d("FileViewModel", "Filename: ${state.fileName}")
        Log.d("FileViewModel", "IsPublic: ${state.isPublic}")

        if (uri == null) {
            Log.e("FileViewModel", "Upload failed: URI is null")
            _uploadState.value = state.copy(error = "No file selected")
            return
        }

        if (state.fileName.isBlank()) {
            Log.e("FileViewModel", "Upload failed: Filename is blank")
            _uploadState.value = state.copy(error = "File name cannot be empty")
            return
        }

        viewModelScope.launch {
            _uploadState.value = state.copy(isUploading = true, error = null)
            Log.d("FileViewModel", "Starting upload to Firebase...")

            when (val result = uploadFileUseCase(uri, state.fileName, state.isPublic)) {
                is Resource.Success -> {
                    Log.d("FileViewModel", "Upload successful! File ID: ${result.result.id}")
                    _uploadState.value = UploadState() // Reset
                }
                is Resource.Failure -> {
                    Log.e("FileViewModel", "Upload failed", result.exception)
                    _uploadState.value = state.copy(
                        isUploading = false,
                        error = result.exception.message ?: "Upload failed"
                    )
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun deleteFile(fileId: String) {
        Log.d("FileViewModel", "Deleting file: $fileId")
        viewModelScope.launch {
            when (val result = deleteFileUseCase(fileId)) {
                is Resource.Success -> {
                    Log.d("FileViewModel", "File deleted successfully")
                }
                is Resource.Failure -> {
                    Log.e("FileViewModel", "Delete failed", result.exception)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun shareFile(fileId: String, userIdentifier: String) {
        Log.d("FileViewModel", "Sharing file $fileId with $userIdentifier")
        viewModelScope.launch {
            when (val result = shareFileUseCase(fileId, userIdentifier)) {
                is Resource.Success -> {
                    Log.d("FileViewModel", "File shared successfully")
                }
                is Resource.Failure -> {
                    Log.e("FileViewModel", "Share failed", result.exception)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun downloadFile(file: FileItem) {
        Log.d("FileViewModel", "Opening file: ${file.fileName}")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(file.fileUrl))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun clearUpload() {
        Log.d("FileViewModel", "Clearing upload state")
        _uploadState.value = UploadState()
    }

    fun refreshFiles() {
        Log.d("FileViewModel", "Refreshing files")
        loadFiles()
    }

    private fun getFileName(uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) it.getString(nameIndex) else "file"
            } else "file"
        } ?: "file"
    }
}