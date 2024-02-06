package com.example.mashop.ui.cart

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.mashop.R
import com.example.mashop.databinding.FragmentCartBinding
import com.example.mashop.db.offer.Offer
import com.example.mashop.db.offer.OfferRepository
import com.example.mashop.db.order.OrderRepository
import com.example.mashop.db.supabase
import com.example.mashop.db.user.User
import com.example.mashop.db.user.UserRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CartFragment : Fragment() {
    private lateinit var cartViewModel: CartViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userInfo: UserInfo

    class CartStateItem(val product: Offer, var quantity: Int)

    private var _binding: FragmentCartBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    init {
        lifecycleScope.launch {
            withStarted {
                launch {
                    if (_binding != null) binding.cartLoading.visibility = View.VISIBLE
                    fetchData()
                    if (_binding != null) {
                        showResult()
                        binding.cartLoading.visibility = View.GONE
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

    private fun clearCartState() {
        sharedPreferences.edit().putString("cart", "").apply()
    }

    @SuppressLint("SetTextI18n")
    private fun refreshSummaryPrice() {
        if (cartViewModel.cartState == null) return
        val summaryPrice = cartViewModel.cartState!!.fold(0.0) { a, b -> a + (b.product.price * b.quantity) }
        binding.cartSummaryPrice.text = String.format("%.2f", summaryPrice) + " zł"
    }

    private suspend fun fetchUser(): User? {
        val sessionStr = sharedPreferences.getString("session", "")
        if (sessionStr.isNullOrEmpty()) throw Exception("Session string is null or empty")
        userInfo = Json.decodeFromString<UserSession>(sessionStr).user!!

        return UserRepository.getUserById(userInfo.id)
    }

    private suspend fun fetchData() {
        if (cartViewModel.cartState != null) return

        if (cartViewModel.user == null) {
            try {
                val user = fetchUser()
                cartViewModel.user = user
            } catch (e: Exception) {
                Log.e("ERR", e.toString())
                return
            }
        }

        val lastCart = getCartState()
        var offers: List<Offer>? = null
        try {
            offers = OfferRepository.getOffersFromList(lastCart.map { el -> el.productId })
        } catch (e: Exception) {
            Log.e("ERR", e.toString())
        }
        if (offers == null) return

        cartViewModel.cartState = ArrayList()

        for (offer in offers) {
            val found = lastCart.findLast { el -> el.productId == offer.id }
            if (found == null) continue
            cartViewModel.cartState!!.add(CartStateItem(
                offer,
                found.quantity
            ))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showResult() {
        val user = cartViewModel.user!!

        val userDetailsEmpty = user.firstName == null || user.lastName == null || user.address == null
                || user.city == null || user.country == null || user.zipCode == null

        if (!userDetailsEmpty) {
            binding.cartDeliveryAddress.text = """
                ${user.firstName} ${user.lastName}
                ${user.zipCode} ${user.city}
                ${user.address}
            """.trimIndent()
        } else {
            binding.cartDeliveryAddress.text = "Dane wysyłkowe muszą zostać uzupełnione"
        }

        if (cartViewModel.cartState.isNullOrEmpty())
            binding.cartNoResult.visibility = View.VISIBLE

        if (cartViewModel.cartState == null)
            return

        refreshSummaryPrice()

        for (cartStateElement in cartViewModel.cartState!!) {
            val offer = cartStateElement.product
            val cv = View.inflate(context, R.layout.cart_list_item, null)
            val tvId = cv.findViewById<TextView>(R.id.cartListItemId)
            val tvTitle = cv.findViewById<TextView>(R.id.cartListItemTitle)
            val tvPrice = cv.findViewById<TextView>(R.id.cartListItemPrice)
            val tvQuantity = cv.findViewById<TextView>(R.id.cartListItemQuantity)
            val removeBtn = cv.findViewById<MaterialButton>(R.id.cartListItemRemoveBtn)
            val minusBtn = cv.findViewById<MaterialButton>(R.id.cartListItemMinusBtn)
            val plusBtn = cv.findViewById<MaterialButton>(R.id.cartListItemPlusBtn)
            val imageView = cv.findViewById<ImageView>(R.id.cartListItemImage)

            tvId.text = offer.id.toString()
            tvTitle.text = offer.title
            tvPrice.text = String.format("%.2f", offer.price) + " zł"
            tvQuantity.text = cartStateElement.quantity.toString()

            if (offer.imageUrl != null) {
                val url = "https://${supabase.supabaseUrl}/storage/v1/object/public/${offer.imageUrl}"
                Glide.with(this)
                    .load(url)
                    .into(imageView)
            }

            binding.cartList.addView(cv)

            minusBtn.setOnClickListener {
                val curValue = cartStateElement.quantity - 1
                if (curValue < 1) return@setOnClickListener
                cartStateElement.quantity = curValue
                tvQuantity.text = curValue.toString()
                refreshSummaryPrice()

                val lastCart = cartViewModel.cartState!!.map { el -> CartItem(
                    el.product.id,
                    if (el.product.id == offer.id) curValue else el.quantity
                )
                }

                setCartState(lastCart)
            }
            plusBtn.setOnClickListener {
                val curValue = cartStateElement.quantity + 1
                cartStateElement.quantity = curValue
                tvQuantity.text = curValue.toString()
                refreshSummaryPrice()

                val lastCart = cartViewModel.cartState!!.map { el -> CartItem(
                    el.product.id,
                    if (el.product.id == offer.id) curValue else el.quantity
                )
                }

                setCartState(lastCart)
            }

            removeBtn.setOnClickListener {
                cartViewModel.cartState!!.removeIf { el -> el.product.id == offer.id }

                val lastCart = cartViewModel.cartState!!.map { el -> CartItem(el.product.id, el.quantity) }
                setCartState(lastCart)

                binding.cartList.removeView(cv)
                refreshSummaryPrice()

                if (cartViewModel.cartState!!.isEmpty())
                    binding.cartNoResult.visibility = View.VISIBLE
            }
        }

        binding.cartClearBtn.setOnClickListener {
            clearCartState()

            cartViewModel.cartState!!.clear()
            Snackbar
                .make(requireView(), getString(R.string.cart_cleared), Snackbar.LENGTH_LONG)
                .show()

            refreshSummaryPrice()
            binding.cartList.removeAllViews()
            binding.cartNoResult.visibility = View.VISIBLE
        }

        binding.cartProceedBtn.setOnClickListener {
            if (user.firstName == null || user.lastName == null || user.address == null
                || user.city == null || user.country == null || user.zipCode == null
            ) {
                Snackbar
                    .make(requireView(), getString(R.string.user_data_must_be_filled), Snackbar.LENGTH_LONG)
                    .show()
                findNavController().navigate(R.id.nav_set_user_details)
                return@setOnClickListener
            }

            runBlocking {
                launch {
                    OrderRepository.makeOrder(user.id, cartViewModel.cartState!!)
                }
            }

            clearCartState()
            Snackbar
                .make(requireView(), getString(R.string.ordered_successfully), Snackbar.LENGTH_LONG)
                .show()

            findNavController().navigate(R.id.action_CartFragment_to_MyOrdersFragment)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cartViewModel = ViewModelProvider(this)[CartViewModel::class.java]
        sharedPreferences = requireContext().getSharedPreferences("shared", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)

        if (cartViewModel.cartState != null)
            showResult()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        cartViewModel.cartState = null
    }
}