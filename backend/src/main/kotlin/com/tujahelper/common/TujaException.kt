package com.tujahelper.common

import org.springframework.http.HttpStatus

class TujaException(
    val code: String,
    override val message: String,
    val status: HttpStatus = HttpStatus.BAD_REQUEST,
) : RuntimeException(message) {
    companion object {
        fun unauthorized(message: String = "인증이 필요합니다.") = TujaException("AUTH_001", message, HttpStatus.UNAUTHORIZED)

        fun forbidden(message: String = "권한이 없습니다.") = TujaException("AUTH_002", message, HttpStatus.FORBIDDEN)

        fun notFound(message: String) = TujaException("NOT_FOUND", message, HttpStatus.NOT_FOUND)

        fun conflict(message: String) = TujaException("CONFLICT", message, HttpStatus.CONFLICT)

        fun badRequest(
            code: String,
            message: String,
        ) = TujaException(code, message, HttpStatus.BAD_REQUEST)
    }
}
