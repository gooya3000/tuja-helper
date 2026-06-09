package com.tujahelper.account.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.tujahelper.auth.dto.LoginRequest
import com.tujahelper.auth.dto.SignupRequest
import com.tujahelper.config.EmbeddedRedisConfig
import com.tujahelper.kis.service.KisApiClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

/**
 * GET /api/v1/accounts, GET /api/v1/accounts/{accountNo}/balance 컨트롤러 통합 테스트
 *
 * Red 단계: AccountController 미구현이므로 404 또는 컴파일 오류가 발생한다.
 * 한투 API 실제 호출은 KisApiClient를 @MockBean으로 격리한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(EmbeddedRedisConfig::class)
class AccountControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    // 한투 API 실제 호출 격리
    @MockBean
    lateinit var kisApiClient: KisApiClient

    private var accessToken: String = ""
    private val testAccountNo = "12345678-01"

    @BeforeEach
    fun setUp() {
        val email = "account_test_${System.currentTimeMillis()}@example.com"
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

        // 계좌 정보 사전 등록
        val credentialRequest = mapOf(
            "appKey" to "KISDevKey1234567890",
            "appSecret" to "KISDevSecret1234567890ABCDEFGHIJ",
            "accountNo" to testAccountNo,
        )
        mockMvc.post("/api/v1/brokerage/credentials") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(credentialRequest)
            header("Authorization", "Bearer $accessToken")
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/accounts
    // -------------------------------------------------------------------------

    @Test
    fun `GET accounts_정상_요청_200_계좌_목록_반환`() {
        mockMvc.get("/api/v1/accounts") {
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.accounts") { isArray() }
        }
    }

    @Test
    fun `GET accounts_인증_없이_요청_401`() {
        mockMvc.get("/api/v1/accounts")
            .andExpect {
                status { isUnauthorized() }
            }
    }

    @Test
    fun `GET accounts_등록된_계좌_목록에_accountNo와_accountName_포함`() {
        mockMvc.get("/api/v1/accounts") {
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.accounts[0].accountNo") { exists() }
            jsonPath("$.data.accounts[0].accountName") { exists() }
        }
    }

    @Test
    fun `GET accounts_크리덴셜_미등록_사용자_빈_배열_반환`() {
        // 크리덴셜을 등록하지 않은 새 사용자로 요청
        val email = "no_credential_${System.currentTimeMillis()}@example.com"
        mockMvc.post("/api/v1/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                SignupRequest(email = email, password = "password123")
            )
        }
        val loginResult = mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                LoginRequest(email = email, password = "password123")
            )
        }.andReturn()
        val body = objectMapper.readTree(loginResult.response.contentAsString)
        val newAccessToken = body["data"]["accessToken"].asText()

        mockMvc.get("/api/v1/accounts") {
            header("Authorization", "Bearer $newAccessToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.accounts") { isArray() }
            jsonPath("$.data.accounts.length()") { value(0) }
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/accounts/{accountNo}/balance
    // -------------------------------------------------------------------------

    @Test
    fun `GET accounts accountNo balance_정상_요청_200_잔고_반환`() {
        // KisApiClient가 모의 잔고 데이터를 반환하도록 설정
        Mockito.`when`(kisApiClient.getBalance(any(), any())).thenReturn(
            mapOf(
                "totalEvaluationAmount" to "10000000",
                "depositAmount" to "5000000",
                "holdings" to emptyList<Any>(),
            )
        )

        mockMvc.get("/api/v1/accounts/$testAccountNo/balance") {
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.totalEvaluationAmount") { exists() }
            jsonPath("$.data.depositAmount") { exists() }
            jsonPath("$.data.holdings") { isArray() }
        }
    }

    @Test
    fun `GET accounts accountNo balance_인증_없이_요청_401`() {
        mockMvc.get("/api/v1/accounts/$testAccountNo/balance")
            .andExpect {
                status { isUnauthorized() }
            }
    }

    @Test
    fun `GET accounts accountNo balance_크리덴셜_없는_계좌번호_404_CREDENTIAL_NOT_FOUND`() {
        mockMvc.get("/api/v1/accounts/99999999-99/balance") {
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.success") { value(false) }
            jsonPath("$.error.code") { value("CREDENTIAL_NOT_FOUND") }
        }
    }

    @Test
    fun `GET accounts accountNo balance_한투_API_오류_502_KIS_API_ERROR`() {
        Mockito.`when`(kisApiClient.getBalance(any(), any())).thenThrow(
            com.tujahelper.kis.exception.KisApiException("한국투자증권 API 오류가 발생했습니다")
        )

        mockMvc.get("/api/v1/accounts/$testAccountNo/balance") {
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isBadGateway() }
            jsonPath("$.success") { value(false) }
            jsonPath("$.error.code") { value("KIS_API_ERROR") }
        }
    }

    // 편의 함수: Mockito any() 매처 null-safe 래퍼
    private fun <T> any(): T {
        Mockito.any<T>()
        @Suppress("UNCHECKED_CAST")
        return null as T
    }
}
