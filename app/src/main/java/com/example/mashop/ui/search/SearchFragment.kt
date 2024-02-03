package com.example.mashop.ui.search

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
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {
    private lateinit var searchView: SearchView
    private lateinit var searchBar: SearchBar
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var queryPhrase: String

    private var _binding: FragmentSearchBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    init {
        lifecycleScope.launch {
            withStarted {
                launch {
                    if (_binding != null) _binding!!.searchLoading.visibility = View.VISIBLE
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
        if (arguments?.getString("queryPhrase").isNullOrEmpty()) return
        queryPhrase = requireArguments().getString("queryPhrase")!!

        if (searchViewModel.offerList != null) return

        var offers: List<Offer>? = null
        try {
            offers = OfferRepository.searchForOffers(queryPhrase)
        } catch (e: Exception) {
            Log.e("ERR", e.toString())
        }
        if (offers == null) return

        if (offers.isNotEmpty()) {
            val offerList = arrayListOf<OfferListItem>()

            for (offer in offers)
                offerList.add(OfferListItem(offer.id, offer.title, offer.price, offer.imageUrl))

            searchViewModel.offerList = offerList
        }
    }

    private fun showSearchResult() {
        val offerList = searchViewModel.offerList
        if (_binding == null) return

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
            findNavController().navigate(R.id.action_SearchFragment_to_OfferFragment, bundle)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchViewModel = ViewModelProvider(this)[SearchViewModel::class.java]

        searchView = requireActivity().findViewById(R.id.search_view)
        searchBar = requireActivity().findViewById(R.id.search_bar)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        if (searchViewModel.offerList != null)
            showSearchResult()

        return binding.root
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        searchBar.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPrimaryNavigationFragmentChanged(isPrimaryNavigationFragment: Boolean) {
        super.onPrimaryNavigationFragmentChanged(isPrimaryNavigationFragment)
        if (!isPrimaryNavigationFragment) {
            searchBar.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        searchViewModel.offerList = null
    }
}