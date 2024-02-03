package com.example.mashop.db.user

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User (
    val id: String,

    @SerialName("first_name")
    val firstName: String?,

    @SerialName("last_name")
    val lastName: String?,

    val country: String?,

    val city: String?,

    @SerialName("zip_code")
    val zipCode: String?,

    val address: String?,

    @SerialName("created_at")
    val createdAt: Instant
)