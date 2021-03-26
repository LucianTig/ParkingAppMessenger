package com.luciantig.parkingapp.models

import java.io.Serializable

data class User(
    val uid: String? = null,
    val username: String,
    val carNumber: String,
    val email: String,
    val token: String? = null
) : Serializable