package com.psyfen.domain.respository


import com.example.common.Resource
import com.psyfen.domain.model.ContentTile


interface ContentTileRepository {

    suspend fun fetchContentTiles(): Resource<List<ContentTile>>

    suspend fun fetchTileById(tileId: String): Resource<ContentTile>

    suspend fun addTile(tile: ContentTile): Resource<String>
}