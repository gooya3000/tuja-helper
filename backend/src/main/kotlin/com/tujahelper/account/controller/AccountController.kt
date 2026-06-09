package com.tujahelper.account.controller

import com.tujahelper.account.dto.AccountDto
import com.tujahelper.account.dto.BalanceDto
import com.tujahelper.account.service.AccountService
import com.tujahelper.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "계좌", description = "계좌 목록 및 잔고 조회 API")
@RestController
@RequestMapping("/api/v1/accounts")
class AccountController(
    private val accountService: AccountService,
) {
    @Operation(summary = "계좌 목록 조회")
    @GetMapping
    fun getAccounts(): ResponseEntity<ApiResponse<List<AccountDto>>> {
        val userId = SecurityContextHolder.getContext().authentication.principal as Long
        return ResponseEntity.ok(ApiResponse.ok(accountService.getAccounts(userId)))
    }

    @Operation(summary = "잔고 조회")
    @GetMapping("/{accountNo}/balance")
    fun getBalance(
        @PathVariable accountNo: String,
    ): ResponseEntity<ApiResponse<BalanceDto>> {
        val userId = SecurityContextHolder.getContext().authentication.principal as Long
        return ResponseEntity.ok(ApiResponse.ok(accountService.getBalance(userId, accountNo)))
    }
}
