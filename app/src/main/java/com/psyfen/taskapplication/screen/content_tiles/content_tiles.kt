package com.psyfen.taskapplication.com.psyfen.taskapplication.screen.content_tiles


import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.psyfen.common.Resource
import com.psyfen.domain.model.ContentTile
import com.psyfen.domain.model.TileType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentTilesScreen(
    viewModel: ContentTilesViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Explore Content",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF101322), // mainColor
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF101322) // mainColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
                      when(uiState){
                          is Resource.Loading ->{
                              LoadingView()
                          }

                          is Resource.Failure -> {
                              ErrorView(
                                  (uiState as Resource.Failure).exception.message?:"Unkown Error",
                              onRetry = {viewModel.refreshTiles()})
                          }
                          is Resource.Success -> {
                             val tiles = (uiState as Resource.Success<List<ContentTile>>).result
                              if(tiles.isNotEmpty()){
                                  TilesGrid(tiles)
                              }else{
                                  EmptyView()
                              }
                          }
                      }

            }

    }
}


@Composable
fun TilesGrid(tiles: List<ContentTile>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3), // 3 columns for landscape
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(tiles) { tile ->
            TileItem(tile = tile)
        }
    }
}


@Composable
fun TileItem(tile: ContentTile) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Square tiles
            .clickable {
                handleTileClick(context, tile)
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Tile Image
            val imageUrl = when (tile.type) {
                TileType.YOUTUBE -> tile.youtubeThumbnailUrl ?: tile.imageUrl
                TileType.CONTENT -> tile.imageUrl
            }

            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = tile.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

             Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 200f
                        )
                    )
            )

             Text(
                text = tile.title ?: "",
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(12.dp),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

             if (tile.type == TileType.YOUTUBE) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Video",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp),
                    tint = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}


@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = Color(0xFFfd511e),
                strokeWidth = 4.dp
            )
            Text(
                text = "Loading content...",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}



@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Error: $message",
                color = Color(0xFFFF3333), // red color
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFfd511e) // orange color
                )
            ) {
                Text("Retry", color = Color.White)
            }
        }
    }
}


@Composable
fun EmptyView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No content available",
            color = Color.White,
            fontSize = 16.sp
        )
    }
}

/**
 * Handles tile click events
 * Opens YouTube videos or web URLs
 */
private fun handleTileClick(context: android.content.Context, tile: ContentTile) {
    when (tile.type) {
        TileType.YOUTUBE -> {
            // Open YouTube video
            tile.youtubeVideoId?.let { videoId ->
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/watch?v=$videoId")
                )
                context.startActivity(intent)
            }
        }
        TileType.CONTENT -> {
            // Open WebView or URL
            tile.webViewUrl?.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }
        }
    }
}
private fun setupContentTilesNavigation() {
    // Add click listener to an existing button or create a new one
    binding.exploreContentBtn?.setOnClickListener {
        val intent = Intent(this, ContentTilesActivity::class.java)
        startActivity(intent)
    }
}