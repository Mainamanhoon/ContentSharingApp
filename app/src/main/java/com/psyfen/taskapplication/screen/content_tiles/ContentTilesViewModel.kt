package com.psyfen.taskapplication.com.psyfen.taskapplication.screen.content_tiles

import androidx.lifecycle.viewModelScope
import com.psyfen.common.Resource
import com.psyfen.common.AppViewModel
import com.psyfen.domain.model.ContentTile
import com.psyfen.domain.use_cases.GetContentTilesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContentTilesViewModel @Inject constructor(
    private val getContentTilesUseCase: GetContentTilesUseCase
) : AppViewModel() {



     private val _uiState = MutableStateFlow<Resource<List<ContentTile>>>(Resource.Loading)
     val uiState: StateFlow<Resource<List<ContentTile>>> = _uiState.asStateFlow()

    init {
         loadContentTiles()
    }

    fun loadContentTiles() {
        viewModelScope.launch {
             _uiState.value = Resource.Loading

             when (val result = getContentTilesUseCase()) {
                is Resource.Success -> {
                    // Data loaded successfully
                    _uiState.value = Resource.Success(result.result)
                }
                is Resource.Failure -> {
                     _uiState.value = Resource.Failure(result.exception)
                }
                is Resource.Loading -> {
                    // Already set above
                    Unit
                }
            }
        }
    }


    fun refreshTiles() {
        loadContentTiles()
    }


    fun onTileClick(tile: ContentTile) {
        // You can add analytics or other logic here
        // For now, just log
        println("Tile clicked: ${tile.title}")
    }
}

