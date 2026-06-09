package com.tujahelper.kis.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class KisTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,

    @JsonProperty("token_type")
    val tokenType: String,

    @JsonProperty("expires_in")
    val expiresIn: Long,

    @JsonProperty("access_token_token_expired")
    val accessTokenTokenExpired: String,
)
