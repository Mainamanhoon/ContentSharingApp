package com.psyfen.taskapplication.com.psyfen.taskapplication.screen.content_tiles

import android.content.ActivityNotFoundException
import android.content.Context
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.psyfen.common.Resource
import com.psyfen.domain.model.ContentTile
import com.psyfen.domain.model.TileType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentTilesScreen(
    viewModel: ContentTilesViewModel = hiltViewModel(),
    onNavigateToWebView: (String, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

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
                    containerColor = Color(0xFF101322),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF101322)
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
                        (uiState as Resource.Failure).exception.message?:"Unknown Error",
                        onRetry = {viewModel.refreshTiles()})
                }
                is Resource.Success -> {
                    val tiles = (uiState as Resource.Success<List<ContentTile>>).result
                    if(tiles.isNotEmpty()){
                        TilesGrid(
                            tiles = tiles,
                            onTileClick = { tile ->
                                handleTileClick(context, tile, onNavigateToWebView)
                            }
                        )
                    }else{
                        EmptyView()
                    }
                }
            }
        }
    }
}

@Composable
fun TilesGrid(
    tiles: List<ContentTile>,
    onTileClick: (ContentTile) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(tiles) { tile ->
            TileItem(
                tile = tile,
                onClick = { onTileClick(tile) }
            )
        }
    }
}

@Composable
fun TileItem(
    tile: ContentTile,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                color = Color(0xFFFF3333),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFfd511e)
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

// Handle tile clicks
// YouTube videos: Open in YouTube app (or browser if app not installed)
// Regular content: Open in WebView
private fun handleTileClick(
    context: Context,
    tile: ContentTile,
    onNavigateToWebView: (String, String) -> Unit
) {
    when (tile.type) {
        TileType.YOUTUBE -> {
            tile.youtubeVideoId?.let { videoId ->
                openYouTubeVideo(context, videoId)
            }
        }
        TileType.CONTENT -> {
            tile.webViewUrl?.let { url ->
                onNavigateToWebView(url, tile.title ?: "Content")
            }
        }
    }
}

// Opens YouTube video in YouTube app
// Falls back to browser if YouTube app not installed
private fun openYouTubeVideo(context: Context, videoId: String) {
    // Try to open in YouTube app
    val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoId"))
    appIntent.putExtra("force_fullscreen", true)

    try {
        context.startActivity(appIntent)
    } catch (e: ActivityNotFoundException) {
        // YouTube app not installed, open in browser
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.youtube.com/watch?v=$videoId")
        )
        context.startActivity(webIntent)
    }
}