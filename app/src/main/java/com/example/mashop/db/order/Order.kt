package com.example.mashop.db.order

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Order (
    val id: Long,

    @SerialName("created_at")
    val createdAt: Instant,

    @SerialName("buyer_id")
    val buyerId: String
)