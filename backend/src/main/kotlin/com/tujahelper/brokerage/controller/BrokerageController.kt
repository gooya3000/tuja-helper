package com.tujahelper.brokerage.controller

import com.tujahelper.brokerage.dto.SaveCredentialsRequest
import com.tujahelper.brokerage.service.BrokerageService
import com.tujahelper.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "증권사 API 키", description = "한투 API 키 등록 관련 API")
@RestController
@RequestMapping("/api/v1/brokerage")
class BrokerageController(
    private val brokerageService: BrokerageService,
) {
    @Operation(summary = "API 키 등록", description = "한국투자증권 API Key/Secret/계좌번호를 등록한다")
    @PostMapping("/credentials")
    fun saveCredentials(
        @Valid @RequestBody request: SaveCredentialsRequest,
    ): ResponseEntity<ApiResponse<Unit>> {
        val userId = SecurityContextHolder.getContext().authentication.principal as Long
        brokerageService.saveCredentials(userId, request.appKey, request.appSecret, request.accountNo)
        return ResponseEntity.ok(ApiResponse.ok())
    }
}
