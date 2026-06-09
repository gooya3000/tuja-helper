package com.tujahelper.brokerage.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.tujahelper.auth.service.JwtProvider
import com.tujahelper.brokerage.dto.SaveCredentialsRequest
import com.tujahelper.brokerage.service.BrokerageService
import com.tujahelper.config.EmbeddedRedisConfig
import io.mockk.every
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
class BrokerageControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var jwtProvider: JwtProvider

    @MockkBean
    lateinit var brokerageService: BrokerageService

    // -------------------------------------------------------------------------
    // POST /api/v1/brokerage/credentials
    // -------------------------------------------------------------------------

    @Test
    fun `POST brokerage_credentials_정상_저장_200`() {
        val userId = 1L
        val accessToken = jwtProvider.createAccessToken(userId)

        every { brokerageService.saveCredentials(userId, any(), any(), any()) } returns
            com.tujahelper.brokerage.domain.BrokerageCredential(
                id = 1L, userId = userId,
                appKey = "enc-key", appSecret = "enc-secret", accountNo = "12345678901234",
            )

        val request =
            SaveCredentialsRequest(
                appKey = "my-app-key",
                appSecret = "my-app-secret",
                accountNo = "12345678901234",
            )

        mockMvc.post("/api/v1/brokerage/credentials") {
            header("Authorization", "Bearer $accessToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
        }
    }

    @Test
    fun `POST brokerage_credentials_토큰_없이_접근_401`() {
        val request =
            SaveCredentialsRequest(
                appKey = "my-app-key",
                appSecret = "my-app-secret",
                accountNo = "12345678901234",
            )

        mockMvc.post("/api/v1/brokerage/credentials") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `POST brokerage_credentials_appKey_빈값_400`() {
        val userId = 1L
        val accessToken = jwtProvider.createAccessToken(userId)

        val request =
            SaveCredentialsRequest(
                appKey = "",
                appSecret = "my-app-secret",
                accountNo = "12345678901234",
            )

        mockMvc.post("/api/v1/brokerage/credentials") {
            header("Authorization", "Bearer $accessToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.success") { value(false) }
        }
    }
}
