package com.tujahelper.config

import com.tujahelper.auth.service.RedisRefreshTokenRepository
import io.mockk.every
import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate

/**
 * 테스트 환경에서 Redis 의존성을 제거하기 위한 설정.
 * 실제 Redis 대신 인메모리 맵으로 동작하는 가짜 구현체를 제공한다.
 */
@TestConfiguration
class EmbeddedRedisConfig {

    @Bean
    @Primary
    fun testRedisConnectionFactory(): RedisConnectionFactory {
        return mockk(relaxed = true)
    }

    @Bean
    @Primary
    fun testStringRedisTemplate(): StringRedisTemplate {
        return mockk(relaxed = true)
    }

    @Bean
    @Primary
    fun testRedisRefreshTokenRepository(): RedisRefreshTokenRepository {
        val store = mutableMapOf<Long, String>()
        val repo = mockk<RedisRefreshTokenRepository>(relaxed = true)
        every { repo.save(any(), any()) } answers {
            val userId = firstArg<Long>()
            val token = secondArg<String>()
            store[userId] = token
        }
        every { repo.findByUserId(any()) } answers {
            store[firstArg<Long>()]
        }
        every { repo.deleteByUserId(any()) } answers {
            store.remove(firstArg<Long>())
            Unit
        }
        return repo
    }
}
