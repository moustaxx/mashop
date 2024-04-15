package com.example.mashop.ui.offer

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import com.bumptech.glide.Glide
import com.example.mashop.ui.cart.CartItem
import com.example.mashop.R
import com.example.mashop.databinding.FragmentOfferBinding
import com.example.mashop.db.offer.OfferRepository
import com.example.mashop.db.offer.OfferWithSeller
import com.example.mashop.db.supabase
import com.google.android.material.snackbar.Snackbar
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class OfferFragment : Fragment() {
    private var _binding: FragmentOfferBinding? = null
    private lateinit var offerViewModel: OfferViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userInfo: UserInfo
    private var quantity = 1

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    init {
        lifecycleScope.launch {
            withStarted {
                launch {
                    if (_binding != null) binding.offerLoading.visibility = View.VISIBLE
                    fetchData()
                    if (_binding != null) {
                        showFetchResult()
                        binding.offerLoading.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun getCartState(): ArrayList<CartItem> {
        val lastCart =  sharedPreferences.getString("cart", "")!!
        var lastCartData: ArrayList<CartItem> = ArrayList()
        try {
            lastCartData = Json.decodeFromString(lastCart)
        } catch (_: Exception) {}

        return lastCartData
    }

    private fun setCartState(new: List<CartItem>) {
        val jsonData = Json.encodeToString(new)
        sharedPreferences.edit().putString("cart", jsonData).apply()
    }

    private suspend fun fetchData() {
        val offerId = arguments?.getLong("offerId")
        if (offerId == null) return

        var offer: OfferWithSeller? = null
        try {
            offer = OfferRepository.getOffer(offerId)
        } catch (e: Exception) {
            Log.e("ERR", e.toString())
        }
        if (offer == null) return
        offerViewModel.offer = offer
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        offerViewModel = ViewModelProvider(this)[OfferViewModel::class.java]
        sharedPreferences = requireContext().getSharedPreferences("shared", Context.MODE_PRIVATE)

        val sessionStr = sharedPreferences.getString("session", "")
        if (sessionStr.isNullOrEmpty()) throw Exception("Session string is null or empty")
        userInfo = Json.decodeFromString<UserSession>(sessionStr).user!!
    }

    @SuppressLint("SetTextI18n")
    private fun showFetchResult() {
        val offer = offerViewModel.offer
        if (offer == null) return

        binding.offerTitle.text = offer.title
        binding.offerPrice.text = String.format("%.2f", offer.price) + " " + getString(R.string.currency_symbol)
        binding.offerContent.text = offer.content
        binding.offerSellerName.text = offer.seller.firstName
        binding.offerSellerCity.text = offer.seller.city

        if (offer.imageUrl != null) {
            val url = "https://${supabase.supabaseUrl}/storage/v1/object/public/${offer.imageUrl}"
            Glide.with(this)
                .load(url)
                .into(binding.offerImageView)
            binding.offerImageView.background = null
        }

        if (offer.sellerId == userInfo.id) {
            binding.offerAddToCart.visibility = View.GONE
            binding.offerQuantityLayout.visibility = View.GONE
        }

        binding.offerMinusBtn.setOnClickListener {
            val curValue = quantity - 1
            if (curValue < 1) return@setOnClickListener
            quantity = curValue
            binding.offerQuantity.text = curValue.toString()
        }
        binding.offerPlusBtn.setOnClickListener {
            quantity += 1
            binding.offerQuantity.text = quantity.toString()
        }

        binding.offerAddToCart.setOnClickListener {
            val lastCart = getCartState()
            if (lastCart.findLast { el -> el.productId == offer.id } != null) {
                Snackbar
                    .make(requireView(), getString(R.string.already_in_cart), Snackbar.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }

            lastCart.add(CartItem(offer.id, quantity))
            setCartState(lastCart)

            Snackbar
                .make(requireView(), getString(R.string.added_to_cart), Snackbar.LENGTH_LONG)
//                .setAction("Pokaż koszyk") { findNavController().navigate(R.id.nav_cart) }
                .show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOfferBinding.inflate(inflater, container, false)
        val root: View = binding.root

        if (offerViewModel.offer != null)
            showFetchResult()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        offerViewModel.offer = null
    }
}