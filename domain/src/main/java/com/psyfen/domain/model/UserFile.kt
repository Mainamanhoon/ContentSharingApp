package com.psyfen.domain.model

import java.io.Serializable


data class UserFile(
    var id: String? = null,
    var fileName: String? = null,
    var fileUrl: String? = null,
    var fileType: String? = null,
    var fileSize: Long = 0,
    var uploadedBy: String? = null,
    var uploadedAt: Long = 0,
    var visibility: FileVisibility = FileVisibility.PRIVATE,
    var sharedWith: List<String> = emptyList()
) : Serializable
