package com.example.mashop.ui.my_orders

import androidx.lifecycle.ViewModel
import com.example.mashop.db.order_offer.OrderOfferRepository

class MyOrdersViewModel : ViewModel() {
    var orders: List<OrderOfferRepository.Companion.OrderWithOffers>? = null
}