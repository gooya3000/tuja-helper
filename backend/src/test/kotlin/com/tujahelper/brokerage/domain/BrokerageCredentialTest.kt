package com.tujahelper.brokerage.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * BrokerageCredential 엔티티 단위 테스트
 *
 * 엔티티 자체는 암호화 책임이 없다.
 * 암호화는 EncryptionService가 담당하며, EncryptionServiceTest에서 검증한다.
 */
class BrokerageCredentialTest {

    @Test
    fun `BrokerageCredential_생성_시_userId가_올바르게_저장된다`() {
        val credential = BrokerageCredential(
            userId = 1L,
            appKey = "someAppKey",
            appSecret = "someAppSecret",
            accountNo = "12345678-01",
        )

        assertThat(credential.userId).isEqualTo(1L)
    }

    @Test
    fun `BrokerageCredential_생성_시_appKey가_그대로_저장된다`() {
        val appKey = "KISDevKey1234567"

        val credential = BrokerageCredential(
            userId = 1L,
            appKey = appKey,
            appSecret = "someAppSecret",
            accountNo = "12345678-01",
        )

        assertThat(credential.appKey).isEqualTo(appKey)
    }

    @Test
    fun `BrokerageCredential_생성_시_appSecret이_그대로_저장된다`() {
        val appSecret = "KISDevSecret9876543210ABCDEFGHIJ"

        val credential = BrokerageCredential(
            userId = 1L,
            appKey = "KISDevKey1234567",
            appSecret = appSecret,
            accountNo = "12345678-01",
        )

        assertThat(credential.appSecret).isEqualTo(appSecret)
    }

    @Test
    fun `BrokerageCredential_생성_시_accountNo가_그대로_저장된다`() {
        val accountNo = "12345678-01"

        val credential = BrokerageCredential(
            userId = 1L,
            appKey = "KISDevKey1234567",
            appSecret = "KISDevSecret9876543210ABCDEFGHIJ",
            accountNo = accountNo,
        )

        assertThat(credential.accountNo).isEqualTo(accountNo)
    }

    @Test
    fun `BrokerageCredential_appKey_업데이트`() {
        val credential = BrokerageCredential(
            userId = 1L,
            appKey = "oldKey",
            appSecret = "someAppSecret",
            accountNo = "12345678-01",
        )

        credential.appKey = "newKey"

        assertThat(credential.appKey).isEqualTo("newKey")
    }

    @Test
    fun `BrokerageCredential_appSecret_업데이트`() {
        val credential = BrokerageCredential(
            userId = 1L,
            appKey = "someAppKey",
            appSecret = "oldSecret",
            accountNo = "12345678-01",
        )

        credential.appSecret = "newSecret"

        assertThat(credential.appSecret).isEqualTo("newSecret")
    }
}
