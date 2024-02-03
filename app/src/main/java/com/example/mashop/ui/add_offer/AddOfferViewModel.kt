package com.example.mashop.ui.add_offer

import androidx.lifecycle.ViewModel
import com.example.mashop.db.category.Category

class AddOfferViewModel : ViewModel() {
    var categories: List<Category>? = null
    var categoryList: ArrayList<String>? = null

    var uploadedImageKey: String? = null
}