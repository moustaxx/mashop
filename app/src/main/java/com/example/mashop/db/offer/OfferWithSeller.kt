package com.example.mashop.db.offer

import com.example.mashop.db.user.User
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OfferWithSeller (
    val id: Long,

    @SerialName("created_at")
    val createdAt: Instant,

    val title: String,

    val content: String,

    val price: Double,

    @SerialName("seller_id")
    val sellerId: String,

    @SerialName("category_id")
    val categoryId: Long,

    @SerialName("is_available")
    val isAvailable: Boolean,

    @SerialName("image_url")
    val imageUrl: String?,

    @SerialName("user")
    val seller: User
)