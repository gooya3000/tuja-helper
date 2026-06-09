package com.tujahelper.brokerage.service

import com.tujahelper.brokerage.domain.BrokerageCredential
import com.tujahelper.brokerage.repository.BrokerageCredentialRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

data class DecryptedCredentials(
    val appKey: String,
    val appSecret: String,
    val accountNo: String,
)

@Service
class BrokerageService(
    private val brokerageCredentialRepository: BrokerageCredentialRepository,
    private val encryptionService: EncryptionService,
) {
    @Transactional
    fun saveCredentials(userId: Long, appKey: String, appSecret: String, accountNo: String): BrokerageCredential {
        val encryptedKey = encryptionService.encrypt(appKey)
        val encryptedSecret = encryptionService.encrypt(appSecret)

        val existing = brokerageCredentialRepository.findByUserId(userId)
        return if (existing != null) {
            existing.appKey = encryptedKey
            existing.appSecret = encryptedSecret
            existing.accountNo = accountNo
            existing.updatedAt = LocalDateTime.now()
            brokerageCredentialRepository.save(existing)
        } else {
            brokerageCredentialRepository.save(
                BrokerageCredential(
                    userId = userId,
                    appKey = encryptedKey,
                    appSecret = encryptedSecret,
                    accountNo = accountNo,
                )
            )
        }
    }

    @Transactional(readOnly = true)
    fun getCredentials(userId: Long): DecryptedCredentials? {
        val credential = brokerageCredentialRepository.findByUserId(userId) ?: return null
        return DecryptedCredentials(
            appKey = encryptionService.decrypt(credential.appKey),
            appSecret = encryptionService.decrypt(credential.appSecret),
            accountNo = credential.accountNo,
        )
    }
}
