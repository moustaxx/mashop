package com.example.mashop.ui.category

import androidx.lifecycle.ViewModel
import com.example.mashop.ui.offer_list.OfferListItem

class CategoryViewModel : ViewModel() {
    var offerList: ArrayList<OfferListItem>? = null
}