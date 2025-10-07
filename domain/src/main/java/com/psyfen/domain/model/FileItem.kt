package com.psyfen.domain.model

import java.io.Serializable

data class FileItem(
    val id: String = "",
    val fileName: String = "",
    val fileUrl: String = "",
    val fileType: String = "",
    val fileSize: Long = 0,
    val isPublic: Boolean = false,
    val ownerId: String = "",
    val ownerName: String = "",
    val sharedWith: List<String> = emptyList(), // List of user IDs or phone numbers
    val uploadedAt: Long = System.currentTimeMillis()
) : Serializable
