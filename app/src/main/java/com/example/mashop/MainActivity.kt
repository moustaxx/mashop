package com.example.mashop

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.mashop.databinding.ActivityMainBinding
import com.example.mashop.db.supabase
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userSession: UserSession
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_my_orders,
                R.id.nav_add_offer,
                R.id.nav_set_user_details,
                R.id.nav_cart
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        sharedPreferences = getSharedPreferences("shared", Context.MODE_PRIVATE)

        val sessionStr = sharedPreferences.getString("session", "")
        if (!sessionStr.isNullOrEmpty())
            userSession = Json.decodeFromString(sessionStr)
    }

    override fun onStart() {
        super.onStart()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val hv = navigationView.getHeaderView(0)
        hv.findViewById<TextView>(R.id.navHeaderUserName2).text = userSession.user!!.email

        val searchView = findViewById<SearchView>(R.id.search_view)
        val searchBar = findViewById<SearchBar>(R.id.search_bar)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)

        toolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.search)
                searchView.show()
            return@setOnMenuItemClickListener true
        }

        @Suppress("UNUSED_ANONYMOUS_PARAMETER")
        searchView.editText.setOnEditorActionListener { v, actionId, event ->
            searchBar.setText(searchView.text)
            searchView.hide()
            Log.v("searchView.text", searchView.text.toString())
            if (searchView.text.isEmpty())
                return@setOnEditorActionListener false

            val bundle = bundleOf("queryPhrase" to searchView.text.toString())

            val currentId = navController.currentDestination?.id

            searchBar.visibility = View.VISIBLE
            if (currentId == R.id.nav_search) {
                navController.popBackStack(currentId,true)
                navController.navigate(currentId, bundle)
            } else {
                navController.navigate(R.id.nav_search, bundle)
            }

            return@setOnEditorActionListener false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    @Suppress("UNUSED_PARAMETER")
    fun signOut(v: MenuItem) {
        runBlocking {
            launch {
                supabase.auth.signOut()
            }
        }
        sharedPreferences.edit().clear().apply()
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }
}