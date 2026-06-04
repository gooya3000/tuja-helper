package com.tujahelper.auth.dto

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
)
