package com.psyfen.data.repository


import android.content.Context
import android.net.Uri
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
    val userId :String? = prefs.getString("user_id", null)
    val username: String?= prefs.getString("username", "Unknown")

    override suspend fun uploadFile(
        uri: Uri,
        fileName: String,
        isPublic: Boolean
    ): Resource<FileItem> {
        return try {

            if(userId.isNullOrEmpty() || username.isNullOrEmpty()) {
                return Resource.Failure(Exception("User not Logged In"))
            }

            // Generate unique file name
            val uniqueFileName = "${UUID.randomUUID()}_$fileName"
            val storageRef = storage.reference
                .child(STORAGE_PATH)
                .child(userId)
                .child(uniqueFileName)

             val uploadTask = storageRef.putFile(uri).await()
            val downloadUrl = storageRef.downloadUrl.await()

             val fileSize = uploadTask.metadata?.sizeBytes ?: 0L
            val fileType = context.contentResolver.getType(uri) ?: "unknown"

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

            Resource.Success(fileItem.copy(id = docRef.id))
        } catch (e: Exception) {
            e.printStackTrace()
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

        val listener = firestore.collection(COLLECTION_FILES)
            .whereEqualTo("ownerId", userId)
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


    override suspend fun getPublicFiles(): Flow<Resource<List<FileItem>>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_FILES)
            .whereEqualTo("isPublic", true)
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

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }
    override suspend fun shareFileWithUser(
        fileId: String,
        userIdentifier: String
    ): Resource<Unit> {
        return try {
            // Find user by username or phone number
            //TODO once everything works , we might remove shareing using username , seems like bullshit
            val userSnapshot = firestore.collection("users")
                .whereEqualTo("username", userIdentifier)
                .get()


            val targetUserId = if (userSnapshot.result.isEmpty()) {
                // Try phone number
                val phoneSnapshot = firestore.collection("users")
                    .whereEqualTo("phoneNumber", userIdentifier)
                    .get()
                    .await()

                if (phoneSnapshot.isEmpty) {
                    return Resource.Failure(Exception("User not found"))
                }
                phoneSnapshot.documents.first().id
            } else {

                userSnapshot.result.documents.first().id
            }

            // Update file's sharedWith array
            firestore.collection(COLLECTION_FILES)
                .document(fileId)
                .update(
                    "sharedWith",
                    com.google.firebase.firestore.FieldValue.arrayUnion(targetUserId)
                )
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }

    override suspend fun downloadFile(fileItem: FileItem): Resource<Uri> {
        return try {
            // For simplicity, return the download URL
            // In a real app, you might want to download to local storage
            Resource.Success(Uri.parse(fileItem.fileUrl))
        } catch (e: Exception) {
            Resource.Failure(e)
        }
    }

}