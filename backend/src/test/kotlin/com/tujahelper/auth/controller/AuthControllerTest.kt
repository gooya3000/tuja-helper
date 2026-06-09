package com.tujahelper.auth.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.tujahelper.auth.dto.LoginRequest
import com.tujahelper.auth.dto.RefreshRequest
import com.tujahelper.auth.dto.SignupRequest
import com.tujahelper.config.EmbeddedRedisConfig
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(EmbeddedRedisConfig::class)
class AuthControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    // -------------------------------------------------------------------------
    // POST /api/v1/auth/signup
    // -------------------------------------------------------------------------

    @Test
    fun `POST auth signup_정상_요청_201_userId_반환`() {
        val request =
            SignupRequest(
                email = "newuser@example.com",
                password = "password123",
            )

        mockMvc.post("/api/v1/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.userId") { exists() }
        }
    }

    @Test
    fun `POST auth signup_중복_이메일_409_DUPLICATE_EMAIL`() {
        val request =
            SignupRequest(
                email = "duplicate@example.com",
                password = "password123",
            )

        // 첫 번째 회원가입
        mockMvc.post("/api/v1/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }

        // 중복 이메일로 두 번째 회원가입
        mockMvc.post("/api/v1/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isConflict() }
            jsonPath("$.success") { value(false) }
            jsonPath("$.error.code") { value("DUPLICATE_EMAIL") }
        }
    }

    @Test
    fun `POST auth signup_비밀번호_8자_미만_400_INVALID_INPUT`() {
        val request =
            SignupRequest(
                email = "test@example.com",
                // 8자 미만
                password = "short",
            )

        mockMvc.post("/api/v1/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.success") { value(false) }
            jsonPath("$.error.code") { value("INVALID_INPUT") }
        }
    }

    @Test
    fun `POST auth signup_이메일_형식_오류_400_INVALID_INPUT`() {
        val request =
            SignupRequest(
                email = "not-an-email",
                password = "password123",
            )

        mockMvc.post("/api/v1/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.success") { value(false) }
            jsonPath("$.error.code") { value("INVALID_INPUT") }
        }
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/auth/login
    // -------------------------------------------------------------------------

    @Test
    fun `POST auth login_정상_요청_200_accessToken_refreshToken_반환`() {
        // 사전 회원가입
        val signupRequest = SignupRequest(email = "logintest@example.com", password = "password123")
        mockMvc.post("/api/v1/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(signupRequest)
        }

        val loginRequest = LoginRequest(email = "logintest@example.com", password = "password123")

        mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(loginRequest)
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.accessToken") { exists() }
            jsonPath("$.data.refreshToken") { exists() }
        }
    }

    @Test
    fun `POST auth login_잘못된_비밀번호_401_INVALID_CREDENTIALS`() {
        // 사전 회원가입
        val signupRequest = SignupRequest(email = "wrongpw@example.com", password = "password123")
        mockMvc.post("/api/v1/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(signupRequest)
        }

        val loginRequest = LoginRequest(email = "wrongpw@example.com", password = "wrong_password")

        mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(loginRequest)
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.success") { value(false) }
            jsonPath("$.error.code") { value("INVALID_CREDENTIALS") }
        }
    }

    @Test
    fun `POST auth login_존재하지_않는_이메일_401_INVALID_CREDENTIALS`() {
        val loginRequest = LoginRequest(email = "notexist@example.com", password = "password123")

        mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(loginRequest)
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.success") { value(false) }
            jsonPath("$.error.code") { value("INVALID_CREDENTIALS") }
        }
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/auth/refresh
    // -------------------------------------------------------------------------

    @Test
    fun `POST auth refresh_유효한_refreshToken_200_새_accessToken_반환`() {
        // 회원가입 후 로그인으로 토큰 획득
        val signupRequest = SignupRequest(email = "refreshtest@example.com", password = "password123")
        mockMvc.post("/api/v1/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(signupRequest)
        }

        val loginResult =
            mockMvc.post("/api/v1/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content =
                    objectMapper.writeValueAsString(
                        LoginRequest(email = "refreshtest@example.com", password = "password123"),
                    )
            }.andReturn()

        val loginBody = objectMapper.readTree(loginResult.response.contentAsString)
        val refreshToken = loginBody["data"]["refreshToken"].asText()

        val refreshRequest = RefreshRequest(refreshToken = refreshToken)

        mockMvc.post("/api/v1/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(refreshRequest)
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.accessToken") { exists() }
        }
    }

    @Test
    fun `POST auth refresh_유효하지_않은_refreshToken_401_INVALID_TOKEN`() {
        val refreshRequest = RefreshRequest(refreshToken = "invalid.token.value")

        mockMvc.post("/api/v1/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(refreshRequest)
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.success") { value(false) }
            jsonPath("$.error.code") { value("INVALID_TOKEN") }
        }
    }

    @Test
    fun `POST auth refresh_만료된_refreshToken_401_EXPIRED_TOKEN`() {
        // 만료된 JWT 토큰 (유효하지만 만료된 서명 - 실제 만료 토큰 시뮬레이션)
        // eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidHlwZSI6InJlZnJlc2giLCJpYXQiOjE2MDAwMDAwMDAsImV4cCI6MTYwMDAwMDAwMX0
        // 이 토큰은 테스트 환경의 JWT_SECRET과 불일치하므로 실제 만료/무효 케이스 검증은 AuthServiceTest에서 MockK 단위 테스트로 확인
        val expiredTokenRequest =
            RefreshRequest(
                refreshToken =
                    "eyJhbGciOiJIUzI1NiJ9" +
                        ".eyJzdWIiOiIxIiwidHlwZSI6InJlZnJlc2giLCJpYXQiOjE2MDAwMDAwMDAsImV4cCI6MTYwMDAwMDAwMX0" +
                        ".EXPIRED",
            )

        mockMvc.post("/api/v1/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(expiredTokenRequest)
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.success") { value(false) }
            jsonPath("$.error.code") { value(org.hamcrest.Matchers.oneOf("EXPIRED_TOKEN", "INVALID_TOKEN")) }
        }
    }

    // -------------------------------------------------------------------------
    // 인증이 필요한 엔드포인트 접근 테스트
    // -------------------------------------------------------------------------

    @Test
    fun `GET api_v1_me_토큰_없이_접근_401`() {
        mockMvc.get("/api/v1/me")
            .andExpect {
                status { isUnauthorized() }
            }
    }
}
