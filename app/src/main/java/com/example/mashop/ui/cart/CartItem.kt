package com.example.mashop.ui.cart

import kotlinx.serialization.Serializable

@Serializable
class CartItem(
    val productId: Long,
    val quantity: Int
)
