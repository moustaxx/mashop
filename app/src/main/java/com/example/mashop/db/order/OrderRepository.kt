package com.example.mashop.db.order

import com.example.mashop.db.order_offer.OrderOfferInput
import com.example.mashop.db.supabase
import com.example.mashop.ui.cart.CartFragment
import io.github.jan.supabase.postgrest.from

class OrderRepository {
    companion object {
        suspend fun makeOrder(userId: String, cartState: ArrayList<CartFragment.CartStateItem>) {
            val order = supabase.from("order")
                .insert(OrderCreateInput(userId)){ select() }.decodeSingle<Order>()

            val arr = mutableListOf<OrderOfferInput>()
            for (cartStateElement in cartState) {
                arr.add(
                    OrderOfferInput(
                    order.id,
                    cartStateElement.product.id,
                    cartStateElement.quantity
                )
                )
            }
            supabase.from("order_offer").insert(arr.toList())
        }
    }
}