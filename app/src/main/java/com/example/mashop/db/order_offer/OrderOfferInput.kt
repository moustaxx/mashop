package com.example.mashop.db.order_offer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderOfferInput (
    @SerialName("order_id")
    val orderId: Long,

    @SerialName("offer_id")
    val offerId: Long,

    val quantity: Int
)