package com.tujahelper.account.service

import com.tujahelper.account.dto.AccountDto
import com.tujahelper.account.dto.BalanceDto
import com.tujahelper.brokerage.service.BrokerageService
import com.tujahelper.common.TujaException
import com.tujahelper.kis.client.KisApiClient
import com.tujahelper.kis.service.KisTokenService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class AccountService(
    private val brokerageService: BrokerageService,
    private val kisTokenService: KisTokenService,
    private val kisApiClient: KisApiClient,
) {
    fun getAccounts(userId: Long): List<AccountDto> {
        val credentials =
            brokerageService.getCredentials(userId)
                ?: throw TujaException("CREDENTIALS_NOT_FOUND", "API 키가 등록되어 있지 않습니다.", HttpStatus.NOT_FOUND)
        return listOf(AccountDto(accountNo = credentials.accountNo, accountName = "종합계좌"))
    }

    fun getBalance(
        userId: Long,
        accountNo: String,
    ): BalanceDto {
        val credentials =
            brokerageService.getCredentials(userId)
                ?: throw TujaException("CREDENTIALS_NOT_FOUND", "API 키가 등록되어 있지 않습니다.", HttpStatus.NOT_FOUND)
        val accessToken = kisTokenService.getToken(userId, credentials.appKey, credentials.appSecret)
        return kisApiClient.getBalance(accessToken, credentials.appKey, credentials.appSecret, accountNo)
    }
}
