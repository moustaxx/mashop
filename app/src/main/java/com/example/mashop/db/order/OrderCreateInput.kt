package com.example.mashop.db.order

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderCreateInput (
    @SerialName("buyer_id")
    val buyerId: String
)