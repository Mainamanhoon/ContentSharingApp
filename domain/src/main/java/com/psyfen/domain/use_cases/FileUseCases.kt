package com.psyfen.domain.use_cases


import android.net.Uri
import com.psyfen.common.Resource
import com.psyfen.domain.model.FileItem
import com.psyfen.domain.respository.FileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UploadFileUseCase @Inject constructor(
    private val repository: FileRepository
) {
    suspend operator fun invoke(uri: Uri, fileName: String, isPublic: Boolean): Resource<FileItem> {
        return repository.uploadFile(uri, fileName, isPublic)
    }
}

class GetMyFilesUseCase @Inject constructor(
    private val repository: FileRepository
) {
    suspend operator fun invoke(): Flow<Resource<List<FileItem>>> {
        return repository.getMyFiles()
    }
}

class GetPublicFilesUseCase @Inject constructor(
    private val repository: FileRepository
) {
    suspend operator fun invoke(): Flow<Resource<List<FileItem>>> {
        return repository.getPublicFiles()
    }
}

class DeleteFileUseCase @Inject constructor(
    private val repository: FileRepository
) {
    suspend operator fun invoke(fileId: String): Resource<Unit> {
        return repository.deleteFile(fileId)
    }
}

class ShareFileUseCase @Inject constructor(
    private val repository: FileRepository
) {
    suspend operator fun invoke(fileId: String, userIdentifier: String): Resource<Unit> {
        return repository.shareFileWithUser(fileId, userIdentifier)
    }
}