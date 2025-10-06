package com.psyfen.domain.model

import java.io.Serializable


data class ContentTile(
    var id: String? = null,
    var title: String? = null,
    var imageUrl: String? = null,
    var type: TileType = TileType.CONTENT,
    var order: Int = 0,
    var youtubeVideoId: String? = null,
    var youtubeThumbnailUrl: String? = null,
    var webViewUrl: String? = null
) : Serializable


