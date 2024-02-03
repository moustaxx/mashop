package com.example.mashop.ui.cart

import androidx.lifecycle.ViewModel
import com.example.mashop.db.user.User

class CartViewModel : ViewModel() {
    var cartState: ArrayList<CartFragment.CartStateItem>? = null
    var user: User? = null
}