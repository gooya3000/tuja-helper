package com.tujahelper.kis.client

import com.tujahelper.kis.dto.KisTokenResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class KisOAuthClient(
    @Value("\${app.kis.base-url}") private val baseUrl: String,
) {
    private val webClient: WebClient = WebClient.builder()
        .baseUrl(baseUrl)
        .build()

    fun issueToken(appKey: String, appSecret: String): KisTokenResponse {
        val requestBody = mapOf(
            "grant_type" to "client_credentials",
            "appkey" to appKey,
            "appsecret" to appSecret,
        )

        return webClient.post()
            .uri("/oauth2/tokenP")
            .header("Content-Type", "application/json")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono<KisTokenResponse>()
            .block()
            ?: throw IllegalStateException("한투 OAuth 토큰 발급 실패")
    }
}
