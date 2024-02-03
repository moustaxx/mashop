package com.example.mashop.db.order_offer

import android.util.Log
import com.example.mashop.db.offer.Offer
import com.example.mashop.db.order.Order
import com.example.mashop.db.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class OrderOfferRepository {
    companion object {
        @Serializable
        data class OfferWithOrder (
            val id: Long,
            val quantity: Int,
            val offer: Offer,

            @SerialName("order_id")
            val orderId: Long
        )

        data class OrderWithOffers (
            val id: Long,
            val createdAt: Instant,
            val orderedOffers: List<OfferWithOrder>
        )

        suspend fun getOrderedOffers(buyerId: String): List<OrderWithOffers> {
            val orders = supabase.from("order").select {
                filter {
                    Order::buyerId eq buyerId
                }
            }.decodeList<Order>()
            Log.v("orders", orders.toString())

            val columns = Columns.raw("id, quantity, order_id, offer (*)".trimIndent())
            val offersWithOrder = supabase.from("order_offer").select(columns) {
                filter { OrderOffer::orderId isIn orders.map { el -> el.id } }
            }.decodeList<OfferWithOrder>()

            val ordersWithOffers = mutableListOf<OrderWithOffers>()
            for (order in orders) {
                ordersWithOffers.add(OrderWithOffers(
                    order.id,
                    order.createdAt,
                    offersWithOrder.filter { el -> el.orderId == order.id }
                ))
            }

            return ordersWithOffers
        }
    }
}