package com.example.mashop.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import androidx.navigation.fragment.findNavController
import com.example.mashop.ui.offer_list.OfferListItem
import com.example.mashop.ui.offer_list.OfferListViewAdapter
import com.example.mashop.R
import com.example.mashop.databinding.FragmentHomeBinding
import com.example.mashop.db.category.Category
import com.example.mashop.db.category.CategoryRepository
import com.example.mashop.db.offer.Offer
import com.example.mashop.db.offer.OfferRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private lateinit var homeViewModel: HomeViewModel

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    init {
        lifecycleScope.launch {
            withStarted {
                launch {
                    if (_binding != null) {
                        binding.listViewOffers.visibility = View.GONE
                        binding.listViewCategories.visibility = View.GONE
                        binding.homeOffersLoading.visibility = View.VISIBLE
                        binding.homeCategoriesLoading.visibility = View.VISIBLE
                    }

                    fetchOffers()
                    if (_binding != null) {
                        showOffersResult()
                        binding.homeOffersLoading.visibility = View.GONE
                        binding.listViewOffers.visibility = View.VISIBLE
                    }

                    fetchCategories()
                    if (_binding != null) {
                        showCategoriesResult()
                        binding.homeCategoriesLoading.visibility = View.GONE
                        binding.listViewCategories.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private suspend fun fetchCategories() {
        var categories: List<Category>? = null
        try {
            categories = CategoryRepository.getAllCategories()
        } catch (e: Exception) {
            Log.e("ERR", e.toString())
        }
        if (categories.isNullOrEmpty()) return

        val categoryList = arrayListOf<String>()
        for (category in categories)
            categoryList.add(category.name)

        homeViewModel.categories = categories
        homeViewModel.categoryList = categoryList
    }

    private suspend fun fetchOffers() {
        var offers: List<Offer>? = null
        try {
            offers = OfferRepository.getLastAddedOffers(10)
        } catch (e: Exception) {
            Log.e("ERR", e.toString())
        }
        if (offers.isNullOrEmpty()) return

        val offerList = arrayListOf<OfferListItem>()
        for (offer in offers)
            offerList.add(OfferListItem(offer.id, offer.title, offer.price, offer.imageUrl))

        homeViewModel.offerList = offerList
    }

    private fun showOffersResult() {
        val offerList = homeViewModel.offerList
        if (offerList.isNullOrEmpty()) {
            binding.homeOffersNoResult.visibility = View.VISIBLE
            return
        }

        val offerListView = binding.listViewOffers
        val offerAdapter = OfferListViewAdapter(requireContext(), offerList)
        offerListView.adapter = offerAdapter

        @Suppress("UNUSED_ANONYMOUS_PARAMETER")
        offerListView.setOnItemClickListener { parent, view, position, id ->
            val element = offerAdapter.getItem(position)

            val bundle = bundleOf("offerId" to element!!.id)
            findNavController().navigate(R.id.action_HomeFragment_to_OfferFragment, bundle)
        }
    }

    private fun showCategoriesResult() {
        val categoryList = homeViewModel.categoryList
        if (categoryList.isNullOrEmpty()) {
            binding.homeCategoriesNoResult.visibility = View.VISIBLE
            return
        }

        val categoryListView = binding.listViewCategories
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, categoryList)
        categoryListView.adapter = categoryAdapter

        @Suppress("UNUSED_ANONYMOUS_PARAMETER")
        categoryListView.setOnItemClickListener { parent, view, position, id ->
            val element = categoryAdapter.getItem(position)
            val selectedCategoryId = homeViewModel.categories!!.find { cat -> cat.name == element }?.id
            val bundle = bundleOf("categoryId" to selectedCategoryId, "categoryName" to element)
            findNavController().navigate(R.id.action_HomeFragment_to_CategoryFragment, bundle)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        if (arguments?.getBoolean("shouldReloadOffers") == true) {
            binding.homeOffersLoading.visibility = View.VISIBLE
            runBlocking {
                launch {
                    fetchOffers()
                }
            }
            binding.homeOffersLoading.visibility = View.GONE
        }

        if (homeViewModel.categoryList != null && homeViewModel.offerList != null) {
            showOffersResult()
            showCategoriesResult()
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}