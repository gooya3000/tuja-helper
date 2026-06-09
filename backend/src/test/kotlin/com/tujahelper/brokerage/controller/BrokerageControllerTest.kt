package com.tujahelper.brokerage.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.tujahelper.auth.dto.LoginRequest
import com.tujahelper.auth.dto.SignupRequest
import com.tujahelper.config.EmbeddedRedisConfig
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

/**
 * POST /api/v1/brokerage/credentials 컨트롤러 통합 테스트
 *
 * Red 단계: BrokerageController 미구현이므로 404 또는 컴파일 오류가 발생한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(EmbeddedRedisConfig::class)
class BrokerageControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    private var accessToken: String = ""

    // 테스트 전 회원가입 + 로그인하여 Bearer 토큰 획득
    @BeforeEach
    fun setUp() {
        val email = "brokerage_test_${System.currentTimeMillis()}@example.com"
        val signupRequest = SignupRequest(email = email, password = "password123")
        mockMvc.post("/api/v1/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(signupRequest)
        }

        val loginResult = mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                LoginRequest(email = email, password = "password123")
            )
        }.andReturn()

        val body = objectMapper.readTree(loginResult.response.contentAsString)
        accessToken = body["data"]["accessToken"].asText()
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/brokerage/credentials
    // -------------------------------------------------------------------------

    @Test
    fun `POST brokerage credentials_정상_요청_201_id_반환`() {
        val request = mapOf(
            "appKey" to "KISDevKey1234567890",
            "appSecret" to "KISDevSecret1234567890ABCDEFGHIJ",
            "accountNo" to "12345678-01",
        )

        mockMvc.post("/api/v1/brokerage/credentials") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isCreated() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.id") { exists() }
        }
    }

    @Test
    fun `POST brokerage credentials_인증_없이_요청_401`() {
        val request = mapOf(
            "appKey" to "KISDevKey1234567890",
            "appSecret" to "KISDevSecret1234567890ABCDEFGHIJ",
            "accountNo" to "12345678-01",
        )

        mockMvc.post("/api/v1/brokerage/credentials") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
            // Authorization 헤더 미포함
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `POST brokerage credentials_중복_등록_409_DUPLICATE_CREDENTIAL`() {
        val request = mapOf(
            "appKey" to "KISDevKey1234567890",
            "appSecret" to "KISDevSecret1234567890ABCDEFGHIJ",
            "accountNo" to "12345678-01",
        )

        // 첫 번째 등록
        mockMvc.post("/api/v1/brokerage/credentials") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
            header("Authorization", "Bearer $accessToken")
        }

        // 중복 등록 시도
        mockMvc.post("/api/v1/brokerage/credentials") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isConflict() }
            jsonPath("$.success") { value(false) }
            jsonPath("$.error.code") { value("DUPLICATE_CREDENTIAL") }
        }
    }

    @Test
    fun `POST brokerage credentials_appKey_빈값_400_INVALID_INPUT`() {
        val request = mapOf(
            "appKey" to "",
            "appSecret" to "KISDevSecret1234567890ABCDEFGHIJ",
            "accountNo" to "12345678-01",
        )

        mockMvc.post("/api/v1/brokerage/credentials") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.success") { value(false) }
            jsonPath("$.error.code") { value("INVALID_INPUT") }
        }
    }

    @Test
    fun `POST brokerage credentials_appSecret_빈값_400_INVALID_INPUT`() {
        val request = mapOf(
            "appKey" to "KISDevKey1234567890",
            "appSecret" to "",
            "accountNo" to "12345678-01",
        )

        mockMvc.post("/api/v1/brokerage/credentials") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.success") { value(false) }
            jsonPath("$.error.code") { value("INVALID_INPUT") }
        }
    }

    @Test
    fun `POST brokerage credentials_accountNo_빈값_400_INVALID_INPUT`() {
        val request = mapOf(
            "appKey" to "KISDevKey1234567890",
            "appSecret" to "KISDevSecret1234567890ABCDEFGHIJ",
            "accountNo" to "",
        )

        mockMvc.post("/api/v1/brokerage/credentials") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.success") { value(false) }
            jsonPath("$.error.code") { value("INVALID_INPUT") }
        }
    }
}
