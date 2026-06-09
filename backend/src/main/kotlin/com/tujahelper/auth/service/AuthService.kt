package com.tujahelper.auth.service

import com.tujahelper.auth.domain.User
import com.tujahelper.auth.dto.LoginRequest
import com.tujahelper.auth.dto.LoginResponse
import com.tujahelper.auth.dto.RefreshResponse
import com.tujahelper.auth.dto.SignupRequest
import com.tujahelper.auth.dto.SignupResponse
import com.tujahelper.auth.repository.UserRepository
import com.tujahelper.common.TujaException
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider,
    private val redisRefreshTokenRepository: RedisRefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun signup(request: SignupRequest): SignupResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw TujaException("DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다", HttpStatus.CONFLICT)
        }
        val user = User(email = request.email, password = passwordEncoder.encode(request.password))
        val saved = userRepository.save(user)
        return SignupResponse(saved.id)
    }

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): LoginResponse {
        val user =
            userRepository.findByEmail(request.email)
                ?: throw TujaException("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다", HttpStatus.UNAUTHORIZED)
        if (!passwordEncoder.matches(request.password, user.password)) {
            throw TujaException("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다", HttpStatus.UNAUTHORIZED)
        }
        val accessToken = jwtProvider.createAccessToken(user.id)
        val refreshToken = jwtProvider.createRefreshToken(user.id)
        redisRefreshTokenRepository.save(user.id, refreshToken)
        return LoginResponse(accessToken, refreshToken)
    }

    fun refresh(refreshToken: String): RefreshResponse {
        when (jwtProvider.validateToken(refreshToken)) {
            TokenValidationResult.EXPIRED -> throw TujaException("EXPIRED_TOKEN", "만료된 토큰입니다", HttpStatus.UNAUTHORIZED)
            TokenValidationResult.INVALID -> throw TujaException("INVALID_TOKEN", "유효하지 않은 토큰입니다", HttpStatus.UNAUTHORIZED)
            TokenValidationResult.VALID -> {}
        }
        val userId = jwtProvider.getUserIdFromToken(refreshToken)
        val stored = redisRefreshTokenRepository.findByUserId(userId)
        if (stored != refreshToken) {
            throw TujaException("INVALID_TOKEN", "유효하지 않은 토큰입니다", HttpStatus.UNAUTHORIZED)
        }
        return RefreshResponse(jwtProvider.createAccessToken(userId))
    }
}
