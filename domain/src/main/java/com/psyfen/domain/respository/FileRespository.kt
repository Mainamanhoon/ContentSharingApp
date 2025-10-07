package com.psyfen.domain.respository

import android.net.Uri
import com.psyfen.common.Resource
import com.psyfen.domain.model.FileItem
import kotlinx.coroutines.flow.Flow

interface FileRepository {
    suspend fun uploadFile(uri: Uri, fileName: String, isPublic: Boolean): Resource<FileItem>
    suspend fun getMyFiles(): Flow<Resource<List<FileItem>>>
    suspend fun getPublicFiles(): Flow<Resource<List<FileItem>>>
    suspend fun getSharedFiles(): Flow<Resource<List<FileItem>>>
    suspend fun deleteFile(fileId: String): Resource<Unit>
    suspend fun shareFileWithUser(fileId: String, userIdentifier: String): Resource<Unit>
    suspend fun downloadFile(fileItem: FileItem): Resource<Uri>
}