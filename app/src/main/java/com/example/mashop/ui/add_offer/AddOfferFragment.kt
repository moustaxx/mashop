package com.example.mashop.ui.add_offer

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import androidx.navigation.fragment.findNavController
import com.example.mashop.R
import com.example.mashop.databinding.FragmentAddOfferBinding
import com.example.mashop.db.category.Category
import com.example.mashop.db.category.CategoryRepository
import com.example.mashop.db.offer.OfferCreateInput
import com.example.mashop.db.offer.OfferRepository
import com.example.mashop.db.supabase
import com.example.mashop.db.user.UserRepository
import com.google.android.material.snackbar.Snackbar
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.gotrue.user.UserSession
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.time.LocalDateTime

class AddOfferFragment : Fragment() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userInfo: UserInfo
    private lateinit var addOfferViewModel: AddOfferViewModel

    private var _binding: FragmentAddOfferBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            binding.addOfferImageSendingTv.visibility = View.VISIBLE
            Log.d("PhotoPicker", "Selected URI: $uri")
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            requireContext().contentResolver.takePersistableUriPermission(uri, flag)
            binding.addOfferImage.setImageURI(uri)
            binding.addOfferImage.visibility = View.VISIBLE
            val mime = MimeTypeMap.getSingleton()
            val extension = mime.getExtensionFromMimeType(requireContext().contentResolver.getType(uri))
            val random = (0..100).random()
            val time = LocalDateTime.now()

            runBlocking {
                launch {
                    val bucket = supabase.storage.from("images")
                    val imageKey = bucket.upload("$time-$random.$extension", uri, upsert = false)
                    addOfferViewModel.uploadedImageKey = imageKey
                }
            }
            binding.addOfferImageSendingTv.visibility = View.GONE
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    init {
        lifecycleScope.launch {
            withStarted {
                launch {
                    fetchCategories()
                    if (_binding != null) loadCategories()
                }
            }
        }
    }

    private suspend fun fetchCategories() {
        val sessionStr = sharedPreferences.getString("session", "")
        if (sessionStr.isNullOrEmpty()) return
        userInfo = Json.decodeFromString<UserSession>(sessionStr).user!!

        if (addOfferViewModel.categoryList != null) return

        val categoryList = arrayListOf<String>()

        var categories: List<Category>? = null
        try {
            categories = CategoryRepository.getAllCategories()
        } catch (e: Exception) {
            Log.e("ERR", e.toString())
        }
        if (categories == null) return

        for (category in categories)
            categoryList.add(category.name)

        addOfferViewModel.categories = categories
        addOfferViewModel.categoryList = categoryList
    }

    private fun loadCategories() {
        val categoryList = addOfferViewModel.categoryList
        if (categoryList.isNullOrEmpty()) return

        val categoryListView = binding.filledExposedDropdown
        val categoryAdapter = ArrayAdapter(requireContext(), R.layout.select_dialog_item, categoryList)
        categoryListView.setAdapter(categoryAdapter)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addOfferViewModel = ViewModelProvider(this)[AddOfferViewModel::class.java]
        sharedPreferences = requireContext().getSharedPreferences("shared", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddOfferBinding.inflate(inflater, container, false)
        val root: View = binding.root

        if (addOfferViewModel.categoryList != null)
            loadCategories()

        binding.addOfferSubmitBtn.setOnClickListener(onSubmitClick)
        binding.addOfferImagePickBtn.setOnClickListener(onImagePickClick)
        return root
    }

    private val onImagePickClick = OnClickListener  {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val onSubmitClick = OnClickListener {
        val title = binding.addOfferTitle.editText?.text.toString().trim()
        val price = binding.addOfferPrice.editText?.text.toString()
        val content = binding.addOfferContent.editText?.text.toString().trim()
        val category = binding.addOfferCategoryDropdown.editText?.text.toString()

        val imageSendInProgress = binding.addOfferImageSendingTv.visibility == View.VISIBLE
        val imageKey = addOfferViewModel.uploadedImageKey

        if (imageSendInProgress) {
            Toast.makeText(context, getString(R.string.wait_for_photo_sent), Toast.LENGTH_LONG).show()
            return@OnClickListener
        }

        if (title.isEmpty() || price.isEmpty() || content.isEmpty() || category.isEmpty()) {
            Toast.makeText(context, getString(R.string.fields_cannot_be_empty), Toast.LENGTH_LONG).show()
            return@OnClickListener
        }

        val selectedCategoryId = addOfferViewModel.categories!!.find { cat -> cat.name == category }?.id

        if (selectedCategoryId == null) {
            Toast.makeText(context, getString(R.string.wrong_category), Toast.LENGTH_LONG).show()
            return@OnClickListener
        }

        runBlocking {
            launch {
                val seller = UserRepository.getUserById(userInfo.id)

                if (seller?.firstName == null || seller.lastName == null || seller.address == null
                    || seller.city == null || seller.country == null || seller.zipCode == null
                ) {
                    Snackbar
                        .make(requireView(), getString(R.string.user_data_must_be_filled), Snackbar.LENGTH_LONG)
                        .show()
                    findNavController().navigate(R.id.nav_set_user_details)
                    return@launch
                }

                OfferRepository.addOffer(OfferCreateInput(
                    title, content, price.toDouble(), userInfo.id,
                    selectedCategoryId, true, imageKey
                ))
            }
        }
        Snackbar.make(requireView(), getString(R.string.offer_added_successfully), Snackbar.LENGTH_LONG).show()

        val bundle = bundleOf("shouldReloadOffers" to true)
        findNavController().navigate(R.id.action_AddOfferFragment_to_HomeFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        addOfferViewModel.uploadedImageKey = null
    }
}