package com.example.mashop.ui.my_orders

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import com.bumptech.glide.Glide
import com.example.mashop.R
import com.example.mashop.databinding.FragmentMyOrdersBinding
import com.example.mashop.db.order_offer.OrderOfferRepository
import com.example.mashop.db.supabase
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class MyOrdersFragment : Fragment() {
    private lateinit var myOrdersViewModel: MyOrdersViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userInfo: UserInfo

    private var _binding: FragmentMyOrdersBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    init {
        lifecycleScope.launch {
            withStarted {
                launch {
                    if (_binding != null) binding.myOrdersLoading.visibility = View.VISIBLE
                    fetchData()
                    if (_binding != null) {
                        showResult()
                        binding.myOrdersLoading.visibility = View.GONE
                    }
                }
            }
        }
    }

    private suspend fun fetchData() {
        if (myOrdersViewModel.orders != null) return

        var orders: List<OrderOfferRepository.Companion.OrderWithOffers>? = null
        try {
            orders = OrderOfferRepository.getOrderedOffers(userInfo.id)
        } catch (e: Exception) {
            Log.e("ERR", e.toString())
        }
        if (orders == null) return

        myOrdersViewModel.orders = orders
    }

    @SuppressLint("SetTextI18n")
    private fun showResult() {
        if (myOrdersViewModel.orders.isNullOrEmpty())
            binding.myOrdersNoResult.visibility = View.VISIBLE

        if (myOrdersViewModel.orders == null)
            return

        for (order in myOrdersViewModel.orders!!) {
            val cv = View.inflate(context, R.layout.order_list_item, null) as LinearLayout
            val tvId = cv.findViewById<TextView>(R.id.orderListItemId)
            val tvDate = cv.findViewById<TextView>(R.id.orderListItemDate)
            val list = cv.findViewById<LinearLayout>(R.id.orderListItemOffers)
            tvId.text = getString(R.string.order_number) + " " + order.id.toString()
            tvDate.text = getString(R.string.ordered_day) + " " + order.createdAt.toString()

            for (orderedOffer in order.orderedOffers) {
                val offer = orderedOffer.offer
                val cv2 = View.inflate(context, R.layout.order_list_item_offer, null)
                val tvTitle = cv2.findViewById<TextView>(R.id.orderListItemOfferTitle)
                val tvPrice = cv2.findViewById<TextView>(R.id.orderListItemOfferPrice)
                val tvQuantity = cv2.findViewById<TextView>(R.id.orderListItemOfferQuantity)
                val imageView = cv2.findViewById<ImageView>(R.id.orderListItemOfferImage)

                tvTitle.text = offer.title
                tvPrice.text = String.format("%.2f", offer.price) + " " + getString(R.string.currency_symbol)
                tvQuantity.text = orderedOffer.quantity.toString()

                if (offer.imageUrl != null) {
                    val url = "https://${supabase.supabaseUrl}/storage/v1/object/public/${offer.imageUrl}"
                    Glide.with(this)
                        .load(url)
                        .into(imageView)
                }
                list.addView(cv2)
            }
            binding.myOrdersList.addView(cv)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myOrdersViewModel = ViewModelProvider(this)[MyOrdersViewModel::class.java]
        sharedPreferences = requireContext().getSharedPreferences("shared", Context.MODE_PRIVATE)

        val sessionStr = sharedPreferences.getString("session", "")
        if (sessionStr.isNullOrEmpty()) return
        userInfo = Json.decodeFromString<UserSession>(sessionStr).user!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyOrdersBinding.inflate(inflater, container, false)

        if (myOrdersViewModel.orders != null)
            showResult()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        myOrdersViewModel.orders = null
    }
}