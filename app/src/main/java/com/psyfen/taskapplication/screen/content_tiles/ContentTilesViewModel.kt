package com.psyfen.taskapplication.com.psyfen.taskapplication.screen.content_tiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.Resource
import com.psyfen.domain.model.ContentTile
import com.psyfen.domain.use_cases.GetContentTilesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ContentTilesViewModel @Inject constructor(
    private val getContentTilesUseCase: GetContentTilesUseCase
) : ViewModel() {

    private val _dataState = MutableStateFlow<Resource<List<ContentTile>>?>(null)
    val dataState: StateFlow<Resource<List<ContentTile>>?> = _dataState

    init {
        loadContentTiles()
    }

    /**
     * Loads content tiles from Firebase Firestore
     */
    fun loadContentTiles() {
        _dataState.value = Resource.Loading
        viewModelScope.launch {
            val result = getContentTilesUseCase()
            _dataState.value = result
        }

    }

    /**
     * Refreshes the content tiles
     */
    fun refreshTiles() {
        loadContentTiles()
    }

    /**
     * Gets a tile at a specific index
     * @param index The index (0-5 for 6 tiles)
     * @return ContentTile or null
     */
    fun getTileAt(index: Int): ContentTile? {
        return when (val state = _dataState.value) {
            is Resource.Success -> state.result.getOrNull(index)
            else -> null
        }
    }
}