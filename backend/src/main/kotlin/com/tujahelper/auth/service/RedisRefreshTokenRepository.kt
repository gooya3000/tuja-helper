package com.tujahelper.auth.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class RedisRefreshTokenRepository(
    private val redisTemplate: StringRedisTemplate,
) {
    companion object {
        private const val KEY_PREFIX = "refresh:"
        private val TTL = Duration.ofDays(7)
    }

    fun save(userId: Long, refreshToken: String) {
        redisTemplate.opsForValue().set("$KEY_PREFIX$userId", refreshToken, TTL)
    }

    fun findByUserId(userId: Long): String? {
        return redisTemplate.opsForValue().get("$KEY_PREFIX$userId")
    }

    fun deleteByUserId(userId: Long) {
        redisTemplate.delete("$KEY_PREFIX$userId")
    }
}
