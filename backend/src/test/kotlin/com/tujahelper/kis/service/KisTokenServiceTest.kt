package com.tujahelper.kis.service

import com.tujahelper.kis.client.KisOAuthClient
import com.tujahelper.kis.dto.KisTokenResponse
import com.tujahelper.kis.repository.KisTokenRepository
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class KisTokenServiceTest {
    private val kisOAuthClient: KisOAuthClient = mockk()
    private val kisTokenRepository: KisTokenRepository = mockk()
    private val kisTokenService = KisTokenService(kisOAuthClient, kisTokenRepository)

    @Test
    fun `getToken_캐시에_토큰이_있는_경우_캐시_반환`() {
        val userId = 1L
        val cachedToken = "cached-access-token"

        every { kisTokenRepository.findByUserId(userId) } returns cachedToken

        val result = kisTokenService.getToken(userId, "app-key", "app-secret")

        assertThat(result).isEqualTo(cachedToken)
        verify(exactly = 0) { kisOAuthClient.issueToken(any(), any()) }
    }

    @Test
    fun `getToken_캐시에_없는_경우_새_토큰_발급_및_저장`() {
        val userId = 1L
        val newToken = "new-access-token"
        val expiresIn = 86400L // 1일

        every { kisTokenRepository.findByUserId(userId) } returns null
        every { kisOAuthClient.issueToken("app-key", "app-secret") } returns
            KisTokenResponse(
                accessToken = newToken,
                tokenType = "Bearer",
                expiresIn = expiresIn,
                accessTokenTokenExpired = "2025-01-01 00:00:00",
            )
        every { kisTokenRepository.save(userId, newToken, expiresIn) } returns Unit

        val result = kisTokenService.getToken(userId, "app-key", "app-secret")

        assertThat(result).isEqualTo(newToken)
        verify { kisOAuthClient.issueToken("app-key", "app-secret") }
        verify { kisTokenRepository.save(userId, newToken, expiresIn) }
    }
}
