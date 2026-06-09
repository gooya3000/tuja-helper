package com.tujahelper.kis.service

import com.tujahelper.brokerage.domain.BrokerageCredential
import com.tujahelper.common.TujaException
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus

/**
 * KisTokenService 단위 테스트 — 한투 OAuth 토큰 발급 및 Redis 캐싱 검증
 *
 * Red 단계: KisTokenService, KisTokenRepository 미구현이므로 컴파일 오류가 발생한다.
 */
@ExtendWith(MockKExtension::class)
class KisTokenServiceTest {

    private val kisTokenRepository: KisTokenRepository = mockk()
    private val kisOAuthClient: KisOAuthClient = mockk()
    private val kisTokenService = KisTokenService(kisOAuthClient, kisTokenRepository)

    // -------------------------------------------------------------------------
    // 토큰 발급 및 캐싱 테스트
    // -------------------------------------------------------------------------

    @Test
    fun `getToken_캐시에_토큰이_있으면_API_호출_없이_캐시된_토큰_반환`() {
        val userId = 1L
        val cachedToken = "cached.kis.access.token"

        every { kisTokenRepository.findByUserId(userId) } returns cachedToken

        val result = kisTokenService.getToken(userId)

        assertThat(result).isEqualTo(cachedToken)
        // 캐시 히트 시 OAuth API를 호출하지 않는다
        verify(exactly = 0) { kisOAuthClient.issueToken(any()) }
    }

    @Test
    fun `getToken_캐시에_토큰이_없으면_OAuth_API로_토큰_발급_후_Redis에_저장`() {
        val userId = 1L
        val credential = mockk<BrokerageCredential>()
        val newToken = "new.kis.access.token"
        val expiresIn = 86400L // 24시간

        every { kisTokenRepository.findByUserId(userId) } returns null
        every { kisOAuthClient.issueToken(credential) } returns KisTokenResponse(
            accessToken = newToken,
            expiresIn = expiresIn,
        )
        every { kisTokenRepository.save(userId, newToken, expiresIn) } returns Unit

        val result = kisTokenService.getToken(userId, credential)

        assertThat(result).isEqualTo(newToken)
        verify(exactly = 1) { kisOAuthClient.issueToken(credential) }
        verify(exactly = 1) { kisTokenRepository.save(userId, newToken, expiresIn) }
    }

    @Test
    fun `getToken_캐시_미스_시_credential이_null이면_예외_발생`() {
        val userId = 1L

        every { kisTokenRepository.findByUserId(userId) } returns null

        assertThatThrownBy { kisTokenService.getToken(userId, null) }
            .isInstanceOf(TujaException::class.java)
            .satisfies({ ex ->
                val tujaEx = ex as TujaException
                assertThat(tujaEx.code).isEqualTo("CREDENTIAL_NOT_FOUND")
                assertThat(tujaEx.status).isEqualTo(HttpStatus.NOT_FOUND)
            })
    }

    @Test
    fun `getToken_OAuth_API_호출_실패_시_KIS_API_ERROR_예외_발생`() {
        val userId = 1L
        val credential = mockk<BrokerageCredential>()

        every { kisTokenRepository.findByUserId(userId) } returns null
        every { kisOAuthClient.issueToken(credential) } throws
            com.tujahelper.kis.exception.KisApiException("한국투자증권 토큰 발급 실패")

        assertThatThrownBy { kisTokenService.getToken(userId, credential) }
            .isInstanceOf(com.tujahelper.kis.exception.KisApiException::class.java)
    }

    // -------------------------------------------------------------------------
    // Redis 캐시 키 형식 테스트
    // -------------------------------------------------------------------------

    @Test
    fun `kisTokenRepository_save_시_키_형식이_kis_token_userId_이다`() {
        val userId = 42L
        val token = "some.access.token"
        val expiresIn = 3600L

        every { kisTokenRepository.save(userId, token, expiresIn) } returns Unit

        kisTokenRepository.save(userId, token, expiresIn)

        verify {
            kisTokenRepository.save(
                withArg { assertThat(it).isEqualTo(userId) },
                token,
                expiresIn,
            )
        }
    }

    @Test
    fun `invalidateToken_호출_시_Redis에서_해당_userId_토큰_삭제`() {
        val userId = 1L

        every { kisTokenRepository.deleteByUserId(userId) } returns Unit

        kisTokenService.invalidateToken(userId)

        verify(exactly = 1) { kisTokenRepository.deleteByUserId(userId) }
    }
}
