package com.psyfen.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.psyfen.common.Resource
import com.psyfen.common.firebase.await
import com.psyfen.domain.model.FileItem
import com.psyfen.domain.respository.FileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID
import javax.inject.Inject

class FileRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
): FileRepository {

    companion object {
        private const val COLLECTION_FILES = "files"
        private const val STORAGE_PATH = "user_files"
    }

    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val userId: String? = prefs.getString("user_id", null)
    val username: String? = prefs.getString("username", "Unknown")

    override suspend fun uploadFile(
        uri: Uri,
        fileName: String,
        isPublic: Boolean
    ): Resource<FileItem> {
        return try {
            if(userId.isNullOrEmpty() || username.isNullOrEmpty()) {
                Log.e("FileRepository", "User not logged in")
                return Resource.Failure(Exception("User not Logged In"))
            }

            Log.d("FileRepository", "Starting upload for user: $userId")

            // Generate unique file name
            val uniqueFileName = "${UUID.randomUUID()}_$fileName"
            val storageRef = storage.reference
                .child(STORAGE_PATH)
                .child(userId)
                .child(uniqueFileName)

            Log.d("FileRepository", "Uploading to: ${storageRef.path}")

            val uploadTask = storageRef.putFile(uri).await()
            val downloadUrl = storageRef.downloadUrl.await()

            val fileSize = uploadTask.metadata?.sizeBytes ?: 0L
            val fileType = context.contentResolver.getType(uri) ?: "unknown"

            Log.d("FileRepository", "Upload complete, creating Firestore entry")

            val fileItem = FileItem(
                fileName = fileName,
                fileUrl = downloadUrl.toString(),
                fileType = fileType,
                fileSize = fileSize,
                isPublic = isPublic,
                ownerId = userId,
                ownerName = username,
                uploadedAt = System.currentTimeMillis()
            )

            // Save to Firestore
            val docRef = firestore.collection(COLLECTION_FILES)
                .add(fileItem)
                .await()

            Log.d("FileRepository", "File saved with ID: ${docRef.id}")
            Resource.Success(fileItem.copy(id = docRef.id))
        } catch (e: Exception) {
            Log.e("FileRepository", "Upload failed", e)
            Resource.Failure(e)
        }
    }

    override suspend fun getMyFiles(): Flow<Resource<List<FileItem>>> = callbackFlow {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getString("user_id", null)

        if (userId == null) {
            trySend(Resource.Failure(Exception("User not logged in")))
            close()
            return@callbackFlow
        }

        Log.d("FileRepository", "Listening for files of user: $userId")

        val listener = firestore.collection(COLLECTION_FILES)
            .whereEqualTo("ownerId", userId)
            .orderBy("uploadedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FileRepository", "Error loading my files", error)
                    trySend(Resource.Failure(error))
                    return@addSnapshotListener
                }

                val files = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FileItem::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                Log.d("FileRepository", "My files updated: ${files.size} files")
                trySend(Resource.Success(files))
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getPublicFiles(): Flow<Resource<List<FileItem>>> = callbackFlow {
        Log.d("FileRepository", "Listening for public files")

        val listener = firestore.collection(COLLECTION_FILES)
            .whereEqualTo("isPublic", true)
            .orderBy("uploadedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FileRepository", "Error loading public files", error)
                    trySend(Resource.Failure(error))
                    return@addSnapshotListener
                }

                val files = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FileItem::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                Log.d("FileRepository", "Public files updated: ${files.size} files")
                trySend(Resource.Success(files))
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getSharedFiles(): Flow<Resource<List<FileItem>>> = callbackFlow {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getString("user_id", null)

        if (userId == null) {
            trySend(Resource.Failure(Exception("User not logged in")))
            close()
            return@callbackFlow
        }

        val listener = firestore.collection(COLLECTION_FILES)
            .whereArrayContains("sharedWith", userId)
            .orderBy("uploadedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Failure(error))
                    return@addSnapshotListener
                }

                val files = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FileItem::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(Resource.Success(files))
            }

        awaitClose { listener.remove() }
    }

    override suspend fun deleteFile(fileId: String): Resource<Unit> {
        return try {
            Log.d("FileRepository", "Deleting file: $fileId")

            val doc = firestore.collection(COLLECTION_FILES)
                .document(fileId)
                .get()
                .await()

            val fileItem = doc.toObject(FileItem::class.java)
                ?: return Resource.Failure(Exception("File not found"))

            // Delete from Storage
            val storageRef = storage.getReferenceFromUrl(fileItem.fileUrl)
            storageRef.delete().await()

            // Delete from Firestore
            firestore.collection(COLLECTION_FILES)
                .document(fileId)
                .delete()
                .await()

            Log.d("FileRepository", "File deleted successfully")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("FileRepository", "Delete failed", e)
            Resource.Failure(e)
        }
    }

    override suspend fun shareFileWithUser(
        fileId: String,
        rawUserIdentifier: String
    ): Resource<Unit> {
        return try {

            val userIdentifier = rawUserIdentifier.filterNot { it.isWhitespace() }
            Log.d("FileRepository", "Searching for user: $userIdentifier")


            // Find user by username or phone number
            val userSnapshot = firestore.collection("users")
                .whereEqualTo("username", userIdentifier)
                .get()
                .await()

            val targetUserId = if (userSnapshot.isEmpty) {
                Log.d("FileRepository", "User not found by username, trying phone number")
                // Try phone number
                val phoneSnapshot = firestore.collection("users")
                    .whereEqualTo("phoneNumber", userIdentifier)
                    .get()
                    .await()

                if (phoneSnapshot.isEmpty) {
                    Log.e("FileRepository", "User not found: $userIdentifier")
                    return Resource.Failure(Exception("User not found"))
                }
                phoneSnapshot.documents.first().id
            } else {
                userSnapshot.documents.first().id
            }

            Log.d("FileRepository", "Found user ID: $targetUserId, sharing file: $fileId")

            // Update file's sharedWith array
            firestore.collection(COLLECTION_FILES)
                .document(fileId)
                .update(
                    "sharedWith",
                    com.google.firebase.firestore.FieldValue.arrayUnion(targetUserId)
                )
                .await()

            Log.d("FileRepository", "File shared successfully")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("FileRepository", "Error sharing file", e)
            Resource.Failure(e)
        }
    }

    override suspend fun downloadFile(fileItem: FileItem): Resource<Uri> {
        return try {
            // For simplicity, return the download URL
            Resource.Success(Uri.parse(fileItem.fileUrl))
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }
}