package com.tujahelper.brokerage.service

import com.tujahelper.brokerage.domain.BrokerageCredential
import com.tujahelper.brokerage.repository.BrokerageCredentialRepository
import com.tujahelper.common.TujaException
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus

@ExtendWith(MockKExtension::class)
class BrokerageServiceTest {

    private val brokerageCredentialRepository: BrokerageCredentialRepository = mockk()
    private val encryptionService: EncryptionService = mockk()
    private val brokerageService = BrokerageService(brokerageCredentialRepository, encryptionService)

    @Test
    fun `saveCredentials_정상_저장`() {
        val userId = 1L
        val appKey = "my-app-key"
        val appSecret = "my-app-secret"
        val accountNo = "12345678901234"

        every { encryptionService.encrypt(appKey) } returns "encrypted-app-key"
        every { encryptionService.encrypt(appSecret) } returns "encrypted-app-secret"

        val savedCredential = BrokerageCredential(
            id = 1L,
            userId = userId,
            appKey = "encrypted-app-key",
            appSecret = "encrypted-app-secret",
            accountNo = accountNo,
        )
        every { brokerageCredentialRepository.findByUserId(userId) } returns null
        every { brokerageCredentialRepository.save(any()) } returns savedCredential

        val result = brokerageService.saveCredentials(userId, appKey, appSecret, accountNo)

        assertThat(result.userId).isEqualTo(userId)
        verify { encryptionService.encrypt(appKey) }
        verify { encryptionService.encrypt(appSecret) }
    }

    @Test
    fun `saveCredentials_이미_등록된_경우_업데이트`() {
        val userId = 1L
        val existingCredential = BrokerageCredential(
            id = 1L,
            userId = userId,
            appKey = "old-encrypted-key",
            appSecret = "old-encrypted-secret",
            accountNo = "00000000000000",
        )

        every { brokerageCredentialRepository.findByUserId(userId) } returns existingCredential
        every { encryptionService.encrypt("new-app-key") } returns "new-encrypted-key"
        every { encryptionService.encrypt("new-app-secret") } returns "new-encrypted-secret"
        every { brokerageCredentialRepository.save(any()) } returns existingCredential

        brokerageService.saveCredentials(userId, "new-app-key", "new-app-secret", "11111111111111")

        verify { brokerageCredentialRepository.save(any()) }
    }

    @Test
    fun `getCredentials_존재하는_경우_반환`() {
        val userId = 1L
        val credential = BrokerageCredential(
            id = 1L,
            userId = userId,
            appKey = "encrypted-key",
            appSecret = "encrypted-secret",
            accountNo = "12345678901234",
        )

        every { brokerageCredentialRepository.findByUserId(userId) } returns credential
        every { encryptionService.decrypt("encrypted-key") } returns "decrypted-key"
        every { encryptionService.decrypt("encrypted-secret") } returns "decrypted-secret"

        val result = brokerageService.getCredentials(userId)

        assertThat(result).isNotNull()
        assertThat(result!!.appKey).isEqualTo("decrypted-key")
        assertThat(result.appSecret).isEqualTo("decrypted-secret")
        assertThat(result.accountNo).isEqualTo("12345678901234")
    }

    @Test
    fun `getCredentials_없는_경우_null_반환`() {
        val userId = 99L
        every { brokerageCredentialRepository.findByUserId(userId) } returns null

        val result = brokerageService.getCredentials(userId)

        assertThat(result).isNull()
    }
}
