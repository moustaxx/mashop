package com.example.mashop

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mashop.db.auth.AuthRepository
import com.example.mashop.db.user.UserRepository
import com.google.android.material.textfield.TextInputLayout
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SignUpActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    private fun CharSequence?.isValidEmail() = !isNullOrEmpty()
            && Patterns.EMAIL_ADDRESS.matcher(this).matches()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        sharedPreferences = getSharedPreferences("shared", Context.MODE_PRIVATE)
    }

    override fun onStart() {
        super.onStart()

        val btn = findViewById<Button>(R.id.signUpSubmitBtn)
        btn.setOnClickListener(signUpSubmit)
    }

    private fun makeToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    private val signUpSubmit = OnClickListener { view ->
        if (view !is Button) return@OnClickListener
        view.isEnabled = false

        val emailInputLayout =  findViewById<TextInputLayout>(R.id.signUpEmail)
        val passwordInputLayout =  findViewById<TextInputLayout>(R.id.signUpPwd)
        val consentCheckBox =  findViewById<CheckBox>(R.id.signUpConsent)

        val email = emailInputLayout.editText?.text.toString()
        val password = passwordInputLayout.editText?.text.toString()
        val consentChecked = consentCheckBox.isChecked

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

        if (password.length < 8) {
            passwordInputLayout.error = getString(R.string.password_too_short)
            hasErrors = true
        }
        else passwordInputLayout.error = null

        if (!consentChecked) {
            consentCheckBox.error = getString(R.string.consent_not_checked)
            hasErrors = true
        }
        else consentCheckBox.error = null

        if (hasErrors) {
            view.isEnabled = true
            return@OnClickListener
        }

        runBlocking {
            launch {
                try {
                    val user = AuthRepository.signUp(email, password)
                    if (user == null) throw Exception("User cannot be null")
                    UserRepository.createUser(user.id)

                    makeToast(getString(R.string.confirmation_email_sent_at) + " " + email)
                    finish()

                } catch (ex: Exception) {
                    Log.e("SIGNUP", ex.message.toString())
                    val err = findViewById<TextView>(R.id.signUpErrorMsg)

                    err.visibility = View.VISIBLE
                    view.isEnabled = true

                    if (ex is RestException && ex.error == "All object keys must match") {
                        err.text = getString(R.string.user_with_email_already_exist)
                        view.error = getString(R.string.user_with_email_already_exist)
                    } else if (ex is HttpRequestException || ex is HttpRequestTimeoutException) {
                        err.text = getString(R.string.internet_connection_error)
                        view.error = getString(R.string.internet_connection_error)
                    } else {
                        err.text = getString(R.string.unknown_error)
                        view.error = getString(R.string.unknown_error)
                    }
                }
            }
        }
    }
}