package com.tujahelper.auth.controller

import com.tujahelper.auth.dto.LoginRequest
import com.tujahelper.auth.dto.LoginResponse
import com.tujahelper.auth.dto.RefreshRequest
import com.tujahelper.auth.dto.RefreshResponse
import com.tujahelper.auth.dto.SignupRequest
import com.tujahelper.auth.dto.SignupResponse
import com.tujahelper.auth.service.AuthService
import com.tujahelper.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "인증", description = "회원가입, 로그인, 토큰 갱신 API")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {
    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    fun signup(
        @Valid @RequestBody request: SignupRequest,
    ): ResponseEntity<ApiResponse<SignupResponse>> =
        ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(authService.signup(request)))

    @Operation(summary = "로그인")
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ResponseEntity<ApiResponse<LoginResponse>> =
        ResponseEntity.ok(ApiResponse.ok(authService.login(request)))

    @Operation(summary = "토큰 갱신")
    @PostMapping("/refresh")
    fun refresh(
        @RequestBody request: RefreshRequest,
    ): ResponseEntity<ApiResponse<RefreshResponse>> =
        ResponseEntity.ok(ApiResponse.ok(authService.refresh(request.refreshToken)))
}
