package com.tujahelper.me

import com.tujahelper.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "사용자", description = "현재 사용자 정보 API")
@RestController
@RequestMapping("/api/v1")
class MeController {

    @Operation(summary = "내 정보 조회", security = [SecurityRequirement(name = "bearerAuth")])
    @GetMapping("/me")
    fun me(
        @AuthenticationPrincipal userId: Long,
    ): ResponseEntity<ApiResponse<Map<String, Long>>> =
        ResponseEntity.ok(ApiResponse.ok(mapOf("userId" to userId)))
}
