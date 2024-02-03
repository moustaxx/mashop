package com.example.mashop.db.auth

import android.util.Log
import com.example.mashop.db.supabase
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email

class AuthRepository {
    companion object {
        suspend fun signIn(email: String, password: String) {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
        }

        suspend fun signUp(email: String, password: String): Email.Result? {
            try {
                return supabase.auth.signUpWith(
                    Email,
                    redirectUrl = "https://mashop-auth.netlify.app/"
                ) {
                    this.email = email
                    this.password = password
                }
            } catch (e: Exception) {
                Log.e("ERR", e.toString())
            }
            return null
        }
    }
}