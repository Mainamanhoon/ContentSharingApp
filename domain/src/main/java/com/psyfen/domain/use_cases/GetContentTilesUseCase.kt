package com.psyfen.domain.use_cases


import com.psyfen.common.Resource
import com.psyfen.domain.model.ContentTile
 import com.psyfen.domain.respository.ContentTileRepository
import javax.inject.Inject


class GetContentTilesUseCase @Inject constructor(
    private val repository: ContentTileRepository
) {

    suspend operator fun invoke(): Resource<List<ContentTile>> {
        return when (val result = repository.fetchContentTiles()) {
            is Resource.Success -> {
                // Business logic: Sort tiles by order
                val sortedTiles = result.result.sortedBy { it.order }
                Resource.Success(sortedTiles)
            }
            is Resource.Failure -> result
            is Resource.Loading -> Resource.Loading
        }
    }
}


class AddContentTileUseCase @Inject constructor(
    private val repository: ContentTileRepository
) {
    suspend operator fun invoke(tile: ContentTile): Resource<String> {
        return repository.addTile(tile)
    }
}