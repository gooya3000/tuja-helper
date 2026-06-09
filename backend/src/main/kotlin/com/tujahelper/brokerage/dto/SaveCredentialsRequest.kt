package com.tujahelper.brokerage.dto

import jakarta.validation.constraints.NotBlank

data class SaveCredentialsRequest(
    @field:NotBlank(message = "appKey는 필수입니다.")
    val appKey: String,

    @field:NotBlank(message = "appSecret은 필수입니다.")
    val appSecret: String,

    @field:NotBlank(message = "accountNo는 필수입니다.")
    val accountNo: String,
)
