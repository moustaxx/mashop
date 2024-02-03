package com.example.mashop.db.offer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OfferCreateInput (
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
    val imageUrl: String?
)