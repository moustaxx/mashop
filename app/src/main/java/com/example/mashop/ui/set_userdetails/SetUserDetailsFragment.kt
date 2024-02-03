package com.example.mashop.ui.set_userdetails

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import com.example.mashop.R
import com.example.mashop.databinding.FragmentSetUserDetailsBinding
import com.example.mashop.db.user.User
import com.example.mashop.db.user.UserRepository
import com.example.mashop.db.user.UserDetails
import com.google.android.material.snackbar.Snackbar
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

class SetUserDetailsFragment : Fragment() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userInfo: UserInfo
    private lateinit var viewModel: SetUserDetailsModel

    private var _binding: FragmentSetUserDetailsBinding? = null

    init {
        lifecycleScope.launch {
            withStarted {
                launch {
                    fetchUserDetails()
                    if (_binding != null) loadUserDetails()
                }
            }
        }
    }

    private suspend fun fetchUserDetails() {
        var user: User? = null
        try {
            user = UserRepository.getUserById(userInfo.id)
        } catch (e: Exception) {
            Log.e("ERR", e.toString())
        }
        if (user == null) return
        viewModel.user = user
    }

    private fun loadUserDetails() {
        val user = viewModel.user
        if (user == null) return

        binding.compUserFristName.editText?.setText(user.firstName)
        binding.compUserLastName.editText?.setText(user.lastName)
        binding.compUserCountry.editText?.setText(user.country)
        binding.compUserCity.editText?.setText(user.city)
        binding.compUserZipCode.editText?.setText(user.zipCode)
        binding.compUserAddress.editText?.setText(user.address)
    }


    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[SetUserDetailsModel::class.java]
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
        _binding = FragmentSetUserDetailsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.compUserSubmitBtn.setOnClickListener(onSubmitClick)
        return root
    }

    private val onSubmitClick = OnClickListener {
        val firstName = binding.compUserFristName.editText?.text.toString()
        val lastName = binding.compUserLastName.editText?.text.toString()
        val country = binding.compUserCountry.editText?.text.toString()
        val city = binding.compUserCity.editText?.text.toString()
        val zipCode = binding.compUserZipCode.editText?.text.toString()
        val address = binding.compUserAddress.editText?.text.toString()

        if (firstName.isEmpty() || lastName.isEmpty() || country.isEmpty()
            || city.isEmpty() || zipCode.isEmpty() || address.isEmpty()
        ) {
            Toast.makeText(context, getString(R.string.fields_cannot_be_empty), Toast.LENGTH_LONG).show()
            return@OnClickListener
        }

        runBlocking {
            launch {
                UserRepository.fillUserDetails(
                    userInfo.id,
                    UserDetails(firstName, lastName, country, city, zipCode, address)
                )
                Snackbar.make(requireView(), getString(R.string.data_saved_successfully), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}