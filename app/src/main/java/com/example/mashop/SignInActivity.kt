package com.example.mashop

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mashop.db.auth.AuthRepository
import com.example.mashop.db.supabase
import com.google.android.material.textfield.TextInputLayout
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SignInActivity : AppCompatActivity() {
    private fun CharSequence?.isValidEmail() = !isNullOrEmpty()
        && Patterns.EMAIL_ADDRESS.matcher(this).matches()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
    }

    override fun onStart() {
        super.onStart()

        val btn = findViewById<Button>(R.id.signInSubmitBtn)
        btn.setOnClickListener(signInSubmit)
    }

    private val signInSubmit = OnClickListener { view ->
        if (view !is Button) return@OnClickListener
        view.isEnabled = false

        val emailInputLayout =  findViewById<TextInputLayout>(R.id.signInEmail)
        val passwordInputLayout =  findViewById<TextInputLayout>(R.id.signInPwd)
        val email = emailInputLayout.editText?.text.toString()
        val password = passwordInputLayout.editText?.text.toString()

        var hasErrors = false
        if (email.isEmpty()) {
            emailInputLayout.error = getString(R.string.email_is_required)
            hasErrors = true
        } else if (!email.isValidEmail()) {
            emailInputLayout.error = getString(R.string.email_is_wrong)
            hasErrors = true
        }
        else emailInputLayout.error = null

        if (password.isEmpty()) {
            passwordInputLayout.error = getString(R.string.password_is_required)
            hasErrors = true
        }
        else passwordInputLayout.error = null

        if (hasErrors) {
            view.isEnabled = true
            return@OnClickListener
        }

        runBlocking {
            launch {
                try {
                    AuthRepository.signIn(email, password)

                    if (supabase.auth.sessionStatus.value is SessionStatus.Authenticated) {
                        val status =
                            (supabase.auth.sessionStatus.value as SessionStatus.Authenticated).session
                        val session = Json.encodeToString(status)

                        getSharedPreferences("shared", Context.MODE_PRIVATE)
                            .edit()
                            .putString("session", session)
                            .apply()

                        Log.v("SIGNIN", status.toString())
                    } else {
                        Log.v("SIGNIN", supabase.auth.sessionStatus.value.toString())
                    }
                    finish()

                } catch (ex: Exception) {
                    Log.e("SIGNIN", ex.message.toString())
                    val btn = findViewById<Button>(R.id.signInSubmitBtn)
                    val err = findViewById<TextView>(R.id.signInErrorMsg)

                    err.visibility = View.VISIBLE
                    view.isEnabled = true

                    if (ex is RestException && ex.error == "invalid_grant") {
                        err.text = getString(R.string.wrong_email_or_password)
                        btn.error = getString(R.string.wrong_email_or_password)
                    } else if (ex is HttpRequestException || ex is HttpRequestTimeoutException) {
                        err.text = getString(R.string.internet_connection_error)
                        btn.error = getString(R.string.internet_connection_error)
                    } else {
                        err.text = getString(R.string.unknown_error)
                        btn.error = getString(R.string.unknown_error)
                    }
                }
            }
        }
    }
}