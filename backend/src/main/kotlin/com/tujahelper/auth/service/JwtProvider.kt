package com.tujahelper.auth.service

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtProvider(
    @Value("\${app.jwt.secret}") private val secret: String,
    @Value("\${app.jwt.access-token-expiry-ms}") private val accessTokenExpiryMs: Long,
    @Value("\${app.jwt.refresh-token-expiry-ms}") private val refreshTokenExpiryMs: Long,
) {
    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    fun createAccessToken(userId: Long): String {
        val now = Date()
        return Jwts.builder()
            .subject(userId.toString())
            .claim("type", "access")
            .issuedAt(now)
            .expiration(Date(now.time + accessTokenExpiryMs))
            .signWith(secretKey)
            .compact()
    }

    fun createRefreshToken(userId: Long): String {
        val now = Date()
        return Jwts.builder()
            .subject(userId.toString())
            .claim("type", "refresh")
            .issuedAt(now)
            .expiration(Date(now.time + refreshTokenExpiryMs))
            .signWith(secretKey)
            .compact()
    }

    fun validateToken(token: String): TokenValidationResult {
        return try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token)
            TokenValidationResult.VALID
        } catch (e: ExpiredJwtException) {
            TokenValidationResult.EXPIRED
        } catch (e: JwtException) {
            TokenValidationResult.INVALID
        } catch (e: Exception) {
            TokenValidationResult.INVALID
        }
    }

    fun getUserIdFromToken(token: String): Long {
        val claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
        return claims.subject.toLong()
    }
}
