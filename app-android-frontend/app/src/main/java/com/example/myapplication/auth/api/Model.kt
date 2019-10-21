package com.example.myapplication.auth.api

object Model {
    data class Session(val userId: String, val properties: Properties, val optional: Optional)
    data class Properties(val score: Int, val email: String)
    data class Optional(val userDescription: String, val dob: String, val phoneNumber: String)
}
