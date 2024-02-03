package com.example.mashop.db.category

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Category (
    val id: Long,

    @SerialName("created_at")
    val createdAt: Instant,

    val name: String
)