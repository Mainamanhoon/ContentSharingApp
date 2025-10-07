package com.psyfen.domain.model

import java.io.Serializable

data class User(
    var uid: String = "",
    val username: String = "",
    val phoneNumber: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Serializable
