package com.example.mashop.db.user

import com.example.mashop.db.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.serialization.Serializable

class UserRepository {
    companion object {
        @Serializable
        class CreateUserInput(
            val id: String
        )

        suspend fun getUserById(id: String): User? {
            return supabase.from("user").select {
                filter {
                    User::id eq id
                }
            }.decodeSingleOrNull()
        }

        suspend fun createUser(id: String) {
            supabase.from("user").insert(CreateUserInput(id))
        }

        suspend fun fillUserDetails(userId: String, userDetails: UserDetails): PostgrestResult {
            return supabase.from("user").update({
                User::firstName setTo userDetails.firstName
                User::lastName setTo userDetails.lastName
                User::country setTo userDetails.country
                User::city setTo userDetails.city
                User::zipCode setTo userDetails.zipCode
                User::address setTo userDetails.address
            }) {
                filter {
                    User::id eq userId
                }
            }
        }
    }
}