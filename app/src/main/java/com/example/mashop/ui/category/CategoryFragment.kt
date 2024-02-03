package com.example.mashop.ui.category

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import androidx.navigation.fragment.findNavController
import com.example.mashop.ui.offer_list.OfferListItem
import com.example.mashop.ui.offer_list.OfferListViewAdapter
import com.example.mashop.R
import com.example.mashop.databinding.FragmentSearchBinding
import com.example.mashop.db.offer.Offer
import com.example.mashop.db.offer.OfferRepository
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class CategoryFragment : Fragment() {
    private lateinit var categoryViewModel: CategoryViewModel

    private var _binding: FragmentSearchBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    init {
        lifecycleScope.launch {
            withStarted {
                launch {
                    if (_binding != null) binding.searchLoading.visibility = View.VISIBLE
                    fetchData()
                    if (_binding != null) {
                        showSearchResult()
                        binding.searchLoading.visibility = View.GONE
                    }
                }
            }
        }
    }

    private suspend fun fetchData() {
        val categoryId = requireArguments().getLong("categoryId")
        if (arguments?.getLong("categoryId") == null) return

        if (categoryViewModel.offerList != null) return

        var offers: List<Offer>? = null
        try {
            offers = OfferRepository.getCategoryOffers(categoryId)
        } catch (e: Exception) {
            Log.e("ERR", e.toString())
        }
        if (offers == null) return

        if (offers.isNotEmpty()) {
            val offerList =  arrayListOf<OfferListItem>()

            for (offer in offers)
                offerList.add(OfferListItem(offer.id, offer.title, offer.price, offer.imageUrl))

            categoryViewModel.offerList = offerList
        }
    }

    private fun showSearchResult() {
        val offerList = categoryViewModel.offerList

        if (offerList.isNullOrEmpty()) {
            binding.searchNoResult.visibility = View.VISIBLE
            return
        }

        val offerListView = binding.listView
        val offerAdapter = OfferListViewAdapter(requireContext(), offerList)
        offerListView.adapter = offerAdapter

        offerListView.setOnItemClickListener { _, _, position, _ ->
            val element = offerAdapter.getItem(position)

            val bundle = bundleOf("offerId" to element!!.id)
            findNavController().navigate(R.id.action_CategoryFragment_to_OfferFragment, bundle)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryViewModel = ViewModelProvider(this)[CategoryViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        if (arguments?.getString("categoryName") == null) return binding.root
        val categoryName = requireArguments().getString("categoryName")

        val searchBar = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        searchBar.title = categoryName

        if (categoryViewModel.offerList != null)
            showSearchResult()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        categoryViewModel.offerList = null
    }
}