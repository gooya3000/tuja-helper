package com.tujahelper.brokerage.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

/**
 * BrokerageCredential 엔티티 + AES-256 암호화/복호화 단위 테스트
 *
 * Red 단계: 구현 전이므로 컴파일 오류가 발생한다.
 */
class BrokerageCredentialTest {

    // 테스트용 32바이트(256비트) AES 키
    private val testEncryptionKey = "test-aes-key-32bytes!!!!!!!!"

    // -------------------------------------------------------------------------
    // 엔티티 생성 테스트
    // -------------------------------------------------------------------------

    @Test
    fun `BrokerageCredential_생성_시_appKey와_appSecret이_암호화되어_저장된다`() {
        val plainAppKey = "KISDevKey1234567"
        val plainAppSecret = "KISDevSecret9876543210ABCDEFGHIJ"

        val credential = BrokerageCredential(
            userId = 1L,
            appKey = plainAppKey,
            appSecret = plainAppSecret,
            accountNo = "12345678-01",
            encryptionKey = testEncryptionKey,
        )

        // 저장된 값은 평문과 달라야 한다 (암호화됨)
        assertThat(credential.appKey).isNotEqualTo(plainAppKey)
        assertThat(credential.appSecret).isNotEqualTo(plainAppSecret)
    }

    @Test
    fun `BrokerageCredential_getDecryptedAppKey_원래_appKey를_반환한다`() {
        val plainAppKey = "KISDevKey1234567"

        val credential = BrokerageCredential(
            userId = 1L,
            appKey = plainAppKey,
            appSecret = "KISDevSecret9876543210ABCDEFGHIJ",
            accountNo = "12345678-01",
            encryptionKey = testEncryptionKey,
        )

        assertThat(credential.getDecryptedAppKey()).isEqualTo(plainAppKey)
    }

    @Test
    fun `BrokerageCredential_getDecryptedAppSecret_원래_appSecret을_반환한다`() {
        val plainAppSecret = "KISDevSecret9876543210ABCDEFGHIJ"

        val credential = BrokerageCredential(
            userId = 1L,
            appKey = "KISDevKey1234567",
            appSecret = plainAppSecret,
            accountNo = "12345678-01",
            encryptionKey = testEncryptionKey,
        )

        assertThat(credential.getDecryptedAppSecret()).isEqualTo(plainAppSecret)
    }

    @Test
    fun `BrokerageCredential_동일_평문으로_생성된_두_엔티티의_암호문이_다를_수_있다`() {
        // AES-CBC/GCM 모드에서 IV가 랜덤이라면 동일 평문도 다른 암호문이 나온다
        val plainAppKey = "KISDevKey1234567"

        val credential1 = BrokerageCredential(
            userId = 1L,
            appKey = plainAppKey,
            appSecret = "secret",
            accountNo = "12345678-01",
            encryptionKey = testEncryptionKey,
        )
        val credential2 = BrokerageCredential(
            userId = 2L,
            appKey = plainAppKey,
            appSecret = "secret",
            accountNo = "12345678-01",
            encryptionKey = testEncryptionKey,
        )

        // 복호화 값은 동일해야 한다
        assertThat(credential1.getDecryptedAppKey()).isEqualTo(credential2.getDecryptedAppKey())
    }

    @Test
    fun `BrokerageCredential_accountNo는_평문으로_저장된다`() {
        val accountNo = "12345678-01"

        val credential = BrokerageCredential(
            userId = 1L,
            appKey = "KISDevKey1234567",
            appSecret = "KISDevSecret9876543210ABCDEFGHIJ",
            accountNo = accountNo,
            encryptionKey = testEncryptionKey,
        )

        assertThat(credential.accountNo).isEqualTo(accountNo)
    }
}
