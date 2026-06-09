package com.tujahelper.account.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.tujahelper.account.dto.AccountDto
import com.tujahelper.account.dto.BalanceDto
import com.tujahelper.account.service.AccountService
import com.tujahelper.auth.service.JwtProvider
import com.tujahelper.config.EmbeddedRedisConfig
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.math.BigDecimal

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(EmbeddedRedisConfig::class)
class AccountControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var jwtProvider: JwtProvider

    @MockkBean
    lateinit var accountService: AccountService

    // -------------------------------------------------------------------------
    // GET /api/v1/accounts
    // -------------------------------------------------------------------------

    @Test
    fun `GET accounts_정상_요청_계좌목록_반환`() {
        val userId = 1L
        val accessToken = jwtProvider.createAccessToken(userId)

        every { accountService.getAccounts(userId) } returns
            listOf(
                AccountDto(accountNo = "12345678901234", accountName = "종합계좌"),
            )

        mockMvc.get("/api/v1/accounts") {
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data") { isArray() }
            jsonPath("$.data[0].accountNo") { value("12345678901234") }
        }
    }

    @Test
    fun `GET accounts_토큰_없이_접근_401`() {
        mockMvc.get("/api/v1/accounts")
            .andExpect {
                status { isUnauthorized() }
            }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/accounts/{accountNo}/balance
    // -------------------------------------------------------------------------

    @Test
    fun `GET accounts_accountNo_balance_정상_요청_잔고_반환`() {
        val userId = 1L
        val accountNo = "12345678901234"
        val accessToken = jwtProvider.createAccessToken(userId)

        every { accountService.getBalance(userId, accountNo) } returns
            BalanceDto(
                totalEvaluationAmount = BigDecimal("10000000"),
                depositAmount = BigDecimal("5000000"),
                totalProfitLossAmount = BigDecimal("500000"),
                totalProfitLossRate = BigDecimal("5.26"),
            )

        mockMvc.get("/api/v1/accounts/$accountNo/balance") {
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.totalEvaluationAmount") { exists() }
            jsonPath("$.data.depositAmount") { exists() }
        }
    }

    @Test
    fun `GET accounts_accountNo_balance_토큰_없이_접근_401`() {
        mockMvc.get("/api/v1/accounts/12345678901234/balance")
            .andExpect {
                status { isUnauthorized() }
            }
    }
}
