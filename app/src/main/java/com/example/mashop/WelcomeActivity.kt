package com.example.mashop

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.mashop.db.supabase
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

class WelcomeActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        sharedPreferences = getSharedPreferences("shared", Context.MODE_PRIVATE)
    }

    override fun onStart() {
        super.onStart()

        val sessionStr = sharedPreferences.getString("session", "")
        if (sessionStr.isNullOrEmpty()) return
        val session = Json.decodeFromString<UserSession>(sessionStr)

        runBlocking {
            launch {
                try {
                    supabase.auth.importSession(session)
                } catch (e: Exception) {
                    Log.v("DEBUG", e.message ?: "Session is invalid")
                    return@launch
                }
                if (supabase.auth.sessionStatus.value is SessionStatus.Authenticated) {
                    startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun loginClick(v: View) {
        startActivity(Intent(this, SignInActivity::class.java))
    }

    @Suppress("UNUSED_PARAMETER")
    fun signupClick(v: View) {
        startActivity(Intent(this, SignUpActivity::class.java))
    }
}