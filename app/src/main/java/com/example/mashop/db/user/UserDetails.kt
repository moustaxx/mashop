package com.example.mashop.db.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDetails(
    @SerialName("first_name")
    val firstName: String?,

    @SerialName("last_name")
    val lastName: String?,

    val country: String?,

    val city: String?,

    @SerialName("zip_code")
    val zipCode: String?,

    val address: String?,
)