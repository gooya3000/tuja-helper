package com.tujahelper.auth.service

import com.tujahelper.auth.domain.User
import com.tujahelper.auth.dto.LoginRequest
import com.tujahelper.auth.dto.SignupRequest
import com.tujahelper.auth.repository.UserRepository
import com.tujahelper.common.TujaException
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockKExtension::class)
class AuthServiceTest {
    private val userRepository: UserRepository = mockk()
    private val jwtProvider: JwtProvider = mockk()
    private val redisRefreshTokenRepository: RedisRefreshTokenRepository = mockk()

    // 단위 테스트에서 BCrypt 해시 의존성을 없애기 위해 PasswordEncoder를 mock으로 주입
    private val passwordEncoder: PasswordEncoder = mockk()
    private val authService = AuthService(userRepository, jwtProvider, redisRefreshTokenRepository, passwordEncoder)

    // -------------------------------------------------------------------------
    // 회원가입 테스트
    // -------------------------------------------------------------------------

    @Test
    fun `signup_정상_요청_userId_반환`() {
        val request = SignupRequest(email = "test@example.com", password = "password123")
        val savedUser = User(id = 1L, email = "test@example.com", password = "hashed_password")

        every { userRepository.existsByEmail("test@example.com") } returns false
        every { passwordEncoder.encode("password123") } returns "hashed_password"
        every { userRepository.save(any()) } returns savedUser

        val result = authService.signup(request)

        assertThat(result.userId).isEqualTo(1L)
    }

    @Test
    fun `signup_이미_존재하는_이메일_DUPLICATE_EMAIL_409`() {
        val request = SignupRequest(email = "duplicate@example.com", password = "password123")

        every { userRepository.existsByEmail("duplicate@example.com") } returns true

        assertThatThrownBy { authService.signup(request) }
            .isInstanceOf(TujaException::class.java)
            .satisfies({ ex ->
                val tujaEx = ex as TujaException
                assertThat(tujaEx.code).isEqualTo("DUPLICATE_EMAIL")
                assertThat(tujaEx.status).isEqualTo(HttpStatus.CONFLICT)
            })
    }

    // -------------------------------------------------------------------------
    // 로그인 테스트
    // -------------------------------------------------------------------------

    @Test
    fun `login_정상_요청_accessToken_refreshToken_반환`() {
        val request = LoginRequest(email = "test@example.com", password = "password123")
        val user = User(id = 1L, email = "test@example.com", password = "hashed_password")

        every { userRepository.findByEmail("test@example.com") } returns user
        every { passwordEncoder.matches("password123", "hashed_password") } returns true
        every { jwtProvider.createAccessToken(1L) } returns "access.token.value"
        every { jwtProvider.createRefreshToken(1L) } returns "refresh.token.value"
        every { redisRefreshTokenRepository.save(1L, "refresh.token.value") } returns Unit

        val result = authService.login(request)

        assertThat(result.accessToken).isNotBlank()
        assertThat(result.refreshToken).isNotBlank()
    }

    @Test
    fun `login_잘못된_비밀번호_INVALID_CREDENTIALS_401`() {
        val request = LoginRequest(email = "test@example.com", password = "wrong_password")
        val user = User(id = 1L, email = "test@example.com", password = "hashed_correct_password")

        every { userRepository.findByEmail("test@example.com") } returns user
        every { passwordEncoder.matches("wrong_password", "hashed_correct_password") } returns false

        assertThatThrownBy { authService.login(request) }
            .isInstanceOf(TujaException::class.java)
            .satisfies({ ex ->
                val tujaEx = ex as TujaException
                assertThat(tujaEx.code).isEqualTo("INVALID_CREDENTIALS")
                assertThat(tujaEx.status).isEqualTo(HttpStatus.UNAUTHORIZED)
            })
    }

    @Test
    fun `login_존재하지_않는_이메일_INVALID_CREDENTIALS_401`() {
        val request = LoginRequest(email = "notexist@example.com", password = "password123")

        every { userRepository.findByEmail("notexist@example.com") } returns null

        assertThatThrownBy { authService.login(request) }
            .isInstanceOf(TujaException::class.java)
            .satisfies({ ex ->
                val tujaEx = ex as TujaException
                assertThat(tujaEx.code).isEqualTo("INVALID_CREDENTIALS")
                assertThat(tujaEx.status).isEqualTo(HttpStatus.UNAUTHORIZED)
            })
    }

    // -------------------------------------------------------------------------
    // 토큰 갱신 테스트
    // -------------------------------------------------------------------------

    @Test
    fun `refresh_유효한_refreshToken_새_accessToken_반환`() {
        val validRefreshToken = "valid.refresh.token"

        every { jwtProvider.validateToken(validRefreshToken) } returns TokenValidationResult.VALID
        every { jwtProvider.getUserIdFromToken(validRefreshToken) } returns 1L
        every { redisRefreshTokenRepository.findByUserId(1L) } returns validRefreshToken
        every { jwtProvider.createAccessToken(1L) } returns "new.access.token"

        val result = authService.refresh(validRefreshToken)

        assertThat(result.accessToken).isEqualTo("new.access.token")
    }

    @Test
    fun `refresh_유효하지_않은_refreshToken_INVALID_TOKEN_401`() {
        val invalidToken = "invalid.refresh.token"

        every { jwtProvider.validateToken(invalidToken) } returns TokenValidationResult.INVALID

        assertThatThrownBy { authService.refresh(invalidToken) }
            .isInstanceOf(TujaException::class.java)
            .satisfies({ ex ->
                val tujaEx = ex as TujaException
                assertThat(tujaEx.code).isEqualTo("INVALID_TOKEN")
                assertThat(tujaEx.status).isEqualTo(HttpStatus.UNAUTHORIZED)
            })
    }

    @Test
    fun `refresh_만료된_refreshToken_EXPIRED_TOKEN_401`() {
        val expiredToken = "expired.refresh.token"

        every { jwtProvider.validateToken(expiredToken) } returns TokenValidationResult.EXPIRED

        assertThatThrownBy { authService.refresh(expiredToken) }
            .isInstanceOf(TujaException::class.java)
            .satisfies({ ex ->
                val tujaEx = ex as TujaException
                assertThat(tujaEx.code).isEqualTo("EXPIRED_TOKEN")
                assertThat(tujaEx.status).isEqualTo(HttpStatus.UNAUTHORIZED)
            })
    }
}
