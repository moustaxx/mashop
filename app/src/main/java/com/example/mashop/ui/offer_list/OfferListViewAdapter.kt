package com.example.mashop.ui.offer_list

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.mashop.R
import com.example.mashop.db.supabase

class OfferListViewAdapter(context: Context, entries: ArrayList<OfferListItem>) :
    ArrayAdapter<OfferListItem>(context, 0, entries) {

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var cv = convertView
        if (cv == null)
          cv = LayoutInflater.from(context).inflate(R.layout.offer_list_item, parent, false)!!

        val tvTitle = cv.findViewById<TextView>(R.id.offerListItemTitle)
        val tvPrice = cv.findViewById<TextView>(R.id.offerListItemPrice)
        val imageView = cv.findViewById<ImageView>(R.id.offerListItemImage)

        val data = getItem(position)!!

        tvTitle.text = data.title
        tvPrice.text = String.format("%.2f", data.price) + " " + context.getString(R.string.currency_symbol)

        if (data.imageUrl != null) {
            val url = "https://${supabase.supabaseUrl}/storage/v1/object/public/${data.imageUrl}"
            Glide.with(context)
                .load(url)
                .into(imageView)
        }

        return cv
    }
}