package com.tujahelper.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

data class SignupRequest(
    @field:Email val email: String,
    @field:Size(min = 8) val password: String,
)
