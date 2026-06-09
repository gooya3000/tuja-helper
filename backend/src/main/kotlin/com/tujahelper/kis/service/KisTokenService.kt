package com.tujahelper.kis.service

import com.tujahelper.kis.client.KisOAuthClient
import com.tujahelper.kis.repository.KisTokenRepository
import org.springframework.stereotype.Service

@Service
class KisTokenService(
    private val kisOAuthClient: KisOAuthClient,
    private val kisTokenRepository: KisTokenRepository,
) {
    fun getToken(
        userId: Long,
        appKey: String,
        appSecret: String,
    ): String {
        // 캐시에 토큰이 있으면 바로 반환
        val cached = kisTokenRepository.findByUserId(userId)
        if (cached != null) return cached

        // 없으면 새 토큰 발급 후 저장
        val tokenResponse = kisOAuthClient.issueToken(appKey, appSecret)
        kisTokenRepository.save(userId, tokenResponse.accessToken, tokenResponse.expiresIn)
        return tokenResponse.accessToken
    }
}
