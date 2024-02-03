package com.example.mashop.ui.home

import androidx.lifecycle.ViewModel
import com.example.mashop.ui.offer_list.OfferListItem
import com.example.mashop.db.category.Category

class HomeViewModel : ViewModel() {
    var categories: List<Category>? = null
    var categoryList: ArrayList<String>? = null
    var offerList: ArrayList<OfferListItem>? = null
}