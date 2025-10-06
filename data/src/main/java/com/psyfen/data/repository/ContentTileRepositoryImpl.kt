package com.psyfen.data.repository

import com.psyfen.common.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.psyfen.domain.model.ContentTile
import com.psyfen.domain.respository.ContentTileRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class ContentTileRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ContentTileRepository {

    companion object {
        private const val COLLECTION_TILES = "content_tiles"
    }

    override suspend fun fetchContentTiles(): Resource<List<ContentTile>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_TILES)
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .await()

            if (snapshot.isEmpty) {
                Resource.Failure(Exception("No tiles found"))
            } else {
                val tiles = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ContentTile::class.java)?.apply {
                        id = doc.id
                    }
                }
                Resource.Success(tiles)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun fetchTileById(tileId: String): Resource<ContentTile> {
        return try {
            val doc = firestore.collection(COLLECTION_TILES)
                .document(tileId)
                .get()
                .await()

            if (doc.exists()) {
                val tile = doc.toObject(ContentTile::class.java)?.apply {
                    id = doc.id
                }
                if (tile != null) {
                    Resource.Success(tile)
                } else {
                    Resource.Failure(Exception("Failed to parse tile"))
                }
            } else {
                Resource.Failure(Exception("Tile not found"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun addTile(tile: ContentTile): Resource<String> {
        return try {
            val docRef = firestore.collection(COLLECTION_TILES)
                .add(tile)
                .await()
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }
}