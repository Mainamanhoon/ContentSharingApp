package com.psyfen.domain.model

import java.io.Serializable

data class User(
    var uid: String? = null,
    var userId: String? = null,
    var phoneNumber: String? = null,
    var displayName: String? = null,
    var createdAt: Long = 0
) : Serializable