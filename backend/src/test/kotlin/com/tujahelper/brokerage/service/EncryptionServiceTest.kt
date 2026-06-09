package com.tujahelper.brokerage.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EncryptionServiceTest {

    // AES-256 키는 32바이트여야 함
    private val encryptionService = EncryptionService("test-aes-key-32bytes!!!!!!!!!!!")

    @Test
    fun `encrypt_decrypt_정상_복호화`() {
        val plainText = "my-secret-app-key"

        val encrypted = encryptionService.encrypt(plainText)
        val decrypted = encryptionService.decrypt(encrypted)

        assertThat(decrypted).isEqualTo(plainText)
    }

    @Test
    fun `encrypt_같은_값_다른_암호문_생성_랜덤IV`() {
        val plainText = "same-text"

        val encrypted1 = encryptionService.encrypt(plainText)
        val encrypted2 = encryptionService.encrypt(plainText)

        // 랜덤 IV로 인해 매번 다른 암호문이 생성되어야 함
        assertThat(encrypted1).isNotEqualTo(encrypted2)
    }

    @Test
    fun `encrypt_decrypt_한글_처리`() {
        val plainText = "한국투자증권-앱키"

        val encrypted = encryptionService.encrypt(plainText)
        val decrypted = encryptionService.decrypt(encrypted)

        assertThat(decrypted).isEqualTo(plainText)
    }
}
