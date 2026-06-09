package com.tujahelper.kis.repository

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class KisTokenRepository(
    private val redisTemplate: StringRedisTemplate,
) {
    companion object {
        private const val KEY_PREFIX = "kis:token:"
    }

    fun findByUserId(userId: Long): String? {
        return redisTemplate.opsForValue().get("$KEY_PREFIX$userId")
    }

    fun save(
        userId: Long,
        accessToken: String,
        expiresIn: Long,
    ) {
        // expiresIn은 초 단위
        redisTemplate.opsForValue().set(
            "$KEY_PREFIX$userId",
            accessToken,
            Duration.ofSeconds(expiresIn),
        )
    }

    fun deleteByUserId(userId: Long) {
        redisTemplate.delete("$KEY_PREFIX$userId")
    }
}
